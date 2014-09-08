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
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.objecthunter.larch.annotations.Concat;
import net.objecthunter.larch.annotations.Permission;
import net.objecthunter.larch.annotations.Permission.ObjectType;
import net.objecthunter.larch.annotations.Permission.PermissionType;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.Entity.EntityType;
import net.objecthunter.larch.model.EntityHierarchy;
import net.objecthunter.larch.model.Rights;
import net.objecthunter.larch.model.Rights.Right;
import net.objecthunter.larch.model.security.Group;
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
        if (u != null && u.getGroups() != null && u.getGroups().contains(Group.ADMINS)) {
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

    private boolean hasPermission(User user, Entity permission,
            Rights.Right... permissionsToCheck) {
        // check for role ADMIN
        if (user != null && user.getGroups() != null &&
                user.getGroups().contains(Group.ADMINS)) {
            return true;
        }

        // get Permissions for user and workspace
        EnumSet<Rights.Right> currentPermissions =
                EnumSet.noneOf(Rights.Right.class);
        if (user != null && permission != null && permission.getRights() != null &&
                permission.getRights().getRights() != null &&
                        permission.getRights().getRights().get(user.getName()) != null) {
            currentPermissions = permission.getRights().getRights().get(user.getName());
        }

        // add default-permissions
        currentPermissions.addAll(getDefaultPermissions());

        return currentPermissions.containsAll(Arrays.asList(permissionsToCheck));
    }

    private void checkCurrentUserPermission(Entity permission, Rights.Right... permissionsToCheck) {
        final User u = this.getCurrentUser();
        if (u != null && u.getGroups() != null && u.getGroups().contains(Group.ADMINS)) {
            return;
        }
        if (!hasPermission(u, permission, permissionsToCheck)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private void checkCurrentUserPermission(String permissionId, Rights.Right... permissionsToCheck)
            throws IOException {
        final Entity permission = backendEntityService.retrieve(permissionId);
        this.checkCurrentUserPermission(permission, permissionsToCheck);
    }

    private Rights.Right metadataReadPermissions(Entity e) throws IOException {
        if (e.getState() != null) {
            if (e.getState().equals(EntityState.PENDING)) {
                return Rights.Right.READ_PENDING_METADATA;
            } else if (e.getState().equals(EntityState.SUBMITTED)) {
                return Rights.Right.READ_SUBMITTED_METADATA;
            } else if (e.getState().equals(EntityState.PUBLISHED)) {
                return Rights.Right.READ_PUBLISHED_METADATA;
            } else if (e.getState().equals(EntityState.WITHDRAWN)) {
                return Rights.Right.READ_WITHDRAWN_METADATA;
            } else {
                throw new IOException("Entity has unknown state: " + e.getState());
            }
        } else {
            throw new IOException("State of Entity may not be null");
        }
    }

    private Rights.Right[] metadataReadWritePermissions(Entity e) throws IOException {
        return new Rights.Right[] { this.metadataReadPermissions(e),
            this.metadataWritePermissions(e) };
    }

    private Rights.Right metadataWritePermissions(Entity e) throws IOException {
        if (e.getState() != null) {
            if (e.getState().equals(EntityState.PENDING)) {
                return Rights.Right.WRITE_PENDING_METADATA;
            } else if (e.getState().equals(EntityState.SUBMITTED)) {
                return Rights.Right.WRITE_SUBMITTED_METADATA;
            } else if (e.getState().equals(EntityState.PUBLISHED)) {
                return Rights.Right.WRITE_PUBLISHED_METADATA;
            } else if (e.getState().equals(EntityState.WITHDRAWN)) {
                return Rights.Right.WRITE_WITHDRAWN_METADATA;
            } else {
                throw new IOException("Entity has unknown state: " + e.getState());
            }
        } else {
            throw new IOException("State of Entity may not be null");
        }
    }

    private Rights.Right binaryReadPermissions(Entity e) throws IOException {
        if (e.getState() != null) {
            if (e.getState().equals(EntityState.PENDING)) {
                return Rights.Right.READ_PENDING_BINARY;
            } else if (e.getState().equals(EntityState.SUBMITTED)) {
                return Rights.Right.READ_SUBMITTED_BINARY;
            } else if (e.getState().equals(EntityState.PUBLISHED)) {
                return Rights.Right.READ_PUBLISHED_BINARY;
            } else if (e.getState().equals(EntityState.WITHDRAWN)) {
                return Rights.Right.READ_WITHDRAWN_BINARY;
            } else {
                throw new IOException("Entity has unknown state: " + e.getState());
            }
        } else {
            throw new IOException("State of Entity may not be null");
        }
    }

    private Rights.Right[] binaryReadWritePermissions(Entity e) throws IOException {
        return new Rights.Right[] { this.binaryReadPermissions(e),
            this.binaryWritePermissions(e) };
    }

    private Rights.Right binaryWritePermissions(Entity e) throws IOException {
        if (e.getState() != null) {
            if (e.getState().equals(EntityState.PENDING)) {
                return Rights.Right.WRITE_PENDING_BINARY;
            } else if (e.getState().equals(EntityState.SUBMITTED)) {
                return Rights.Right.WRITE_SUBMITTED_BINARY;
            } else if (e.getState().equals(EntityState.PUBLISHED)) {
                return Rights.Right.WRITE_PUBLISHED_BINARY;
            } else if (e.getState().equals(EntityState.WITHDRAWN)) {
                return Rights.Right.WRITE_WITHDRAWN_BINARY;
            } else {
                throw new IOException("Entity has unknown state: " + e.getState());
            }
        } else {
            throw new IOException("State of Entity may not be null");
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
        if (workspacePermission.permissionType().equals(PermissionType.NULL)) {
            return;
        }
        Object checkObject = result;
        if (checkObject == null) {
            // get object to check
            if (StringUtils.isBlank(id)) {
                throw new AccessDeniedException("No id provided");
            }
            if (workspacePermission.objectType().equals(ObjectType.ENTITY) ||
                    workspacePermission.objectType().equals(ObjectType.BINARY)) {
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
            if (EntityType.PERMISSION.equals(((Entity) checkObject).getType())) {
                checkPermissionPermissions((Entity) checkObject, workspacePermission.permissionType());
            } else {
                if (workspacePermission.objectType().equals(ObjectType.ENTITY)) {
                    checkEntityPermissions(((Entity) checkObject), permissionId, workspacePermission.permissionType());
                } else if (workspacePermission.objectType().equals(ObjectType.BINARY)) {
                    checkBinaryPermissions(((Entity) checkObject), permissionId, workspacePermission.permissionType());
                } else {
                    throw new AccessDeniedException("wrong workspace-permission provided");
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

    /**
     * Trigger check-method for entity dependent on workspacePermissionType.
     * 
     * @param entity Entity
     * @param workspacePermissionType
     */
    private void checkEntityPermissions(Entity entity, String permissionId, PermissionType workspacePermissionType)
            throws IOException {
        switch (workspacePermissionType) {
        case READ:
            checkCurrentUserPermission(permissionId,
                    metadataReadPermissions(entity));
            break;
        case WRITE:
            checkCurrentUserPermission(permissionId,
                    metadataWritePermissions(entity));
            break;
        case READ_WRITE:
            checkCurrentUserPermission(permissionId,
                    metadataReadWritePermissions(entity));
            break;
        default:
            break;
        }
    }

    /**
     * Trigger check-method for binary dependent on workspacePermissionType.
     * 
     * @param entity Entity
     * @param workspacePermissionType
     */
    private void checkBinaryPermissions(Entity entity, String permissionId, PermissionType workspacePermissionType)
            throws IOException {
        switch (workspacePermissionType) {
        case READ:
            checkCurrentUserPermission(permissionId,
                    binaryReadPermissions(entity));
            break;
        case WRITE:
            checkCurrentUserPermission(permissionId,
                    binaryWritePermissions(entity));
            break;
        case READ_WRITE:
            checkCurrentUserPermission(permissionId,
                    binaryReadWritePermissions(entity));
            break;
        default:
            break;
        }
    }

    /**
     * Trigger check-method for workspace dependent on workspacePermissionType.
     * 
     * @param permission Workspace
     * @param workspacePermissionType
     */
    private void checkPermissionPermissions(Entity permission, PermissionType workspacePermissionType)
            throws IOException {
        switch (workspacePermissionType) {
        case READ:
            checkCurrentUserPermission(permission, Right.READ_PERMISSION);
            break;
        case WRITE:
            checkCurrentUserPermission(permission, Right.WRITE_PERMISSION);
            break;
        case READ_WRITE:
            checkCurrentUserPermission(permission, Right.READ_PERMISSION);
            checkCurrentUserPermission(permission, Right.WRITE_PERMISSION);
            break;
        default:
            break;
        }
    }

    /**
     * Get the Permissions for everybody (also anonymous user).
     * 
     * @return Collection of default-permissions
     */
    private Collection<Right> getDefaultPermissions() {
        Set<Right> defaultPermissions = new HashSet<Right>();
        defaultPermissions.add(Right.READ_PUBLISHED_METADATA);
        defaultPermissions.add(Right.READ_PUBLISHED_BINARY);
        return defaultPermissions;
    }
}
