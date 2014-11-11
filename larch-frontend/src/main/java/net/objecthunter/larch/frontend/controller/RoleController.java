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

package net.objecthunter.larch.frontend.controller;

import java.io.IOException;
import java.io.InputStream;

import net.objecthunter.larch.frontend.util.HttpHelper;

import org.apache.http.entity.InputStreamEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web controller responsible for search views
 */
@Controller
@RequestMapping("/user/{username}")
public class RoleController extends AbstractController {

    @Autowired
    private HttpHelper httpHelper;

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
        httpHelper.doPost("/user/" + username + "/roles", new InputStreamEntity(src, -1), null);
        return new ModelAndView("redirect:/user/" + username);
    }

    /**
     * Controller method for setting a right for an anchorId to a User
     * 
     * @param username The name of the user
     */
    @RequestMapping(value = "/role/{rolename}/rights/{anchorId}", method = RequestMethod.POST, produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView setRightWithAnchorHtml(@PathVariable("username") final String username,
            @PathVariable("rolename") final String rolename, @PathVariable("anchorId") final String anchorId,
            final InputStream src) throws IOException {
        httpHelper.doPost("/user/" + username + "/role/" + rolename + "/rights/" + anchorId, new InputStreamEntity(src, -1), "application/json");
        return new ModelAndView("redirect:/user/" + username);
    }

    /**
     * Controller method for setting a right without anchorId to a User
     * 
     * @param username The name of the user
     */
    @RequestMapping(value = "/role/{rolename}/rights", method = RequestMethod.POST, produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView setRightWithoutAnchorHtml(@PathVariable("username") final String username,
            @PathVariable("rolename") final String rolename,
            final InputStream src) throws IOException {
        httpHelper.doPost("/user/" + username + "/role/" + rolename + "/rights", new InputStreamEntity(src, -1), "application/json");
        return new ModelAndView("redirect:/user/" + username);
    }

}
