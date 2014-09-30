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

package net.objecthunter.larch.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.objecthunter.larch.model.security.ObjectType;
import net.objecthunter.larch.model.security.PermissionType;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.UserRequest;
import net.objecthunter.larch.model.security.annotation.Permission;
import net.objecthunter.larch.model.security.annotation.PreAuth;
import net.objecthunter.larch.model.security.role.Role.RoleName;
import net.objecthunter.larch.service.CredentialsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Web controller class responsible for larch {@link net.objecthunter.larch.model.Binary} objects
 */
@Controller
public class UserController extends AbstractLarchController {

    @Autowired
    private CredentialsService credentialsService;

    /**
     * Controller method for confirming a {@link net.objecthunter.larch.model.security.UserRequest}
     */
    @RequestMapping(value = "/confirm/{token}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String confirmUserRequest(@PathVariable("token") final String token) throws IOException {
        this.credentialsService.retrieveUserRequest(token);
        return token;
    }

    /**
     * Controller method for confirming a {@link net.objecthunter.larch.model.security.UserRequest}
     */
    @RequestMapping(value = "/confirm/{token}", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView confirmUserRequestHtml(@PathVariable("token") final String token) throws IOException {
        this.confirmUserRequest(token);
        final ModelMap model = new ModelMap();
        model.addAttribute("token", token);
        return new ModelAndView("confirm", model);
    }

    /**
     * Controller method for confirming a {@link net.objecthunter.larch.model.security.UserRequest}
     */
    @RequestMapping(value = "/confirm/{token}", method = RequestMethod.POST, consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.OK)
    public void confirmUserRequest(@PathVariable("token") final String token,
            @RequestParam("password") final String password,
            @RequestParam("passwordRepeat") final String passwordRepeat) throws IOException {
        this.credentialsService.createUser(token, password, passwordRepeat);
    }

    /**
     * Controller method for confirming a {@link net.objecthunter.larch.model.security.UserRequest}
     */
    @RequestMapping(value = "/confirm/{token}", method = RequestMethod.POST, consumes = "multipart/form-data",
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView confirmUserRequestHtml(@PathVariable("token") final String token,
            @RequestParam("password") final String password,
            @RequestParam("passwordRepeat") final String passwordRepeat) throws IOException {
        final User u = this.credentialsService.createUser(token, password, passwordRepeat);
        return success("The user " + u.getName() + " has been created.");
    }

    /**
     * Controller method for deleting a given {@link net.objecthunter.larch.model.security.User}
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
     * Controller method for creating a new {@link net.objecthunter.larch.model.security.User}
     * 
     * @param userName the name of the user
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's mail address
     * @param groups the user's groups
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
     * Controller method for creating a new {@link net.objecthunter.larch.model.security.User}
     * 
     * @param userName the name of the user
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's mail address
     * @param groups the user's groups
     * @throws IOException if the user could not be created
     */
    @RequestMapping(value = "/user", method = RequestMethod.POST, consumes = "multipart/form-data",
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView createUserHtml(@RequestParam("name") final String userName,
            @RequestParam("first_name") final String firstName,
            @RequestParam("last_name") final String lastName,
            @RequestParam("email") final String email) throws IOException {
        return new ModelAndView("redirect:/confirm/" + createUser(userName, firstName, lastName, email));
    }

    /**
     * Controller method for retrieving an existing {@link net.objecthunter.larch.model.security.User}s in the
     * repository as a JSON representation
     * 
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
     * Controller method for retrieving an existing {@link net.objecthunter.larch.model.security.User}s in the
     * repository as a JSON representation
     * 
     * @param name The user's name
     * @return A JSON representation of the user
     * @throws IOException
     */
    @RequestMapping(value = "/user/{name}", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.USER, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN),
            @Permission(rolename = RoleName.ROLE_USER_ADMIN, permissionType = PermissionType.READ) })
    public
            ModelAndView retrieveUserHtml(@PathVariable("name") final String name) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("user", credentialsService.retrieveUser(name));
        model.addAttribute("roles", retrieveRoles());
        return new ModelAndView("user", model);
    }

    /**
     * Controller method to retrieve a list of Roles that exist in the
     * repository as a JSON representation
     * 
     * @return the list of {@link net.objecthunter.larch.model.security.Role}s as a JSON representation
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

    @RequestMapping(value = "/user/{name}", method = RequestMethod.POST, consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_USER_ADMIN, permissionType = PermissionType.WRITE) })
    public ModelAndView updateUserDetails(@PathVariable("name") final String username,
            @RequestParam("first_name") final String firstName,
            @RequestParam("last_name") final String lastName,
            @RequestParam("email") final String email) throws IOException {
        final User u = this.credentialsService.retrieveUser(username);
        u.setLastName(lastName);
        u.setFirstName(firstName);
        u.setEmail(email);
        this.credentialsService.updateUser(u);
        return success("The user " + username + " has been updated");
    }

}
