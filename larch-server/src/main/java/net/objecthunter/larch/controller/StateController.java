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

import net.objecthunter.larch.model.security.annotation.Permission;
import net.objecthunter.larch.model.security.annotation.PreAuth;
import net.objecthunter.larch.model.security.role.TestRole.RoleName;
import net.objecthunter.larch.model.state.LarchState;
import net.objecthunter.larch.service.RepositoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Web controller responsible for creating repository state views
 */
@Controller
@RequestMapping("/state")
public class StateController extends AbstractLarchController {

    @Autowired
    private RepositoryService repositoryService;

    /**
     * Controller method for retrieving a {@link net.objecthunter.larch.model.state.LarchState} object describing the
     * repository state using a HTTP GET, that returns a JSON representation
     * 
     * @return a JSON representation of the repository's {@link net.objecthunter.larch.model.state.LarchState}
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(permissions = {
            @Permission(rolename = RoleName.ADMIN) })
    public LarchState state() throws IOException {
        return repositoryService.status();
    }

    /**
     * Controller method for retrieving a {@link net.objecthunter.larch.model.state.LarchState} object describing the
     * repository state using a HTTP GET, that returns a HTML ciew
     * 
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView}
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView stateHtml() throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("state", state());
        return new ModelAndView("state", model);
    }
}
