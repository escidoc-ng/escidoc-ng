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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.objecthunter.larch.annotations.Permission;
import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.security.Right;
import net.objecthunter.larch.model.security.Right.ObjectType;
import net.objecthunter.larch.model.security.Right.PermissionType;
import net.objecthunter.larch.model.security.Rights;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.AuthorizationService;
import net.objecthunter.larch.service.backend.BackendCredentialsService;
import net.objecthunter.larch.service.backend.BackendEntityService;
import net.objecthunter.larch.service.backend.BackendVersionService;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private BackendCredentialsService backendCredentialsService;

    @Override
    public void authorize(Method method, ObjectType objectType, String id, Integer versionId, Object result,
            Permission[] permissions, Object[] methodArgs) throws IOException {
        final User u = this.getCurrentUser();
        if (u == null) {
            // no user logged in but authorization required
            throw new InsufficientAuthenticationException("No user logged in");
        }

        // handle permissions
        handlePermissions(method, permissions, objectType, id, versionId, result, u);
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
     * @param currentUser logged in user
     */
    private void handlePermissions(Method method, Permission[] permissions,
            ObjectType objectType, String id, Integer versionId, Object result, User currentUser)
            throws IOException {
        if (currentUser == null) {
            // no user logged in but authorization required
            throw new InsufficientAuthenticationException("No user logged in");
        }
        Object checkObject = result;
        Map<String, Rights> roles = currentUser.getRoles();
        if (roles == null) {
            roles = new HashMap<String, Rights>();
        }
        roles.putAll(getDefaultRoles(currentUser.getName()));
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
                    checkObject = getCheckObject(objectType, id, versionId);
                }
                if (checkObject instanceof Entity) {
                    String permissionId = getPermissionId((Entity) checkObject);
                    if (permissionId == null) {
                        throw new AccessDeniedException("Object is not below permission");
                    }
                    Set<Right> rights = roles.get(permission.roleName()).getRights(permissionId);
                    if (rights != null) {
                        ObjectType checkObjectType = objectType;
                        if (ObjectType.INPUT_ENTITY.equals(checkObjectType)) {
                            checkObjectType = ObjectType.ENTITY;
                        }
                        for (Right right : rights) {
                            if (right.getObjectType().equals(checkObjectType)) {
                                if (right.getState() == null ||
                                        right.getState().equals(((Entity) checkObject).getState())) {
                                    if ((right.isTree() && (((Entity) checkObject).getId() == null || !((Entity) checkObject)
                                            .getId().equals(permissionId))) ||
                                            (!right.isTree() && ((Entity) checkObject).getId() != null && ((Entity) checkObject)
                                                    .getId().equals(permissionId))) {
                                        if (permission.permissionType().equals(right.getPermissionType())) {
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (checkObject instanceof User) {
                    Set<Right> rights = roles.get(permission.roleName()).getRights(((User) checkObject).getName());
                    if (rights != null) {
                        for (Right right : rights) {
                            if (right.getObjectType().equals(objectType)) {
                                if (permission.permissionType().equals(right.getPermissionType())) {
                                    return;
                                }
                            }
                        }
                    }
                } else {
                    throw new AccessDeniedException("Object has wrong type");
                }
            }
        }
        throw new AccessDeniedException("User may not call method");
    }

    private Object getCheckObject(ObjectType objectType, String id, Integer versionId) throws IOException {
        Object checkObject = null;
        if (StringUtils.isBlank(id)) {
            throw new AccessDeniedException("No id provided");
        }
        if (ObjectType.ENTITY.equals(objectType) ||
                ObjectType.BINARY.equals(objectType)) {
            // get entity
            if (versionId != null) {
                checkObject = backendVersionService.getOldVersion(id, versionId);
            } else {
                checkObject = backendEntityService.retrieve(id);
            }
        } else if (ObjectType.USER.equals(objectType)) {
            // get User
            checkObject = backendCredentialsService.retrieveUser(id);
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

    private Map<String, Rights> getDefaultRoles(String username) throws IOException {
        Map<String, Rights> defaultRoles = new HashMap<String, Rights>();
        Rights rights = new Rights();
        Right readRight = new Right();
        readRight.setObjectType(ObjectType.USER);
        readRight.setPermissionType(PermissionType.READ);
        Right writeRight = new Right();
        writeRight.setObjectType(ObjectType.USER);
        writeRight.setPermissionType(PermissionType.WRITE);
        rights.addRights(username, readRight, writeRight);
        defaultRoles.put("ANY_ROLE", rights);
        return defaultRoles;
    }

}
