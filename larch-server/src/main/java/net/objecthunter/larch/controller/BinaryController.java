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

import javax.servlet.http.HttpServletResponse;

import net.objecthunter.larch.helpers.AuditRecordHelper;
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.security.ObjectType;
import net.objecthunter.larch.model.security.PermissionType;
import net.objecthunter.larch.model.security.annotation.Permission;
import net.objecthunter.larch.model.security.annotation.PreAuth;
import net.objecthunter.larch.model.security.role.TestRole.RoleName;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.MessagingService;
import net.objecthunter.larch.service.SchemaService;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web controller class responsible for larch {@link net.objecthunter.larch.model.Binary} objects
 */
@Controller
public class BinaryController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Controller method for adding a {@link net.objecthunter.larch.model.Binary} to an existing
     * {@link net .objecthunter.larch.model.Entity} using a multipart/form-data encoded HTTP POST
     * 
     * @param entityId The {@link net.objecthunter.larch.model.Entity}'s to which the created Binary should get added.
     * @param name The name of the Binary
     * @param src The request body containing the actual data
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary", method = RequestMethod.POST,
            consumes = {
                "multipart/form-data",
                "application/x-www-form-urlencoded" })
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ADMIN),
            @Permission(rolename = RoleName.USER, permissionType = PermissionType.WRITE) })
    public void create(@PathVariable("id") final String entityId, @RequestParam("name") final String name,
            @RequestParam("mimetype") final String mimeType, final InputStream src) throws IOException {
        entityService.createBinary(entityId, name, mimeType, src);
        entityService.createAuditRecord(AuditRecordHelper.createBinaryRecord(entityId));
        this.messagingService.publishCreateBinary(entityId, name);
    }

    /**
     * Controller method for adding a {@link net.objecthunter.larch.model.Binary} to an existing
     * {@link net .objecthunter.larch.model.Entity} using a application/json POST
     * 
     * @param entityId The {@link net.objecthunter.larch.model.Entity}'s to which the created Binary should get added.
     * @param src An Inputstream holding the request body's content
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary", method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ADMIN),
            @Permission(rolename = RoleName.USER, permissionType = PermissionType.WRITE) })
    public void create(@PathVariable("id") final String entityId, final InputStream src) throws IOException {
        final Binary b = this.mapper.readValue(src, Binary.class);
        this.entityService.createBinary(entityId, b.getName(), b.getMimetype(), b.getSource()
                .getInputStream());
        entityService.createAuditRecord(AuditRecordHelper.createBinaryRecord(entityId));
        this.messagingService.publishCreateBinary(entityId, b.getName());
    }

    /**
     * Controller method for adding a {@link net.objecthunter.larch.model.Binary} to an existing
     * {@link net .objecthunter.larch.model.Entity} using a multipart/form-data encoded HTTP POST
     * 
     * @param entityId The {@link net.objecthunter.larch.model.Entity}'s to which the created Binary should get added.
     * @param name The name of the Binary
     * @param file A {@link org.springframework.web.multipart.MultipartFile} containing the multipart encoded file
     * @return The redirect address to view the updated Entity
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary/file", method = RequestMethod.POST,
            consumes = {
                "multipart/form-data",
                "application/x-www-form-urlencoded" }, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ADMIN),
            @Permission(rolename = RoleName.USER, permissionType = PermissionType.WRITE) })
    public String createHtml(@PathVariable("id") final String entityId, @RequestParam("name") final String name,
            @RequestParam("binary") final MultipartFile file) throws IOException {
        entityService.createBinary(entityId, name, file.getContentType(), file.getInputStream());
        entityService.createAuditRecord(AuditRecordHelper.createBinaryRecord(entityId));
        this.messagingService.publishCreateBinary(entityId, name);
        return "redirect:/entity/" + entityId;
    }

    /**
     * Controller method for adding a {@link net.objecthunter.larch.model.Binary} to an existing
     * {@link net .objecthunter.larch.model.Entity} using a application/json POST
     * 
     * @param entityId The {@link net.objecthunter.larch.model.Entity}'s to which the created Binary should get added.
     * @param src An Inputstream holding the request body's content
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary", method = RequestMethod.POST,
            consumes = "application/json", produces = "text/html")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ADMIN),
            @Permission(rolename = RoleName.USER, permissionType = PermissionType.WRITE) })
    public String createHtml(@PathVariable("id") final String entityId, final InputStream src) throws IOException {
        final Binary b = this.mapper.readValue(src, Binary.class);
        this.entityService.createBinary(entityId, b.getName(), b.getMimetype(), b.getSource()
                .getInputStream());
        entityService.createAuditRecord(AuditRecordHelper.createBinaryRecord(entityId));
        this.messagingService.publishCreateBinary(entityId, b.getName());
        return "redirect:/entity/" + entityId;
    }

    /**
     * Controller method to retrieve a JSON representation of a {@link net.objecthunter.larch.model.Binary} from the
     * repository
     * 
     * @param entityId The {@link net.objecthunter.larch.model.Entity}'s id, which contains the requested Binary
     * @param name The name of the Binary
     * @return The Binary object requested
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary/{name}", method = RequestMethod.GET,
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ADMIN),
            @Permission(rolename = RoleName.USER, permissionType = PermissionType.READ) })
    public Binary retrieve(@PathVariable("id") final String entityId, @PathVariable("name") final String name)
            throws IOException {
        final Entity e = this.entityService.retrieve(entityId);
        if (e.getBinaries() == null || !e.getBinaries().containsKey(name)) {
            throw new IOException("The Binary " + name + " does not exist on the entity " + entityId);
        }
        return e.getBinaries().get(name);
    }

    /**
     * Controller method to retrieve the HTML representation of a {@link net.objecthunter.larch.model.Binary}
     * 
     * @param entityId The {@link net.objecthunter.larch.model.Entity}'s id, which contains the requested Binary
     * @param name The name of the Binary
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView} object used for rendering the HTML
     *         view
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary/{name}", method = RequestMethod.GET,
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView retrieveHtml(@PathVariable("id") final String entityId,
            @PathVariable("name") final String name) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("binary", this.retrieve(entityId, name));
        model.addAttribute("entityId", entityId);
        model.addAttribute("metadataTypes", schemaService.getSchemaTypes());
        return new ModelAndView("binary", model);
    }

    /**
     * Controller method for downloading the content (i.e. The actual bytes) of a
     * {@link net.objecthunter.larch.model .Binary}.
     * 
     * @param id The {@link net.objecthunter.larch.model.Entity}'s id, which contains the requested Binary
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
            @Permission(rolename = RoleName.ADMIN),
            @Permission(rolename = RoleName.USER, permissionType = PermissionType.READ) })
    public void download(@PathVariable("id") final String id,
            @PathVariable("binary-name") final String name,
            final HttpServletResponse response) throws IOException {
        // TODO: Content Size
        final Entity e = entityService.retrieve(id);
        final Binary bin = e.getBinaries().get(name);
        response.setContentType(bin.getMimetype());
        response.setContentLength(-1);
        response.setHeader("Content-Disposition", "inline");
        IOUtils.copy(entityService.retrieveBinary(bin.getPath()), response.getOutputStream());
        response.flushBuffer();
    }

    /**
     * Controller method to delete a binary
     * 
     * @param entityId the entity's id
     * @param name the name of the binary to delete
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary/{name}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ADMIN),
            @Permission(rolename = RoleName.USER, permissionType = PermissionType.WRITE) })
    public void delete(@PathVariable("id") final String entityId, @PathVariable("name") final String name)
            throws IOException {
        this.entityService.deleteBinary(entityId, name);
        this.entityService.createAuditRecord(AuditRecordHelper.deleteBinaryRecord(entityId));
        this.messagingService.publishDeleteBinary(entityId, name);
    }
}
