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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.helpers.AuditRecordHelper;
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.MetadataType;
import net.objecthunter.larch.model.MetadataValidationResult;
import net.objecthunter.larch.model.security.ObjectType;
import net.objecthunter.larch.model.security.PermissionType;
import net.objecthunter.larch.model.security.annotation.Permission;
import net.objecthunter.larch.model.security.annotation.PreAuth;
import net.objecthunter.larch.model.security.role.Role.RoleName;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.MessagingService;
import net.objecthunter.larch.service.SchemaService;

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

/**
 * Web controller responsible for interaction on the meta data level.
 */
@Controller
public class MetadataController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Controller method for adding {@link net.objecthunter.larch.model.Metadata} with a given name to an
     * {@link net.objecthunter.larch.model.Entity} using a HTTP POST with application/json
     * 
     * @param entityId The is of the Entity to which the Metadata should be added
     * @param src the request body as an InputStream, containing the JSON-Representation of a {@link net.objecthunter.larch.model.Metadata}.
     * @return a redirection to the Entity to which the Metadata was added
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/metadata", method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE) })
    public void addMetadata(@PathVariable("id") final String entityId, final InputStream src) throws IOException {
        final Metadata md = this.mapper.readValue(src, Metadata.class);
        entityService.createMetadata(entityId, md.getName(), md.getType(), md.getMimetype(), md.getSource().getInputStream());
        this.entityService.createAuditRecord(AuditRecordHelper.createMetadataRecord(entityId));
        this.messagingService.publishCreateMetadata(entityId, md.getName());
    }

    /**
     * Controller method for adding {@link net.objecthunter.larch.model.Metadata} with a given name to an
     * {@link net .objecthunter.larch.model.Entity} using a HTTP POST with multipart/form-data.
     * 
     * @param entityId The is of the Entity to which the Metadata should be added
     * @param mdName The name of the Metadata
     * @param type The type of the Metadata
     * @param file The Spring MVC injected MutlipartFile containing the actual data from a html form submission
     * @return a redirection to the Entity to which the Metadata was added
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/metadata", method = RequestMethod.POST,
            consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE) })
    public void addMetadata(@PathVariable("id") final String entityId,
            @RequestParam("name") final String mdName,
            @RequestParam("type") final String type, @RequestParam("data") final MultipartFile file)
            throws IOException {
        entityService.createMetadata(entityId, mdName, type, file.getContentType(), file.getInputStream());
        this.entityService.createAuditRecord(AuditRecordHelper.createMetadataRecord(entityId));
        this.messagingService.publishCreateMetadata(entityId, mdName);
    }

    /**
     * Controller method for adding {@link net.objecthunter.larch.model.Metadata} with a given name to an
     * {@link net .objecthunter.larch.model.Binary} using a HTTP POST with application/json
     * 
     * @param entityId The is of the Entity to which the Metadata should be added
     * @param src the request body as an InputStream
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary/{binary-name}/metadata",
            method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE) })
    public void addBinaryMetadata(@PathVariable("id") final String entityId,
            @PathVariable("binary-name") final String binaryName, final InputStream src) throws IOException {
        final Metadata md = this.mapper.readValue(src, Metadata.class);
        entityService.createBinaryMetadata(entityId, binaryName, md.getName(), md.getType(), md.getMimetype(), md.getSource().getInputStream());
        this.entityService.createAuditRecord(AuditRecordHelper.createBinaryMetadataRecord(entityId));
        this.messagingService.publishCreateBinaryMetadata(entityId, binaryName, md.getName());
    }

    /**
     * Controller method for adding {@link net.objecthunter.larch.model.Metadata} with a given name to an
     * {@link net .objecthunter.larch.model.Binary} using a HTTP POST with multipart/form-data
     * 
     * @param entityId The is of the Entity to which the Metadata should be added
     * @param binaryName the name of the binary
     * @param mdName the meta data set's name
     * @param type the meta data set's type
     * @param file the http multipart file containing the actual bytes
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary/{binary-name}/metadata",
            method = RequestMethod.POST,
            consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE) })
    public void addBinaryMetadata(@PathVariable("id") final String entityId,
            @PathVariable("binary-name") final String binaryName, @RequestParam("name") final String mdName,
            @RequestParam("type") final String type, @RequestParam("data") final MultipartFile file)
            throws IOException {
        entityService.createBinaryMetadata(entityId, binaryName, mdName, type, file.getContentType(), file.getInputStream());
        this.entityService.createAuditRecord(AuditRecordHelper.createBinaryMetadataRecord(entityId));
        this.messagingService.publishCreateBinaryMetadata(entityId, binaryName, mdName);
    }

    /**
     * Controller method to retrieve the XML data of a {@link net.objecthunter.larch.model.Metadata} object of an
     * {@link net.objecthunter.larch.model.Entity} using a HTTP GET
     * 
     * @param id The id of the Entity
     * @param metadataName The name of the Metadata to retrieve
     * @param accept the Spring MVC injected accept header of the HTTP GET request
     * @param resp the Spting MVC injected {@link javax.servlet.http.HttpServletResponse} to which the XML gets
     *        directly written
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "/entity/{id}/metadata/{metadata-name}/content", produces = {
                "application/xml", "text/xml" })
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.READ) })
    public void downloadMetadata(@PathVariable("id") final String id,
            @PathVariable("metadata-name") final String metadataName,
            final HttpServletResponse response) throws IOException {
        final Entity e = entityService.retrieve(id);
        if (e.getMetadata() == null || !e.getMetadata().containsKey(metadataName)) {
            throw new NotFoundException("The Metadata " + metadataName + " does not exist on the entity " + id);
        }
        final Metadata md = e.getMetadata().get(metadataName);
        response.setContentType(md.getMimetype());
        response.setContentLength(-1);
        response.setHeader("Content-Disposition", "inline");
        IOUtils.copy(entityService.retrieveMetadataContent(md.getPath()), response.getOutputStream());
        response.flushBuffer();
    }

    /**
     * Controller method to retrieve the XML data of a {@link net.objecthunter.larch.model.Metadata} object of an
     * {@link net.objecthunter.larch.model.Binary} using a HTTP GET
     * 
     * @param id The id of the Entity
     * @param binaryName the name the name of the binary
     * @param metadataName The name of the Metadata to retrieve
     * @param accept the Spring MVC injected accept header of the HTTP GET request
     * @param resp the Spting MVC injected {@link javax.servlet.http.HttpServletResponse} to which the XML gets
     *        directly written
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "/entity/{id}/binary/{binary-name}/metadata/{metadata-name}/content",
            produces = {
                "application/xml", "text/xml" })
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.READ) })
    public void downloadBinaryMetadata(@PathVariable("id") final String id,
            @PathVariable("binary-name") final String binaryName,
            @PathVariable("metadata-name") final String metadataName,
            final HttpServletResponse response) throws IOException {
        final Entity e = entityService.retrieve(id);
        if (e.getBinaries() == null || !e.getBinaries().containsKey(binaryName)) {
            throw new FileNotFoundException("The binary " + binaryName + " does not exist on entity " + id);
        }
        final Binary bin = e.getBinaries().get(binaryName);
        if (bin.getMetadata() == null || !bin.getMetadata().containsKey(metadataName)) {
            throw new FileNotFoundException("The metadata " + metadataName + " does not exist on the binary "
                    + binaryName + " of the entity " + id);
        }
        final Metadata md = bin.getMetadata().get(metadataName);
        response.setContentType(md.getMimetype());
        response.setContentLength(-1);
        response.setHeader("Content-Disposition", "inline");
        IOUtils.copy(entityService.retrieveMetadataContent(md.getPath()), response.getOutputStream());
        response.flushBuffer();
    }

    /**
     * Controller method to request the validation result for a {@link net.objecthunter.larch.model.Metadata} object
     * of a given {@link net.objecthunter.larch.model.Entity}
     * 
     * @param id the is of the Entity
     * @param metadataName the name of the Metadata
     * @return A JSON representation of a {@link net.objecthunter.larch.model.MetadataValidationResult}
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "/entity/{id}/metadata/{metadata-name}/validate",
            produces = { "application/json" })
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.READ) })
    public MetadataValidationResult validate(@PathVariable("id") final String id,
            @PathVariable("metadata-name") final String metadataName) throws IOException {
        return this.schemaService.validate(id, metadataName);
    }

    /**
     * Controller method to request the validation result for a {@link net.objecthunter.larch.model.Metadata} object
     * of a given {@link net.objecthunter.larch.model.Binary}
     * 
     * @param id the is of the Entity
     * @param binaryName the name of the binary
     * @param metadataName the name of the Metadata
     * @return A JSON representation of a {@link net.objecthunter.larch.model.MetadataValidationResult}
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "/entity/{id}/binary/{binary-name}/metadata/{metadata-name}/validate",
            produces = { "application/json" })
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.READ) })
    public MetadataValidationResult validate(@PathVariable("id") final String id,
            @PathVariable("binary-name") final String binaryName,
            @PathVariable("metadata-name") final String metadataName) throws IOException {
        return this.schemaService.validate(id, binaryName, metadataName);
    }

    /**
     * Controller method to retrieve the available {@link net.objecthunter.larch.model.MetadataType}s in the
     * repository as a JSON representation
     * 
     * @return A JSON representation of a list of MetadataType objects
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/metadatatype", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(permissions = {
        @Permission(rolename = RoleName.ROLE_ANY) })
    public List<MetadataType> retrieveTypes() throws IOException {
        return this.schemaService.getSchemaTypes();
    }

    /**
     * Add a new {@link net.objecthunter.larch.model.MetadataType} to the repository that can be used to validate
     * different kind of Metadata objects.
     * 
     * @param src A JSON representation of the new {@link net.objecthunter.larch.model.MetadataType}
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, value = "/metadatatype", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuth(permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN) })
    public void addSchemaType(final InputStream src) throws IOException {
        final MetadataType newType = mapper.readValue(src, MetadataType.class);
        this.schemaService.createSchemaType(newType);
    }

    /**
     * Controller method to retrieve the XML data of a {@link net.objecthunter.larch.model.Metadata} object of an
     * {@link net.objecthunter.larch.model.Entity} using a HTTP GET
     * 
     * @param id The id of the Entity
     * @param metadataName The name of the Metadata to retrieve
     * @param resp the Spring MVC injected {@link javax.servlet.http.HttpServletResponse} to which the XML gets
     *        directly written
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "/entity/{id}/metadata/{metadata-name}",
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.READ) })
    public Metadata retrieveMetadata(@PathVariable("id") final String entityId,
            @PathVariable("metadata-name") final String mdName) throws IOException {
        final Entity e = this.entityService.retrieve(entityId);
        Metadata md = e.getMetadata().get(mdName);
        if (md == null) {
            throw new NotFoundException("Meta data " + mdName + " does not exist on entity " + entityId);
        }
        return md;
    }

    /**
     * Controller method to retrieve the XML data of a {@link net.objecthunter.larch.model.Metadata} object of an
     * {@link net.objecthunter.larch.model.Binary} using a HTTP GET
     * 
     * @param id The id of the Entity
     * @param binaryName The name of the Binary
     * @param mdName The name of the Metadata to retrieve
     * @param resp the Spring MVC injected {@link javax.servlet.http.HttpServletResponse} to which the XML gets
     *        directly written
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "/entity/{id}/binary/{binary-name}/metadata/{metadata-name}",
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.READ) })
    public Metadata retrieveBinaryMetadata(@PathVariable("id") final String entityId,
            @PathVariable("binary-name") final String binaryName, @PathVariable("metadata-name") final String mdName)
            throws IOException {
        final Entity e = this.entityService.retrieve(entityId);
        final Binary b = e.getBinaries().get(binaryName);
        if (b == null) {
            throw new NotFoundException("Binary " + binaryName + " does not exist on entity " + entityId);
        }
        final Metadata md = b.getMetadata().get(mdName);
        if (md == null) {
            throw new NotFoundException("Meta data " + mdName + " does not exist on Binary " + binaryName
                    + " of entity " + entityId);
        }
        return md;
    }

    /**
     * Controller method to delete a {@link net.objecthunter.larch.model.Metadata} object of an
     * {@link net.objecthunter.larch.model.Entity} using a HTTP DELETE
     * 
     * @param id The id of the Entity
     * @param mdName The name of the Metadata to delete
     * @param resp the Spring MVC injected {@link javax.servlet.http.HttpServletResponse} to which the XML gets
     *        directly written
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.DELETE,
            value = "/entity/{id}/metadata/{metadata-name}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE) })
    public void deleteMetadata(@PathVariable("id") final String entityId,
            @PathVariable("metadata-name") final String mdName) throws IOException {
        this.entityService.deleteMetadata(entityId, mdName);
        this.entityService.createAuditRecord(AuditRecordHelper.deleteMetadataRecord(entityId));
        this.messagingService.publishDeleteMetadata(entityId, mdName);
    }

    /**
     * Controller method to delete a {@link net.objecthunter.larch.model.Metadata} object of an
     * {@link net.objecthunter.larch.model.Binary} using a HTTP DELETE
     * 
     * @param id The id of the Entity
     * @param binaryName The name of the Binary
     * @param mdName The name of the Metadata to delete
     * @param resp the Spring MVC injected {@link javax.servlet.http.HttpServletResponse} to which the XML gets
     *        directly written
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.DELETE,
            value = "/entity/{id}/binary/{binary-name}/metadata/{metadata-name}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE) })
    public void deleteBinaryMetadata(@PathVariable("id") final String entityId,
            @PathVariable("binary-name") final String binaryName, @PathVariable("metadata-name") final String mdName)
            throws IOException {
        this.entityService.deleteBinaryMetadata(entityId, binaryName, mdName);
        this.entityService.createAuditRecord(AuditRecordHelper.deleteBinaryMetadataRecord(entityId));
        this.messagingService.publishDeleteBinaryMetadata(entityId, binaryName, mdName);
    }
}
