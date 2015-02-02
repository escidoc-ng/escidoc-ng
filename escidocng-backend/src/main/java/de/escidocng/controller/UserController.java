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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.escidocng.model.security.ObjectType;
import de.escidocng.model.security.PermissionType;
import de.escidocng.model.security.User;
import de.escidocng.model.security.UserRequest;
import de.escidocng.model.security.annotation.Permission;
import de.escidocng.model.security.annotation.PreAuth;
import de.escidocng.model.security.role.Role;
import de.escidocng.model.security.role.Role.RoleName;
import de.escidocng.model.security.role.Role.RoleRight;
import de.escidocng.service.CredentialsService;

/**
 * Web controller class responsible for escidocng {@link de.escidocng.model.User} objects
 */
@Controller
public class UserController extends AbstractEscidocngController {

    @Autowired
    private CredentialsService credentialsService;

    /**
     * Controller method for checking token validity.
     * 
     * @param token confirmation token.
     * @return String confirmation token.
     * @throws IOException if anything goes wrong.
     */
    @RequestMapping(value = "/confirm/{token}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String confirmUserRequest(@PathVariable("token") final String token) throws IOException {
        this.credentialsService.retrieveUserRequest(token);
        return token;
    }

    /**
     * Controller method for confirming a {@link de.escidocng.model.security.UserRequest}
     * Only after confirming a UserRequest, the user can login.
     * 
     * @param token confirmation token.
     * @param password new password to set.
     * @param passwordRepeat repetition of new password to set.
     * @throws IOException if anything goes wrong.
     */
    @RequestMapping(value = "/confirm/{token}", method = RequestMethod.POST, consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.OK)
    public void confirmUserRequest(@PathVariable("token") final String token,
            @RequestParam("password") final String password,
            @RequestParam("passwordRepeat") final String passwordRepeat) throws IOException {
        this.credentialsService.createUser(token, password, passwordRepeat);
    }

    /**
     * Controller method for deleting a given {@link de.escidocng.model.security.User}
     * 
     * @param name name (login) of the user.
     * @throws IOException if anything goes wrong.
     */
    @RequestMapping(value = "/user/{name}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_USER_ADMIN, permissionType = PermissionType.WRITE) })
    public void deleteUser(@PathVariable("name") final String name) throws IOException {
        this.credentialsService.deleteUser(name);
    }

    /**
     * Controller method for creating a new {@link de.escidocng.model.security.User}
     * 
     * @param userName the name (login) of the user
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's mail address
     * @return String confirmation token.
     * @throws IOException if the user could not be created
     */
    @RequestMapping(value = "/user", method = RequestMethod.POST, consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String createUser(@RequestParam("name") final String userName,
            @RequestParam("first_name") final String firstName,
            @RequestParam("last_name") final String lastName,
            @RequestParam("email") final String email) throws IOException {
        final User u = new User();
        u.setName(userName);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        UserRequest request = this.credentialsService.createNewUserRequest(u);
        return request.getToken();
    }

    /**
     * Controller method for retrieving an existing {@link de.escidocng.model.security.User} in the
     * repository as a JSON representation
     * 
     * @param name name (login) of the user.
     * @return A JSON representation of the user
     * @throws IOException
     */
    @RequestMapping(value = "/user/{name}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.USER, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN),
            @Permission(rolename = RoleName.ROLE_USER_ADMIN, permissionType = PermissionType.READ) })
    public
            User retrieveUser(@PathVariable("name") final String name) throws IOException {
        return credentialsService.retrieveUser(name);
    }

    /**
     * Controller method for retrieving the currently logged in {@link de.escidocng.model.security.User} 
     * as a JSON representation
     * 
     * @return A JSON representation of the user.
     * @throws IOException
     */
    @RequestMapping(value = "/current-user", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public User retrieveCurrentUser() throws IOException {
        return credentialsService.retrieveCurrentUser();
    }

    /**
     * Controller method to retrieve a list of Roles that exist in the
     * repository as a JSON representation
     * 
     * @return the list of {@link de.escidocng.model.security.Role}s as a JSON representation
     * @throws IOException
     */
    @RequestMapping(value = "/roles", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<RoleName> retrieveRoles() throws IOException {
        List<RoleName> roles = new ArrayList<RoleName>(Arrays.asList(RoleName.values()));
        roles.remove(RoleName.ROLE_ANY);
        return roles;
    }

    /**
     * Controller method to retrieve a List of allowed {@link de.escidocng.model.security.role.Role.RoleRight}s 
     * for a given {@link de.escidocng.model.security.role.Role} as JSON representation.
     * 
     * @param rolename the name of the role.
     * @return the list of allowed {@link de.escidocng.model.security.role.Role.RoleRight}s as a JSON representation.
     * @throws IOException
     */
    @RequestMapping(value = "/role/{rolename}/rights", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<RoleRight> retrieveRoleRights(@PathVariable("rolename") final String rolename) throws IOException {
        return Role.getRoleObject(RoleName.valueOf(rolename.toUpperCase())).allowedRights();
    }

    /**
     * Controller method to update an existing {@link de.escidocng.model.User}.
     * 
     * @param name the name (login) of the user.
     * @param firstName the new firstname of the user.
     * @param lastName the new lastname of the user.
     * @param email the new email-address of the user.
     * @return String OK-message.
     * @throws IOException
     */
    @RequestMapping(value = "/user/{name}", method = RequestMethod.POST, consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_USER_ADMIN, permissionType = PermissionType.WRITE) })
    public String updateUserDetails(@PathVariable("name") final String username,
            @RequestParam("first_name") final String firstName,
            @RequestParam("last_name") final String lastName,
            @RequestParam("email") final String email) throws IOException {
        final User u = this.credentialsService.retrieveUser(username);
        u.setLastName(lastName);
        u.setFirstName(firstName);
        u.setEmail(email);
        this.credentialsService.updateUser(u);
        return "The user " + username + " has been updated";
    }

}
