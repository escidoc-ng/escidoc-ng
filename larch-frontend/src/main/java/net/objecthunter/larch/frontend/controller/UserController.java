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

package net.objecthunter.larch.frontend.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.objecthunter.larch.frontend.util.HttpHelper;
import net.objecthunter.larch.model.security.ObjectType;
import net.objecthunter.larch.model.security.PermissionType;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.annotation.Permission;
import net.objecthunter.larch.model.security.annotation.PreAuth;
import net.objecthunter.larch.model.security.role.Role.RoleName;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web controller class responsible for larch {@link net.objecthunter.larch.model.security.User} objects
 */
@Controller
public class UserController extends AbstractController {

    @Autowired
    private HttpHelper httpHelper;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Controller method for confirming a {@link net.objecthunter.larch.model.security.UserRequest}
     */
    @RequestMapping(value = "/confirm/{token}", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView confirmUserRequestHtml(@PathVariable("token") final String token) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("token", httpHelper.doGet("/confirm/" + token));
        return new ModelAndView("confirm", model);
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
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("password", password, ContentType.TEXT_PLAIN);
        builder.addTextBody("passwordRepeat", passwordRepeat, ContentType.TEXT_PLAIN);
        HttpEntity multipart = builder.build();
        httpHelper.doPost("/confirm/" + token, multipart);
        return success("The user has been created.");
    }

    /**
     * Controller method for deleting a given {@link net.objecthunter.larch.model.security.User}
     */
    @RequestMapping(value = "/user/{name}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(@PathVariable("name") final String name) throws IOException {
        httpHelper.doDelete("/user/" + name);
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
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("name", userName));
        nvps.add(new BasicNameValuePair("first_name", firstName));
        nvps.add(new BasicNameValuePair("last_name", lastName));
        nvps.add(new BasicNameValuePair("email", email));
        String username = httpHelper.doPost("/user", new UrlEncodedFormEntity(nvps));
        return new ModelAndView("redirect:/confirm/" + username);
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
        model.addAttribute("user", mapper.readValue(httpHelper.doGet("/user/" + name), User.class));
        model.addAttribute("roles", mapper.readValue(httpHelper.doGet("/roles"), new TypeReference<List<RoleName>>() {}));
        return new ModelAndView("user", model);
    }

    /**
     * Controller method for retrieving an existing {@link net.objecthunter.larch.model.security.User}s in the
     * repository as a JSON representation
     * 
     * @param name The user's name
     * @return A JSON representation of the user
     * @throws IOException
     */
    @RequestMapping(value = "/current-user", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public
            ModelAndView retrieveCurrentUserHtml() throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("user", mapper.readValue(httpHelper.doGet("/current-user"), User.class));
        model.addAttribute("roles", mapper.readValue(httpHelper.doGet("/roles"), new TypeReference<List<RoleName>>() {}));
        return new ModelAndView("user", model);
    }

    @RequestMapping(value = "/user/{name}", method = RequestMethod.POST, consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView updateUserDetails(@PathVariable("name") final String username,
            @RequestParam("first_name") final String firstName,
            @RequestParam("last_name") final String lastName,
            @RequestParam("email") final String email) throws IOException {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("first_name", firstName, ContentType.TEXT_PLAIN);
        builder.addTextBody("last_name", lastName, ContentType.TEXT_PLAIN);
        builder.addTextBody("email", email, ContentType.TEXT_PLAIN);
        HttpEntity multipart = builder.build();
        httpHelper.doPost("/user/" + username, multipart);
        return success("The user " + username + " has been updated");
    }

}
