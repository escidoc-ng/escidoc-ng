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

package net.objecthunter.larch.controller;

import java.io.IOException;

import net.objecthunter.larch.helpers.AuditRecordHelper;
import net.objecthunter.larch.model.security.ObjectType;
import net.objecthunter.larch.model.security.PermissionType;
import net.objecthunter.larch.model.security.annotation.Permission;
import net.objecthunter.larch.model.security.annotation.PreAuth;
import net.objecthunter.larch.model.security.role.Role.RoleName;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.MessagingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Web controller responsible for interactions with identifiers of Entities
 */
@Controller
@RequestMapping("/entity/{id}")
public class IdentifierController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private MessagingService messagingService;

    /**
     * Controller method for adding a new identifier using a HTTP POST.
     * 
     * @param entityId the id of the Entity to which the identifier is added
     * @param type the type of the identifier
     * @param value the value of the identifier
     * @throws IOException
     */
    @RequestMapping(value = "/identifier", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE) })
    public void create(@PathVariable("id") final String entityId, @RequestParam("type") final String type,
            @RequestParam("value") final String value) throws IOException {
        this.entityService.createIdentifier(entityId, type, value);
        this.entityService.createAuditRecord(AuditRecordHelper.createIdentifier(entityId));
        this.messagingService.publishCreateIdentifier(entityId, type, value);
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
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE) })
    public void delete(@PathVariable("id") final String entityId, @PathVariable("type") final String type,
            @PathVariable("value") final String value) throws IOException {
        this.entityService.deleteIdentifier(entityId, type, value);
        this.entityService.createAuditRecord(AuditRecordHelper.deleteIdentifier(entityId));
        this.messagingService.publishDeleteIdentifier(entityId, type, value);
    }

}
