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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Web controller for login
 */
@Controller
public class LoginController extends AbstractLarchController {

    /**
     * Controller method for logging in
     * 
     * @return a Spring MVC {@link org.springframework.web.servlet.ModelAndView} for rendering the HTML view
     */
    @RequestMapping(value = "/login-redirect", method = RequestMethod.GET, produces = { "text/html" })
    @PreAuthorize("!hasRole('ROLE_ANONYMOUS')")
    public String loginRedirect() {
        return "redirect:/";
    }

    /**
     * Controller method for logging in
     * 
     * @return a Spring MVC {@link org.springframework.web.servlet.ModelAndView} for rendering the HTML view
     */
    @RequestMapping(value = "/login-page", method = RequestMethod.GET, produces = { "text/html" })
    public ModelAndView login() {
        final ModelMap model = new ModelMap();
        return new ModelAndView("login-page", model);
    }

}
