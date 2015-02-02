/* 
 * Copyright 2014 FIZ Karlsruhe
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

package de.escidocng.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.escidocng.model.security.ObjectType;
import de.escidocng.model.security.PermissionType;
import de.escidocng.model.security.annotation.Permission;
import de.escidocng.model.security.annotation.PreAuth;
import de.escidocng.model.security.role.Role;
import de.escidocng.model.security.role.Role.RoleName;
import de.escidocng.model.security.role.Role.RoleRight;
import de.escidocng.service.EntityService;
import de.escidocng.service.backend.BackendCredentialsService;

/**
 * Web controller responsible for Users Role-Assignment.
 */
@Controller
@RequestMapping("/user/{username}")
public class RoleController extends AbstractEscidocngController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private BackendCredentialsService backendCredentialsService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Controller method for setting user-roles to a User.
     * 
     * @param username The name of the user
     */
    @RequestMapping(value = "/roles", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN) })
    public void setRoles(@PathVariable("username") final String username, final InputStream src) throws IOException {
        List<Role> roles = mapper.readValue(src, new TypeReference<List<Role>>() {});
        backendCredentialsService.setRoles(username, roles);
    }

    /**
     * Controller method for setting a right for an anchorId to a User.
     * 
     * @param username The name of the user
     * @param rolename The name of the Role.
     * @param anchorId The id of the anchor.
     * @param src JSON-Representation of the Role-Rights to set.
     */
    @RequestMapping(value = "/role/{rolename}/rights/{anchorId}", method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 2, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN, permissionType = PermissionType.WRITE),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE) })
    public void setRightWithAnchor(@PathVariable("username") final String username,
            @PathVariable("rolename") final String rolename,
            @PathVariable("anchorId") final String anchorId, final InputStream src) throws IOException {
        List<RoleRight> rights = mapper.readValue(src, new TypeReference<List<RoleRight>>() {});
        backendCredentialsService.setRight(username, RoleName.valueOf(rolename.toUpperCase()), anchorId, rights);
    }

    /**
     * Controller method for setting a right without anchorId to a User.
     * Only allowed for Roles that do not need an anchorId (ADMIN or ADMIN_USER for all Users)
     * 
     * @param username The name of the user
     * @param rolename The name of the Role.
     * @param src JSON-Representation of the Role-Rights to set.
     */
    @RequestMapping(value = "/role/{rolename}/rights", method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN) })
    public void setRightWithoutAnchor(@PathVariable("username") final String username,
            @PathVariable("rolename") final String rolename, final InputStream src) throws IOException {
        List<RoleRight> rights = mapper.readValue(src, new TypeReference<List<RoleRight>>() {});
        backendCredentialsService.setRight(username, RoleName.valueOf(rolename.toUpperCase()), "", rights);
    }

}
