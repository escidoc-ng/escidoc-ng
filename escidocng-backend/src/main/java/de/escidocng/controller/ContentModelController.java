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

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.escidocng.model.ContentModel;
import de.escidocng.model.security.annotation.Permission;
import de.escidocng.model.security.annotation.PreAuth;
import de.escidocng.model.security.role.Role.RoleName;
import de.escidocng.service.ContentModelService;
import de.escidocng.service.MessagingService;

/**
 * Web controller responsible for interactions on the content-model level
 */
@Controller
@RequestMapping("/content-model")
public class ContentModelController extends AbstractEscidocngController {

    @Autowired
    private ContentModelService contentModelService;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Controller method for creation of a new {@link de.escidocng.model.ContentModel} using a HTTP POST with the
     * JSON representation of the ContentModel as the request body
     * 
     * @param src The Stream injected by Spring MVC containing the JSON representation of the ContentModel to create.
     * @return The id of the created ContentModel.
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
     * Controller method for retrieval of a JSON representation of the current version of an
     * {@link de.escidocng.model.ContentModel}
     * 
     * @param id the {@link de.escidocng.model.ContentModel}'s id
     * @return An ContentModel object which gets transformed into a JSON response by Spring MVC
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
     * Controller method for deleting an {@link de.escidocng.model.ContentModel} using a HTTP DELETE request.
     * 
     * @param id The is of the ContentModel to delete
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
