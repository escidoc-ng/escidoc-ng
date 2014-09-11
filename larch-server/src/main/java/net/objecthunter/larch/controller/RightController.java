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
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.objecthunter.larch.annotations.Permission;
import net.objecthunter.larch.annotations.PreAuth;
import net.objecthunter.larch.model.security.Group;
import net.objecthunter.larch.model.security.Rights;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.backend.BackendCredentialsService;

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
@RequestMapping("/user/{name}/rights")
public class RightController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private BackendCredentialsService backendCredentialsService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Controller method for adding user-rights to a User
     * 
     * @param name The name of the user
     */
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(springSecurityExpression = "!isAnonymous()",
    permission = @Permission(idIndex = 0,
            objectType = net.objecthunter.larch.model.security.Right.ObjectType.ENTITY, permissionType = net.objecthunter.larch.model.security.Right.PermissionType.WRITE))
    public void addRights(@PathVariable("name") final String name, final InputStream src) throws IOException {
        User user = backendCredentialsService.retrieveUser(name);
        Map<Group, Rights> roles = mapper.readValue(src, Map.class);
        //user.setRoles(fillRoles(request, user.getRoles()));
        backendCredentialsService.updateUser(user);
    }

    /**
     * Controller method for adding user-rights to a User
     * 
     * @param name The name of the user
     */
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView addRightsHtml(@PathVariable("name") final String name, final InputStream src) throws IOException {
        addRights(name, src);
        return new ModelAndView("redirect:/user/" + name);
    }

    /**
     * Fill all Parameters in Roles-Map.
     * 
     * @param request HttpServletRequest
     * @return Map<Group, Rights>
     */
    private Map<Group, Rights> fillRoles(HttpServletRequest request, Map<Group, Rights> existingRoles) throws IOException {
        Map<Group, Rights> roles = existingRoles;
//        Map<String, String[]> parameters = request.getParameterMap();
//        String entityId = null;
//        Group group = null;
//        EnumSet<Right> rightSet = EnumSet.noneOf(Right.class);
//        for (Entry<String, String[]> parameter : parameters.entrySet()) {
//            if (parameter.getKey().equals("entityId")) {
//                if (parameter.getValue() != null && parameter.getValue().length > 0) {
//                    entityId = parameter.getValue()[0];
//                }
//            } else if (parameter.getKey().equals("group")) {
//                if (parameter.getValue() != null && parameter.getValue().length > 0) {
//                    group = backendCredentialsService.retrieveGroup(parameter.getValue()[0]);
//                }
//            } else if (parameter.getKey().equals("right")) {
//                if (parameter.getValue() != null) {
//                    for (int i = 0; i < parameter.getValue().length; i++) {
//                        rightSet.add(Right.valueOf(parameter.getValue()[i]));
//                    }
//                }
//            }
//        }
//        if (StringUtils.isBlank(entityId)) {
//            throw new IOException("entityId may not be null");
//        }
//        if (group == null) {
//            throw new IOException("group may not be null");
//        }
//        //check if entity exists
//        entityService.retrieve(entityId);
//        
//        //set roles
//        if (roles == null) {
//            roles = new HashMap<Group, Rights>();
//        }
//        Rights rights = roles.get(group);
//        if (rights == null) {
//            rights = new Rights();
//        }
//        rights.setRights(entityId, rightSet);
//        if (!rights.getRights().isEmpty()) {
//            
//        }
//        roles.put(group, rights);
        return roles;
    }
}
