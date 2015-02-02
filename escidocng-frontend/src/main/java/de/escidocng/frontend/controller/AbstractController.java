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
package de.escidocng.frontend.controller;

import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.escidocng.frontend.Constants;
import de.escidocng.model.security.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael Hoppe
 */
public abstract class AbstractController {

    @Autowired
    protected ObjectMapper mapper;

    /**
     * A method to which adds the current User object to the Spring MVC
     * model which s passed to the corresponding templates
     * 
     * @param user The argument used for the user injection
     * @return The User object which gets added to the model by
     *         SpringMVC
     */
    @ModelAttribute("currentuser")
    protected User addUserToModel() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession();
        if (session.getAttribute(Constants.CURRENT_USER_NAME) != null) {
            return (User)session.getAttribute(Constants.CURRENT_USER_NAME);
        }
        return null;
    }

    /**
     * A method that return a success view indicating a completed operation
     * 
     * @param message the success message
     * @return a {@link org.springframework.web.servlet.ModelAndView} that can be returned by web controller methods
     */
    protected ModelAndView success(final String message) {
        return new ModelAndView("success", new ModelMap("successMessage", message));
    }
}
