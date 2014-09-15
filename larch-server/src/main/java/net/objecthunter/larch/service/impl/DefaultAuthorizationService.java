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
import java.util.Map;
import java.util.Set;

import net.objecthunter.larch.annotations.Concat;
import net.objecthunter.larch.annotations.Permission;
import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityType;
import net.objecthunter.larch.model.EntityHierarchy;
import net.objecthunter.larch.model.security.Group;
import net.objecthunter.larch.model.security.Right;
import net.objecthunter.larch.model.security.Right.ObjectType;
import net.objecthunter.larch.model.security.Right.PermissionType;
import net.objecthunter.larch.model.security.Rights;
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
            Permission[] permissions, Object[] methodArgs) throws IOException {
        final User u = this.getCurrentUser();
        if (u == null) {
            // no user logged in but authorization required
            throw new InsufficientAuthenticationException("No user logged in");
        }

        // handle permissions
        handlePermissions(method, permissions, id, versionId, result, u);
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
     * check if permissions match for user
     * 
     * @param method
     * @param workspacePermission
     * @param workspaceId
     * @param entityId
     * @param result
     * @param u logged in user
     */
    private void handlePermissions(Method method, Permission[] permissions,
            String id, Integer versionId, Object result, User u)
            throws IOException {
        if (u == null) {
            // no user logged in but authorization required
            throw new InsufficientAuthenticationException("No user logged in");
        }
        Object checkObject = result;
        Map<String, Rights> roles = u.getRoles();
        for (Permission permission : permissions) {
            if (permission.permissionType().equals(PermissionType.NULL)) {
                if (roles.containsKey(permission.roleName())) {
                    return;
                }
            } else {
                if (!roles.containsKey(permission.roleName())) {
                    continue;
                }
                if (checkObject == null) {
                    checkObject = getCheckObject(permission.objectType(), id, versionId);
                }
                if (checkObject instanceof Entity) {
                    String permissionId = getPermissionId((Entity) checkObject);
                    if (permissionId == null) {
                        throw new AccessDeniedException("Object is not below permission");
                    }
                    final User u = this.getCurrentUser();
                    if (u != null && u.getRoles() != null && u.getRoles().get(Group.USERS.getName()) != null) {
                        Set<Right> rights = u.getRoles().get(Group.USERS.getName()).getRights(permissionId);
                        if (rights != null) {
                            ObjectType checkObjectType = workspacePermission.objectType();
                            if (ObjectType.INPUT_ENTITY.equals(checkObjectType)) {
                                checkObjectType = ObjectType.ENTITY;
                            }
                            for (Right right : rights) {
                                if (right.getObjectType().equals(checkObjectType)) {
                                    if (right.getState() == null ||
                                            right.getState().equals(((Entity) checkObject).getState())) {
                                        if ((right.isTree() && (((Entity) checkObject).getId() == null || !((Entity) checkObject)
                                                .getId().equals(permissionId))) ||
                                                (!right.isTree() && ((Entity) checkObject).getId() != null && ((Entity) checkObject).getId().equals(permissionId))) {
                                            if (workspacePermission.permissionType().equals(right.getPermissionType())) {
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    throw new AccessDeniedException("User may not call method");
                } else {
                    throw new AccessDeniedException("Object has wrong type");
                }
            }
        }

        
        
        
        
        
        
        if (workspacePermission.permissionType().equals(
                net.objecthunter.larch.model.security.Right.PermissionType.NULL)) {
            return;
        }
        if (checkObject == null) {
            // get object to check
            if (StringUtils.isBlank(id)) {
                throw new AccessDeniedException("No id provided");
            }
            if (workspacePermission.objectType()
                    .equals(net.objecthunter.larch.model.security.Right.ObjectType.ENTITY) ||
                    workspacePermission.objectType().equals(
                            net.objecthunter.larch.model.security.Right.ObjectType.BINARY)) {
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
            if (u != null && u.getRoles() != null && u.getRoles().get(Group.USERS.getName()) != null) {
                Set<Right> rights = u.getRoles().get(Group.USERS.getName()).getRights(permissionId);
                if (rights != null) {
                    ObjectType checkObjectType = workspacePermission.objectType();
                    if (ObjectType.INPUT_ENTITY.equals(checkObjectType)) {
                        checkObjectType = ObjectType.ENTITY;
                    }
                    for (Right right : rights) {
                        if (right.getObjectType().equals(checkObjectType)) {
                            if (right.getState() == null ||
                                    right.getState().equals(((Entity) checkObject).getState())) {
                                if ((right.isTree() && (((Entity) checkObject).getId() == null || !((Entity) checkObject)
                                        .getId().equals(permissionId))) ||
                                        (!right.isTree() && ((Entity) checkObject).getId() != null && ((Entity) checkObject).getId().equals(permissionId))) {
                                    if (workspacePermission.permissionType().equals(right.getPermissionType())) {
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            throw new AccessDeniedException("User may not call method");
        } else {
            throw new AccessDeniedException("Object has wrong type");
        }

    }
    
    private Object getCheckObject(ObjectType objectType, String id, Integer versionId) throws IOException {
        Object checkObject = null;
        if (StringUtils.isBlank(id)) {
            throw new AccessDeniedException("No id provided");
        }
        if (objectType
                .equals(ObjectType.ENTITY) ||
                objectType.equals(ObjectType.BINARY)) {
            // get entity
            if (versionId != null) {
                checkObject = backendVersionService.getOldVersion(id, versionId);
            } else {
                checkObject = backendEntityService.retrieve(id);
            }
        }
        if (checkObject == null) {
            throw new AccessDeniedException("Object to check not found");
        }
        return checkObject;
    }

    private String getPermissionId(Entity entity) throws IOException {
        if (StringUtils.isNotBlank(entity.getId())) {
            try {
                return backendEntityService.getPermissionId(entity.getId());
            } catch (NotFoundException e) {
            }
        }
        if (StringUtils.isNotBlank(entity.getParentId())) {
            return backendEntityService.getPermissionId(entity.getParentId());
        } else {
            return null;
        }
    }

}
