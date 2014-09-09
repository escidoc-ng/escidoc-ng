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
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import net.objecthunter.larch.annotations.Permission;
import net.objecthunter.larch.annotations.PreAuth;
import net.objecthunter.larch.annotations.Permission.ObjectType;
import net.objecthunter.larch.annotations.Permission.PermissionType;
import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityType;
import net.objecthunter.larch.model.Rights;
import net.objecthunter.larch.model.Rights.Right;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.backend.BackendCredentialsService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Web controller responsible for search views
 */
@Controller
@RequestMapping("/entity/{id}/rights")
public class RightController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private BackendCredentialsService backendCredentialsService;

    /**
     * Controller method for adding user-rights to a PERMISSION-Entity
     * 
     * @param id The permission-id
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(springSecurityExpression = "!isAnonymous()",
    permission = @Permission(idIndex = 0,
            objectType = ObjectType.ENTITY, permissionType = PermissionType.WRITE))
    public void addRights(@PathVariable("id") final String id, final HttpServletRequest request) throws IOException {
        Entity permission = entityService.retrieve(id);
        if (!EntityType.PERMISSION.equals(permission.getType())) {
            throw new IOException("The type of the Entity must be PERMISSION to set Rights");
        }
        permission.setRights(fillRights(request, permission.getRights()));
        entityService.update(permission);
    }

    /**
     * Controller method for adding user-rights to a PERMISSION-Entity
     * 
     * @param id The permission-id
     */
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView addRightsHtml(@PathVariable("id") final String id, final HttpServletRequest request) throws IOException {
        addRights(id, request);
        return new ModelAndView("redirect:/entity/" + id);
    }

    /**
     * Fill all Parameters in Rights-Object.
     * 
     * @param request HttpServletRequest
     * @return Rights
     */
    private Rights fillRights(HttpServletRequest request, Rights existingRights) throws IOException {
        Rights rights = existingRights;
        Map<String, String[]> parameters = request.getParameterMap();
        String username = null;
        EnumSet<Right> rightSet = EnumSet.noneOf(Right.class);
        for (Entry<String, String[]> parameter : parameters.entrySet()) {
            if (parameter.getKey().equals("user")) {
                if (parameter.getValue() != null && parameter.getValue().length > 0) {
                    username = parameter.getValue()[0];
                }
            } else if (parameter.getKey().equals("right")) {
                if (parameter.getValue() != null) {
                    for (int i = 0; i < parameter.getValue().length; i++) {
                        rightSet.add(Right.valueOf(parameter.getValue()[i]));
                    }
                }
            }
        }
        if (StringUtils.isNotBlank(username)) {
            //check if user exists
            if (!backendCredentialsService.isExistingUser(username)) {
                throw new NotFoundException("User does not exist");
            }
            if (!rightSet.isEmpty()) {
                if (rights == null) {
                    rights = new Rights();
                }
                rights.setRights(username, rightSet);
            } else {
                if (rights != null) {
                    rights.setRights(username, rightSet);
                }
                if (rights.getRights().isEmpty()) {
                    rights = null;
                }
            }
        } else {
            throw new IOException("User may not be null");
        }
        return rights;
    }
}
