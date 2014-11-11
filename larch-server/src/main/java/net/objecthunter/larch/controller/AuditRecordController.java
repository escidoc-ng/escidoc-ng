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

import net.objecthunter.larch.model.AuditRecords;
import net.objecthunter.larch.model.security.ObjectType;
import net.objecthunter.larch.model.security.annotation.Permission;
import net.objecthunter.larch.model.security.annotation.PreAuth;
import net.objecthunter.larch.model.security.role.Role.RoleName;
import net.objecthunter.larch.service.EntityService;

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
 * Web controller for interaction with {@link net.objecthunter.larch.model.AuditRecord} objects
 */
@Controller
public class AuditRecordController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    /**
     * Controller method for HTTP GET requests of audit records from the repository, describing the provenance of an
     * {@link net.objecthunter.larch.model.Entity}
     * 
     * @param entityId The entity's id for which the {@link net.objecthunter.larch.model.AuditRecord}s should be
     *        returned
     * @param offset The offset for {@link net.objecthunter.larch.model.AuditRecords} returned from the repository
     * @param count The max number of {@link net.objecthunter.larch.model.AuditRecords} returned from the repository
     * @return A {@link java.util.List} of {@link net.objecthunter.larch.model.AuditRecord} objects.
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{entity-id}/audit", method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN)})
    public AuditRecords retrieve(@PathVariable("entity-id") final String entityId, @RequestParam(
                    value = "offset", defaultValue = "0") final int offset, @RequestParam(value = "count",
                    defaultValue = "25") final int count) throws IOException {
        return entityService.retrieveAuditRecords(entityId, offset, count);
    }

}
