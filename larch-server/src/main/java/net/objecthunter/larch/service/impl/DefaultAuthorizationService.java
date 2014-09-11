/* 
 * Copyright 2014 Frank Asseg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package net.objecthunter.larch.service.impl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;

import net.objecthunter.larch.annotations.Concat;
import net.objecthunter.larch.annotations.Permission;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.EntityHierarchy;
import net.objecthunter.larch.model.security.Group;
import net.objecthunter.larch.model.security.Right;
import net.objecthunter.larch.model.security.Right.ObjectType;
import net.objecthunter.larch.model.security.Right.PermissionType;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.AuthorizationService;
import net.objecthunter.larch.service.backend.BackendEntityService;
import net.objecthunter.larch.service.backend.BackendVersionService;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.util.MethodInvocationUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DefaultAuthorizationService implements AuthorizationService {

    @Autowired
    private Client client;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private BackendEntityService backendEntityService;

    @Autowired
    private BackendVersionService backendVersionService;

    @Override
    public void authorize(Method method, String id, Integer versionId, Object result,
            String springSecurityExpression,
            Permission workspacePermission, Concat concat, Object[] methodArgs) throws IOException {
        // Admin may do everything
        final User u = this.getCurrentUser();
        if (u != null && u.getRoles() != null && u.getRoles().keySet().contains(Group.ADMINS)) {
            return;
        }

        boolean securityExpressionDenied = false;
        // handle securityExpression
        try {
            handleSecurityExpression(method, springSecurityExpression, methodArgs);
        } catch (Throwable e) {
            if (e instanceof AccessDeniedException) {
                securityExpressionDenied = true;
                if (Concat.AND.equals(concat)) {
                    if (u != null) {
                        throw e;
                    } else {
                        throw new InsufficientAuthenticationException("No user logged in");
                    }
                }
            } else {
                throw e;
            }
        }

        // handle workspacePermission
        try {
            handleWorkspacePermission(method, workspacePermission, id, versionId, result);
        } catch (Throwable e) {
            if (e instanceof AccessDeniedException) {
                if (Concat.AND.equals(concat) || (Concat.OR.equals(concat)) && securityExpressionDenied) {
                    if (u != null) {
                        throw e;
                    } else {
                        throw new InsufficientAuthenticationException("No user logged in");
                    }
                }
            } else {
                throw e;
            }
        }
    }

    private User getCurrentUser() {
        if (SecurityContextHolder.getContext() == null ||
                SecurityContextHolder.getContext().getAuthentication() == null ||
                SecurityContextHolder.getContext().getAuthentication().getPrincipal() == null ||
                !(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof User)) {
            return null;
        }
        return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    /**
     * check if security-expression matches for logged in user
     * 
     * @param method
     * @param springSecurityExpression
     */
    private void handleSecurityExpression(Method method, String springSecurityExpression, Object[] methodArgs) {
        if (StringUtils.isNotBlank(springSecurityExpression)) {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
            Expression accessExpression =
                    handler.getExpressionParser().parseExpression(springSecurityExpression);
            if (!ExpressionUtils.evaluateAsBoolean(accessExpression, handler.createEvaluationContext(
                    a, MethodInvocationUtils.createFromClass(method, method.getDeclaringClass(), method
                            .getName(), method.getParameterTypes(), methodArgs)))) {
                throw new AccessDeniedException("Access denied");
            }
        }
    }

    /**
     * check if workspace-permission matches for logged in user
     * 
     * @param method
     * @param workspacePermission
     * @param workspaceId
     * @param entityId
     * @param result
     */
    private void handleWorkspacePermission(Method method, Permission workspacePermission,
            String id, Integer versionId, Object result)
            throws IOException {
        if (workspacePermission.permissionType().equals(net.objecthunter.larch.model.security.Right.PermissionType.NULL)) {
            return;
        }
        Object checkObject = result;
        if (checkObject == null) {
            // get object to check
            if (StringUtils.isBlank(id)) {
                throw new AccessDeniedException("No id provided");
            }
            if (workspacePermission.objectType().equals(net.objecthunter.larch.model.security.Right.ObjectType.ENTITY) ||
                    workspacePermission.objectType().equals(net.objecthunter.larch.model.security.Right.ObjectType.BINARY)) {
                // get entity
                if (versionId != null) {
                    checkObject = backendVersionService.getOldVersion(id, versionId);
                } else {
                    checkObject = backendEntityService.retrieve(id);
                }
            }
        }

        if (checkObject == null) {
            throw new AccessDeniedException("Object to check not found");
        }
        if (checkObject instanceof Entity) {
            String permissionId = getPermissionId((Entity) checkObject);
            if (permissionId == null) {
                throw new AccessDeniedException("Object is not below permission");
            }
            final User u = this.getCurrentUser();
            Set<Right> rights = u.getRoles().get(Group.USERS).getRights(permissionId);
            for (Right right : rights) {
                if (right.getObjectType().equals(workspacePermission.objectType())) {
                    if (right.getState() == null || right.getState().equals(((Entity) checkObject).getState())) {
                        if ((right.isTree() && !((Entity) checkObject).getId().equals(permissionId)) ||
                                (!right.isTree() && ((Entity) checkObject).getId().equals(permissionId))) {
                            if (workspacePermission.permissionType().equals(right.getPermissionType())) {
                                return;
                            }
                        }
                    }
                }
            }
        } else {
            throw new AccessDeniedException("Object has wrong type");
        }

    }

    private String getPermissionId(Entity entity) throws IOException {
        EntityHierarchy hierarchy = null;
        if (StringUtils.isNotBlank(entity.getId())) {
            hierarchy = backendEntityService.getHierarchy(entity.getId());
        } else if (StringUtils.isNotBlank(entity.getParentId())) {
            hierarchy = backendEntityService.getHierarchy(entity.getParentId());
        } else {
            return null;
        }
        return hierarchy.getPermissionId();
    }

}
