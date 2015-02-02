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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.escidocng.helpers.AuditRecordHelper;
import de.escidocng.model.security.ObjectType;
import de.escidocng.model.security.PermissionType;
import de.escidocng.model.security.annotation.Permission;
import de.escidocng.model.security.annotation.PreAuth;
import de.escidocng.model.security.role.Role.RoleName;
import de.escidocng.service.EntityService;
import de.escidocng.service.MessagingService;

/**
 * Web controller responsible for interactions on the relation level
 */
@Controller
@RequestMapping("/entity/{id}/relation")
public class RelationController extends AbstractEscidocngController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private MessagingService messagingService;

    /**
     * Controller method for adding a new triple relating an {@link de.escidocng.model.Entity} via a
     * predicate to an object using a HTTP POST
     * 
     * @param id the id of the Entity which should be the subject of this relation
     * @param predicate the predicate of the relation
     * @param object the object of the relation
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE) })
    public void create(@PathVariable("id") final String id,
            @RequestParam("predicate") final String predicate,
            @RequestParam("object") final String object) throws IOException {
        this.entityService.createRelation(id, predicate, object);
        this.entityService.createAuditRecord(AuditRecordHelper.createRelationRecord(id));
        this.messagingService.publishCreateRelation(id, predicate, object);
    }

}
