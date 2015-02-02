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

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import de.escidocng.model.AlternativeIdentifier;

/**
 * Web controller for login.
 */
@Controller
public class LoginController extends AbstractEscidocngController {

    /**
     * Controller method for logging in.
     * Endpoint that returns the login-form that has to be used by frontend-services to login a user.<br>
     * Login-Form should get returned to the client-browser, therefore this endpoint should be reachable from outside.<br>
     * 
     * @return a Spring MVC {@link org.springframework.web.servlet.ModelAndView} for rendering the HTML view
     */
    @RequestMapping(value = "/login-page", method = RequestMethod.GET, produces = { "text/html" })
    public ModelAndView login() {
        final ModelMap model = new ModelMap();
        return new ModelAndView("login-page", model);
    }

    /**
     * Controller method to show login-success-page.
     * Endpoint that returns the login-success-page.<br>
     * Login-Success-Page should get returned to the client-browser, therefore this endpoint should be reachable from outside.<br>
     * 
     * @return a Spring MVC {@link org.springframework.web.servlet.ModelAndView} for rendering the HTML view
     */
    @RequestMapping(value = "/login-success", method = RequestMethod.GET, produces = { "text/html" })
    public ModelAndView loginSuccess() {
        final ModelMap model = new ModelMap();
        return new ModelAndView("login-success", model);
    }

    /**
     * Controller method for logging out.
     */
    @RequestMapping(value = "/logout-page")
    public ModelAndView logout(HttpServletRequest request) {
        request.getSession().invalidate();
        final ModelMap model = new ModelMap();
        if (request.getParameter("redirectUrl") != null) {
            model.addAttribute("redirectUrl", request.getParameter("redirectUrl"));
            return new ModelAndView("logout-page", model);
        } else {
            model.addAttribute("logout", "true");
            return new ModelAndView("login-page", model);
        }
    }

}
