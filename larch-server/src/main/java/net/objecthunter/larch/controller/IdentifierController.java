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

import net.objecthunter.larch.helpers.AuditRecords;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.MessagingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Web controller responsible for interactions on the identifier level
 */
@Controller
@RequestMapping("/workspace/{workspaceId}/entity/{id}")
public class IdentifierController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private MessagingService messagingService;

    /**
     * Controller method for adding a new identifier using a HTTP POST
     * 
     * @param entityId the id of the Entity to which the identifier is added
     * @param type the type of the identifier
     * @param value the value of the identifier
     * @throws IOException
     */
    @RequestMapping(value = "/identifier", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public void create(@PathVariable("workspaceId") final String workspaceId, @PathVariable("id") final String entityId, @RequestParam("type") final String type,
            @RequestParam("value") final String value) throws IOException {
        this.entityService.createIdentifier(workspaceId, entityId, type, value);
        this.entityService.createAuditRecord(AuditRecords.createIdentifier(entityId));
        this.messagingService.publishCreateIdentifier(entityId, type, value);
    }

    /**
     * Controller method for adding a new identifier using a HTTP POST that redirects to an HTML view of the
     * {@link net.objecthunter.larch.model.Entity}
     * 
     * @param entityId the id of the Entity to which the identifier is added
     * @param type the type of the identifier
     * @param value the value of the identifier
     * @throws IOException
     */
    @RequestMapping(value = "/identifier", method = RequestMethod.POST, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public String createHtml(@PathVariable("workspaceId") final String workspaceId, @PathVariable("id") final String entityId, @RequestParam("type") final String type,
            @RequestParam("value") final String value) throws IOException {
        this.entityService.createIdentifier(workspaceId, entityId, type, value);
        this.entityService.createAuditRecord(AuditRecords.createIdentifier(entityId));
        this.messagingService.publishCreateIdentifier(entityId, type, value);
        return "redirect:/entity/" + entityId;
    }

    /**
     * Controller method to delete an identifier
     * 
     * @param entityId the entity's id
     * @param type the type of the identifier
     * @param value the value of the identifier
     * @throws IOException
     */
    @RequestMapping(value = "/identifier/{type}/{value}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public void delete(@PathVariable("workspaceId") final String workspaceId, @PathVariable("id") final String entityId, @PathVariable("type") final String type,
            @PathVariable("value") final String value) throws IOException {
        this.entityService.deleteIdentifier(workspaceId, entityId, type, value);
        this.entityService.createAuditRecord(AuditRecords.deleteEntityRecord(entityId));
        this.messagingService.publishDeleteIdentifier(entityId, type, value);
    }

    /**
     * Controller method to delete an identifier
     * 
     * @param entityId the entity's id
     * @param type the type of the identifier
     * @param value the value of the identifier
     * @throws IOException
     */
    @RequestMapping(value = "/identifier/{type}/{value}", method = RequestMethod.DELETE,
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public String deleteHtml(@PathVariable("workspaceId") final String workspaceId, @PathVariable("id") final String entityId, @PathVariable("type") final String type,
            @PathVariable("value") final String value) throws IOException {
        this.entityService.deleteIdentifier(workspaceId, entityId, type, value);
        this.entityService.createAuditRecord(AuditRecords.deleteEntityRecord(entityId));
        this.messagingService.publishDeleteIdentifier(entityId, type, value);
        return "redirect:/entity/" + entityId;
    }

}
