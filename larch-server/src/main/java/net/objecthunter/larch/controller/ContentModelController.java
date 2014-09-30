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
import java.io.InputStream;

import net.objecthunter.larch.model.ContentModel;
import net.objecthunter.larch.model.security.annotation.Permission;
import net.objecthunter.larch.model.security.annotation.PreAuth;
import net.objecthunter.larch.model.security.role.Role.RoleName;
import net.objecthunter.larch.service.ContentModelService;
import net.objecthunter.larch.service.MessagingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web controller responsible for interactions on the content-model level
 */
@Controller
@RequestMapping("/content-model")
public class ContentModelController extends AbstractLarchController {

    @Autowired
    private ContentModelService contentModelService;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Controller method for retrieval of a JSON representation of the current version of an
     * {@link net.objecthunter .larch.model.Entity}
     * 
     * @param id the {@link net.objecthunter.larch.model.Entity}'s id
     * @return An Entity object which gets transformed into a JSON response by Spring MVC
     * @throws IOException
     */
    @RequestMapping("/{id}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN) })
    public ContentModel retrieve(@PathVariable("id") final String id) throws IOException {
        return contentModelService.retrieve(id);
    }

    /**
     * Controller method for creation of a new {@link net.objecthunter.larch.model.Entity} using a HTTP POST with the
     * JSON representation of the entity as the request body
     * 
     * @param src The Stream injected by Spring MVC containing the JSON representation of the Entity to create.
     * @return The id of the created entity.
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "text/plain")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @PreAuth(permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN) })
    public String create(final InputStream src)
            throws IOException {
        ContentModel c = mapper.readValue(src, ContentModel.class);
        final String id = this.contentModelService.create(c);
        this.messagingService.publishCreateEntity(id);
        return id;
    }

    /**
     * Controller method for deleting an {@link net.objecthunter.larch.model.Entity} using a HTTP DELETE request.
     * 
     * @param id The is of the Entity to delete
     * @throws IOException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN) })
    public void delete(@PathVariable("id") final String id)
            throws IOException {
        this.contentModelService.delete(id);
        this.messagingService.publishDeleteEntity(id);
    }

}
