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

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.escidocng.exceptions.NotFoundException;
import de.escidocng.helpers.AuditRecordHelper;
import de.escidocng.model.Binary;
import de.escidocng.model.Entity;
import de.escidocng.model.security.ObjectType;
import de.escidocng.model.security.PermissionType;
import de.escidocng.model.security.annotation.Permission;
import de.escidocng.model.security.annotation.PreAuth;
import de.escidocng.model.security.role.Role.RoleName;
import de.escidocng.model.source.InputStreamSource;
import de.escidocng.service.EntityService;
import de.escidocng.service.MessagingService;
import de.escidocng.service.SchemaService;

/**
 * Web controller class responsible for escidocng {@link de.escidocng.model.Binary} objects
 */
@Controller
public class BinaryController extends AbstractEscidocngController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Controller method for adding a {@link de.escidocng.model.Binary} to an existing
     * {@link de.escidocng.model.Entity} using a multipart/form-data encoded HTTP POST
     * 
     * @param entityId The {@link de.escidocng.model.Entity}'s to which the created Binary should get added.
     * @param name The name of the Binary
     * @param file The request body containing the actual data
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary", method = RequestMethod.POST,
            consumes = {
                "multipart/form-data",
                "application/x-www-form-urlencoded",
                "application/octet-stream"})
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE) })
    public void createMultipart(@PathVariable("id") final String entityId, @RequestParam("name") final String name,
            @RequestParam("binary") final MultipartFile file) throws IOException {
        Binary binary = new Binary();
        binary.setName(name);
        binary.setMimetype(file.getContentType());
        binary.setFilename(file.getOriginalFilename());
        binary.setSource(new InputStreamSource(file.getInputStream()));
        entityService.createBinary(entityId, binary);
        entityService.createAuditRecord(AuditRecordHelper.createBinaryRecord(entityId));
        this.messagingService.publishCreateBinary(entityId, name);
    }

    /**
     * Controller method for adding a {@link de.escidocng.model.Binary} to an existing
     * {@link de.escidocng.model.Entity} using a application/json POST<br>
     * NOTE: Use this Method only for small binary-files<br>
     * For bigger binary-files use POST with MultipartRequest.
     * 
     * @param entityId The {@link de.escidocng.model.Entity}'s to which the created Binary should get added.
     * @param src An Inputstream holding the request body's content
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary", method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE) })
    public void create(@PathVariable("id") final String entityId, final InputStream src) throws IOException {
        final Binary b = this.mapper.readValue(src, Binary.class);
        this.entityService.createBinary(entityId, b);
        entityService.createAuditRecord(AuditRecordHelper.createBinaryRecord(entityId));
        this.messagingService.publishCreateBinary(entityId, b.getName());
    }

    /**
     * Controller method to retrieve a JSON representation of a {@link de.escidocng.model.Binary} from the
     * repository
     * 
     * @param entityId The {@link de.escidocng.model.Entity}'s id, which contains the requested Binary
     * @param name The name of the Binary
     * @return The Binary object requested
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary/{name}", method = RequestMethod.GET,
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.READ) })
    public Binary retrieve(@PathVariable("id") final String entityId, @PathVariable("name") final String name)
            throws IOException {
        final Entity e = this.entityService.retrieve(entityId);

        if (!e.hasBinary(name)) {
            throw new NotFoundException("The Binary " + name + " does not exist on the entity " + entityId);
        }
        return e.getBinary(name);
    }

    /**
     * Controller method for downloading the content (i.e. The actual bytes) of a
     * {@link de.escidocng.model .Binary}.
     * 
     * @param id The {@link de.escidocng.model.Entity}'s id, which contains the requested Binary
     * @param name The name of the Binary
     * @param response The {@link javax.servlet.http.HttpServletResponse} which gets injected by Spring MVC. This is
     *        used to write the actual byte stream to the client.
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary/{binary-name}/content",
            method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.READ) })
    public void download(@PathVariable("id") final String id,
            @PathVariable("binary-name") final String name,
            final HttpServletResponse response) throws IOException {
        // TODO: Content Size
        final Entity e = entityService.retrieve(id);
        final Binary bin = e.getBinary(name);
        if (bin == null) {
            throw new NotFoundException("The Binary " + name + " does not exist on the entity " + id);
        }
        response.setContentType(bin.getMimetype());
        response.setContentLength(-1);
        response.setHeader("Content-Disposition", "inline");
        IOUtils.copy(entityService.retrieveBinary(bin.getPath()), response.getOutputStream());
        response.flushBuffer();
    }

    /**
     * Controller method to delete a binary.
     * 
     * @param entityId the entity's id
     * @param name the name of the binary to delete
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary/{name}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE) })
    public void delete(@PathVariable("id") final String entityId, @PathVariable("name") final String name)
            throws IOException {
        this.entityService.deleteBinary(entityId, name);
        this.entityService.createAuditRecord(AuditRecordHelper.deleteBinaryRecord(entityId));
        this.messagingService.publishDeleteBinary(entityId, name);
    }
}
