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

package net.objecthunter.larch.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.objecthunter.larch.model.security.Right.ObjectType;
import net.objecthunter.larch.model.security.Right.PermissionType;
import net.objecthunter.larch.model.security.annotation.Permission;
import net.objecthunter.larch.model.security.annotation.PreAuth;
import net.objecthunter.larch.model.security.role.TestRole;
import net.objecthunter.larch.model.security.role.TestRole.RoleName;
import net.objecthunter.larch.model.security.role.TestRole.RoleRight;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.backend.BackendCredentialsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web controller responsible for search views
 */
@Controller
@RequestMapping("/user/{username}")
public class RoleController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private BackendCredentialsService backendCredentialsService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Controller method for setting user-roles to a User
     * 
     * @param username The name of the user
     */
    @RequestMapping(value = "/roles", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(permissions = {
        @Permission(rolename = RoleName.ADMIN) })
    public void setRoles(@PathVariable("username") final String username, final InputStream src) throws IOException {
        List<TestRole> roles = mapper.readValue(src, new TypeReference<List<TestRole>>() {});
        backendCredentialsService.setRoles(username, roles);
    }

    /**
     * Controller method for setting user-roles to a User
     * 
     * @param username The name of the user
     */
    @RequestMapping(value = "/roles", method = RequestMethod.POST, produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView setRolesHtml(@PathVariable("username") final String username, final InputStream src)
            throws IOException {
        setRoles(username, src);
        return new ModelAndView("redirect:/user/" + username);
    }

    /**
     * Controller method for setting a right for an object to a User
     * 
     * @param username The name of the user
     * @param objectId The id of the object the user becomes a right for.
     */
    @RequestMapping(value = "/role/{rolename}/right/{objectId}", method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 1, permissions = {
        @Permission(rolename = RoleName.ADMIN),
        @Permission(rolename = RoleName.AREA_ADMIN, permissionType = PermissionType.WRITE) })
    public void setRight(@PathVariable("username") final String username,
            @PathVariable("rolename") final String rolename,
            @PathVariable("objectId") final String objectId, final InputStream src) throws IOException {
        List<RoleRight> rights = mapper.readValue(src, new TypeReference<List<RoleRight>>() {});
        backendCredentialsService.setRight(username, RoleName.valueOf(rolename.toUpperCase()), objectId, rights);
    }

    /**
     * Controller method for setting a right for an object to a User
     * 
     * @param username The name of the user
     */
    @RequestMapping(value = "/role/{rolename}/right/{objectId}", method = RequestMethod.POST, produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView setRightHtml(@PathVariable("username") final String username,
            @PathVariable("rolename") final String rolename, @PathVariable("objectId") final String objectId,
            final InputStream src) throws IOException {
        setRight(username, rolename, objectId, src);
        return new ModelAndView("redirect:/user/" + username);
    }

}
