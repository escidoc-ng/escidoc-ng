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

import net.objecthunter.larch.model.security.User;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

public abstract class AbstractLarchController {

    /**
     * A method to which adds the current {@link net.objecthunter.larch.model.security.User} object to the Spring MVC
     * model which s passed to the corresponding templates
     * 
     * @param user The argument used for the user injection
     * @return The {@link net.objecthunter.larch.model.security.User} object which gets added to the model by
     *         SpringMVC
     */
    @ModelAttribute("user")
    protected User addUserToModel(@AuthenticationPrincipal User user) {
        return user;
    }

    protected User getCurrentUser() {
        final Object o = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (o instanceof User) {
            return (User) o;
        } else {
            return null;
        }
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
