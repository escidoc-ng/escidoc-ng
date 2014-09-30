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
 * WITHOUT WARRANTIES OR CONDITIONS OF ROLE_ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package net.objecthunter.larch.service.impl;

import java.io.IOException;
import java.lang.reflect.Method;

import net.objecthunter.larch.exceptions.InvalidParameterException;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.EntityHierarchy;
import net.objecthunter.larch.model.security.ObjectType;
import net.objecthunter.larch.model.security.PermissionType;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.annotation.Permission;
import net.objecthunter.larch.model.security.role.Role.RoleName;
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
            Permission[] permissions) throws IOException {
        final User u = this.getCurrentUser();
        if (u == null) {
            // no user logged in but authorization required
            throw new InsufficientAuthenticationException("No user logged in");
        }
        if (permissions == null) {
            throw new AccessDeniedException("nobody may call method");
        }

        // handle permissions
        handlePermissions(method, permissions, objectType, id, versionId, result, u);
    }

    @Override
    public String getId(final int idIndex, final ObjectType objectType, final Object[] args) {
        if (!ObjectType.INPUT_ENTITY.equals(objectType) && idIndex >= 0 && args != null && args.length > idIndex &&
                args[idIndex] instanceof String) {
            return (String) args[idIndex];
        }
        return null;
    }

    /**
     * Get version-Id from method-parameters
     * 
     * @param versionIndex
     * @param args
     * @return Integer versionId or null
     */
    @Override
    public Integer getVersionId(final int versionIndex, final Object[] args) throws IOException {
        if (versionIndex >= 0 && args != null && args.length > versionIndex &&
                args[versionIndex] instanceof String) {
            try {
                return Integer.parseInt((String) args[versionIndex]);
            } catch (NumberFormatException e) {
                throw new InvalidParameterException("versionId has to be a number: " + args[versionIndex]);
            }
        }
        return null;
    }

    /**
     * Get Entity-Object from method-parameters
     * 
     * @param idIndex
     * @param objectType
     * @param args
     * @return Entity or null
     */
    @Override
    public Entity getObject(final int idIndex, final ObjectType objectType, final Object[] args) {
        if (ObjectType.INPUT_ENTITY.equals(objectType) && idIndex >= 0 && args != null && args.length > idIndex &&
                args[idIndex] instanceof Entity) {
            return (Entity) args[idIndex];
        }
        return null;
    }

    @Override
    public User getCurrentUser() {
        if (SecurityContextHolder.getContext() == null ||
                SecurityContextHolder.getContext().getAuthentication() == null ||
                SecurityContextHolder.getContext().getAuthentication().getPrincipal() == null ||
                !(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof User)) {
            return null;
        }
        return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    /**
     * check if permissions match for user-roles
     * 
     * @param method
     * @param workspacePermission
     * @param entityId
     * @param result
     * @param currentUser logged in user
     */
    private void handlePermissions(Method method, Permission[] permissions,
            ObjectType objectType, String id, Integer versionId, Object result, User currentUser)
            throws IOException {
        Object checkObject = result;
        EntityHierarchy entityHierarchy = null;
        boolean entityHierarchySet = false;
        if (objectType != null && ObjectType.INPUT_ENTITY.equals(objectType)) {
            objectType = ObjectType.ENTITY;
        }
        for (Permission permission : permissions) {
            if (RoleName.ROLE_ANY.equals(permission.rolename())) {
                return;
            } else if (permission.permissionType().equals(PermissionType.NULL)) {
                if (currentUser.hasRole(permission.rolename())) {
                    return;
                }
            } else {
                if (!currentUser.hasRole(permission.rolename())) {
                    continue;
                }
                if (checkObject == null) {
                    checkObject = getCheckObject(objectType, id, versionId);
                }
                if (checkObject instanceof Entity) {
                    if (!entityHierarchySet) {
                        Entity entityObject = (Entity) checkObject;
                        if (entityObject.getContentModelId() != null &&
                                entityObject.getContentModelId().equals(FixedContentModel.LEVEL1.getName())) {
                            entityHierarchy = new EntityHierarchy();
                            entityHierarchy.setLevel1Id(entityObject.getId());
                        } else if (entityObject.getContentModelId() != null &&
                                entityObject.getContentModelId().equals(FixedContentModel.LEVEL2.getName())) {
                            entityHierarchy = new EntityHierarchy();
                            entityHierarchy.setLevel2Id(entityObject.getId());
                            entityHierarchy.setLevel1Id(entityObject.getParentId());
                        } else {
                            if (StringUtils.isNotBlank(entityObject.getParentId())) {
                                entityHierarchy = backendEntityService.getHierarchy(entityObject.getParentId());
                            }
                        }
                        entityHierarchySet = true;
                    }
                }
                if (currentUser.getRole(permission.rolename()).compare(permission, objectType, checkObject,
                        entityHierarchy)) {
                    return;
                }
            }
        }
        throw new AccessDeniedException("User may not call method");
    }

    /**
     * Retrieve Object to evaluate.
     * 
     * @param objectType
     * @param id
     * @param versionId
     * @return Object
     * @throws IOException
     */
    private Object getCheckObject(ObjectType objectType, String id, Integer versionId) throws IOException {
        Object checkObject = null;
        if (StringUtils.isBlank(id)) {
            throw new AccessDeniedException("No id provided");
        }
        try {
            if (ObjectType.ENTITY.equals(objectType) ||
                    ObjectType.BINARY.equals(objectType)) {
                // get entity
                checkObject = backendEntityService.retrieve(id);
                if (versionId != null && versionId != ((Entity) checkObject).getVersion()) {
                    checkObject = backendVersionService.getOldVersion(id, versionId);
                }
            } else if (ObjectType.USER.equals(objectType)) {
                // get User
                checkObject = backendCredentialsService.retrieveUser(id);
            }
        } catch (IOException e) {
            throw new AccessDeniedException("Object to check not found");
        }
        if (checkObject == null) {
            throw new AccessDeniedException("Object to check not found");
        }
        return checkObject;
    }

}
