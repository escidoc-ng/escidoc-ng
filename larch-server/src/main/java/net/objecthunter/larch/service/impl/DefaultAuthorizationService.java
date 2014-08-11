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

import net.objecthunter.larch.annotations.WorkspacePermission;
import net.objecthunter.larch.annotations.WorkspacePermission.ObjectType;
import net.objecthunter.larch.annotations.WorkspacePermission.WorkspacePermissionType;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.model.WorkspacePermissions;
import net.objecthunter.larch.model.WorkspacePermissions.Permission;
import net.objecthunter.larch.model.security.Group;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.AuthorizationService;
import net.objecthunter.larch.service.backend.BackendEntityService;
import net.objecthunter.larch.service.backend.BackendVersionService;
import net.objecthunter.larch.service.backend.BackendWorkspaceService;

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

    @Autowired
    private BackendWorkspaceService backendWorkspaceService;

    @Override
    public void authorize(Method method, String id, Integer versionId, Object result,
            String springSecurityExpression,
            WorkspacePermission workspacePermission) throws IOException {
        // Admin may do everything
        final User u = this.getCurrentUser();
        if (u != null && u.getGroups() != null && u.getGroups().contains(Group.ADMINS)) {
            return;
        }

        try {
            // handle securityExpression
            handleSecurityExpression(method, springSecurityExpression);

            // handle workspacePermission
            handleWorkspacePermission(method, workspacePermission, id, versionId, result);
        } catch (Throwable e) {
            if (e instanceof AccessDeniedException) {
                if (u != null) {
                    throw e;
                } else {
                    throw new InsufficientAuthenticationException("No user logged in");
                }
            }
        }
    }

    private boolean hasPermission(User user, Workspace ws,
            WorkspacePermissions.Permission... permissionsToCheck) {
        // check for role ADMIN
        if (user != null && user.getGroups() != null &&
                user.getGroups().contains(Group.ADMINS)) {
            return true;
        }

        // get Permissions for user and workspace
        EnumSet<WorkspacePermissions.Permission> currentPermissions =
                EnumSet.noneOf(WorkspacePermissions.Permission.class);
        if (user != null && ws != null && ws.getPermissions() != null &&
                ws.getPermissions().getPermissions() != null &&
                ws.getPermissions().getPermissions().get(user.getName()) != null) {
            currentPermissions = ws.getPermissions().getPermissions().get(user.getName());
        }

        // add default-permissions
        currentPermissions.addAll(getDefaultPermissions());

        return currentPermissions.containsAll(Arrays.asList(permissionsToCheck));
    }

    private void checkCurrentUserPermission(Workspace ws, WorkspacePermissions.Permission... permissionsToCheck) {
        final User u = this.getCurrentUser();
        if (u != null && u.getGroups() != null && u.getGroups().contains(Group.ADMINS)) {
            return;
        }
        if (!hasPermission(u, ws, permissionsToCheck)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private void
            checkCurrentUserPermission(String workspaceId, WorkspacePermissions.Permission... permissionsToCheck)
                    throws IOException {
        final Workspace ws = backendWorkspaceService.retrieve(workspaceId);
        this.checkCurrentUserPermission(ws, permissionsToCheck);
    }

    private WorkspacePermissions.Permission metadataReadPermissions(Entity e) throws IOException {
        if (e.getState() != null) {
            if (e.getState().equals(Entity.STATE_PENDING)) {
                return WorkspacePermissions.Permission.READ_PENDING_METADATA;
            } else if (e.getState().equals(Entity.STATE_SUBMITTED)) {
                return WorkspacePermissions.Permission.READ_SUBMITTED_METADATA;
            } else if (e.getState().equals(Entity.STATE_PUBLISHED)) {
                return WorkspacePermissions.Permission.READ_PUBLISHED_METADATA;
            } else if (e.getState().equals(Entity.STATE_WITHDRAWN)) {
                return WorkspacePermissions.Permission.READ_WITHDRAWN_METADATA;
            } else {
                throw new IOException("Entity has unknown state: " + e.getState());
            }
        } else {
            throw new IOException("State of Entity may not be null");
        }
    }

    private WorkspacePermissions.Permission[] metadataReadWritePermissions(Entity e) throws IOException {
        return new WorkspacePermissions.Permission[] { this.metadataReadPermissions(e),
            this.metadataWritePermissions(e) };
    }

    private WorkspacePermissions.Permission metadataWritePermissions(Entity e) throws IOException {
        if (e.getState() != null) {
            if (e.getState().equals(Entity.STATE_PENDING)) {
                return WorkspacePermissions.Permission.WRITE_PENDING_METADATA;
            } else if (e.getState().equals(Entity.STATE_SUBMITTED)) {
                return WorkspacePermissions.Permission.WRITE_SUBMITTED_METADATA;
            } else if (e.getState().equals(Entity.STATE_PUBLISHED)) {
                return WorkspacePermissions.Permission.WRITE_PUBLISHED_METADATA;
            } else if (e.getState().equals(Entity.STATE_WITHDRAWN)) {
                return WorkspacePermissions.Permission.WRITE_WITHDRAWN_METADATA;
            } else {
                throw new IOException("Entity has unknown state: " + e.getState());
            }
        } else {
            throw new IOException("State of Entity may not be null");
        }
    }

    private WorkspacePermissions.Permission binaryReadPermissions(Entity e) throws IOException {
        if (e.getState() != null) {
            if (e.getState().equals(Entity.STATE_PENDING)) {
                return WorkspacePermissions.Permission.READ_PENDING_BINARY;
            } else if (e.getState().equals(Entity.STATE_SUBMITTED)) {
                return WorkspacePermissions.Permission.READ_SUBMITTED_BINARY;
            } else if (e.getState().equals(Entity.STATE_PUBLISHED)) {
                return WorkspacePermissions.Permission.READ_PUBLISHED_BINARY;
            } else if (e.getState().equals(Entity.STATE_WITHDRAWN)) {
                return WorkspacePermissions.Permission.READ_WITHDRAWN_BINARY;
            } else {
                throw new IOException("Entity has unknown state: " + e.getState());
            }
        } else {
            throw new IOException("State of Entity may not be null");
        }
    }

    private WorkspacePermissions.Permission[] binaryReadWritePermissions(Entity e) throws IOException {
        return new WorkspacePermissions.Permission[] { this.binaryReadPermissions(e),
            this.binaryWritePermissions(e) };
    }

    private WorkspacePermissions.Permission binaryWritePermissions(Entity e) throws IOException {
        if (e.getState() != null) {
            if (e.getState().equals(Entity.STATE_PENDING)) {
                return WorkspacePermissions.Permission.WRITE_PENDING_BINARY;
            } else if (e.getState().equals(Entity.STATE_SUBMITTED)) {
                return WorkspacePermissions.Permission.WRITE_SUBMITTED_BINARY;
            } else if (e.getState().equals(Entity.STATE_PUBLISHED)) {
                return WorkspacePermissions.Permission.WRITE_PUBLISHED_BINARY;
            } else if (e.getState().equals(Entity.STATE_WITHDRAWN)) {
                return WorkspacePermissions.Permission.WRITE_WITHDRAWN_BINARY;
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
    private void handleSecurityExpression(Method method, String springSecurityExpression) {
        if (StringUtils.isNotBlank(springSecurityExpression)) {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
            Expression accessExpression =
                    handler.getExpressionParser().parseExpression(springSecurityExpression);
            if (!ExpressionUtils.evaluateAsBoolean(accessExpression, handler.createEvaluationContext(
                    a, MethodInvocationUtils.createFromClass(method.getDeclaringClass(), method
                            .getName())))) {
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
    private void handleWorkspacePermission(Method method, WorkspacePermission workspacePermission,
            String id, Integer versionId, Object result)
            throws IOException {
        if (workspacePermission.workspacePermissionType().equals(WorkspacePermissionType.NULL)) {
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
                }
                checkObject = backendEntityService.retrieve(id);
            } else if (workspacePermission.objectType().equals(ObjectType.WORKSPACE)) {
                // get workspace
                checkObject = backendWorkspaceService.retrieve(id);
            }
        }

        if (checkObject == null) {
            throw new AccessDeniedException("Object to check not found");
        }
        if (checkObject instanceof Entity) {
            if (workspacePermission.objectType().equals(ObjectType.ENTITY)) {
                checkEntityPermissions(((Entity) checkObject), workspacePermission.workspacePermissionType());
            } else if (workspacePermission.objectType().equals(ObjectType.BINARY)) {
                checkBinaryPermissions(((Entity) checkObject), workspacePermission.workspacePermissionType());
            } else {
                throw new AccessDeniedException("wrong workspace-permission provided");
            }
        } else if (checkObject instanceof Workspace) {
            checkWorkspacePermissions(((Workspace) checkObject), workspacePermission.workspacePermissionType());
        } else {
            throw new AccessDeniedException("Object has wrong type");
        }

    }

    /**
     * Trigger check-method for entity dependent on workspacePermissionType.
     * 
     * @param entity Entity
     * @param workspacePermissionType
     */
    private void checkEntityPermissions(Entity entity, WorkspacePermissionType workspacePermissionType)
            throws IOException {
        switch (workspacePermissionType) {
        case READ:
            checkCurrentUserPermission(entity.getWorkspaceId(),
                    metadataReadPermissions(entity));
            break;
        case WRITE:
            checkCurrentUserPermission(entity.getWorkspaceId(),
                    metadataWritePermissions(entity));
            break;
        case READ_WRITE:
            checkCurrentUserPermission(entity.getWorkspaceId(),
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
    private void checkBinaryPermissions(Entity entity, WorkspacePermissionType workspacePermissionType)
            throws IOException {
        switch (workspacePermissionType) {
        case READ:
            checkCurrentUserPermission(entity.getWorkspaceId(),
                    binaryReadPermissions(entity));
            break;
        case WRITE:
            checkCurrentUserPermission(entity.getWorkspaceId(),
                    binaryWritePermissions(entity));
            break;
        case READ_WRITE:
            checkCurrentUserPermission(entity.getWorkspaceId(),
                    binaryReadWritePermissions(entity));
            break;
        default:
            break;
        }
    }

    /**
     * Trigger check-method for workspace dependent on workspacePermissionType.
     * 
     * @param workspace Workspace
     * @param workspacePermissionType
     */
    private void checkWorkspacePermissions(Workspace workspace, WorkspacePermissionType workspacePermissionType)
            throws IOException {
        switch (workspacePermissionType) {
        case READ:
            checkCurrentUserPermission(workspace, Permission.READ_WORKSPACE);
            break;
        case WRITE:
            checkCurrentUserPermission(workspace, Permission.WRITE_WORKSPACE);
            break;
        case READ_WRITE:
            checkCurrentUserPermission(workspace, Permission.READ_WORKSPACE);
            checkCurrentUserPermission(workspace, Permission.WRITE_WORKSPACE);
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
    private Collection<Permission> getDefaultPermissions() {
        Set<Permission> defaultPermissions = new HashSet<Permission>();
        defaultPermissions.add(Permission.READ_PUBLISHED_METADATA);
        defaultPermissions.add(Permission.READ_PUBLISHED_BINARY);
        return defaultPermissions;
    }
}
