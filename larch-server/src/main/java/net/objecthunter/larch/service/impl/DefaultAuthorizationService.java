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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.objecthunter.larch.model.security.ObjectType;
import net.objecthunter.larch.model.security.PermissionType;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.annotation.Permission;
import net.objecthunter.larch.model.security.role.TestRole;
import net.objecthunter.larch.model.security.role.TestRole.RoleName;
import net.objecthunter.larch.model.security.role.TestRole.RoleRight;
import net.objecthunter.larch.model.security.role.TestUserAdminRole;
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
        if (permissions == null) {
            throw new AccessDeniedException("nobody may call method");
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
        Object checkObject = result;
        List<TestRole> userRoles = currentUser.getRoles();
        if (userRoles == null) {
            userRoles = new ArrayList<TestRole>();
        }
        userRoles.addAll(getDefaultRoles(currentUser.getName()));
        for (Permission permission : permissions) {
            if (permission.permissionType().equals(PermissionType.NULL)) {
                if (userHasRole(permission.rolename(), userRoles)) {
                    return;
                }
            } else if (RoleName.ANY.equals(permission.rolename())) {
               return;
            } else {
                if (!userHasRole(permission.rolename(), userRoles)) {
                    continue;
                }
                if (checkObject == null) {
                    checkObject = getCheckObject(objectType, id, versionId);
                }
                for (TestRole userRole : userRoles) {
                    if (userRole.compare(permission, checkObject)) {
                        return;
                    }
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

    private List<TestRole> getDefaultRoles(String username) throws IOException {
        List<TestRole> userDefaultRoles = new ArrayList<TestRole>();
        TestUserAdminRole userAdminRole = new TestUserAdminRole();
        Map<String, List<RoleRight>> rights = new HashMap<String, List<RoleRight>>();
        rights.put(username, new ArrayList<RoleRight>() {{add(RoleRight.READ);add(RoleRight.WRITE);}});
        userAdminRole.setRights(rights);
        userDefaultRoles.add(userAdminRole);
        return userDefaultRoles;
    }
    
    private boolean userHasRole(RoleName roleName, List<TestRole> userRoles) {
        if (roleName == null || userRoles == null) {
            return false;
        }
        for (TestRole role : userRoles) {
            if (roleName.equals(role.getRoleName())) {
                return true;
            }
        }
        return false;
    }

}
