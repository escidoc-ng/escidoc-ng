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
import java.util.Collection;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.escidocng.exceptions.NotFoundException;
import de.escidocng.helpers.AuditRecordHelper;
import de.escidocng.model.Binary;
import de.escidocng.model.Entities;
import de.escidocng.model.Entity;
import de.escidocng.model.Metadata;
import de.escidocng.model.security.ObjectType;
import de.escidocng.model.security.PermissionType;
import de.escidocng.model.security.annotation.Permission;
import de.escidocng.model.security.annotation.PostAuth;
import de.escidocng.model.security.annotation.PreAuth;
import de.escidocng.model.security.role.Role.RoleName;
import de.escidocng.model.source.InputStreamSource;
import de.escidocng.service.EntityService;
import de.escidocng.service.MessagingService;
import de.escidocng.service.SchemaService;

/**
 * Web controller responsible for interactions on the entity level
 */
@Controller
@RequestMapping("/entity")
public class EntityController extends AbstractEscidocngController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Controller method for patching an {@link de.escidocng.model.Entity} stored in the repository.<br>
     * The patch method allows only a set of given fields to be updated<br>
     * and therefore allowing for more efficient resource usage.
     * 
     * @param id The id of the {@link de.escidocng.model.Entity} to patch
     * @param src The JSON representation of the subset of fields which should get updated on the Entity
     * @throws IOException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE),
        @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN, permissionType = PermissionType.WRITE) })
    public void patch(@PathVariable("id") final String id,
            final InputStream src) throws IOException {
        final JsonNode node = mapper.readTree(src);
        this.entityService.patch(id, node);
        this.entityService.createAuditRecord(AuditRecordHelper.updateEntityRecord(id));
        this.messagingService.publishUpdateEntity(id);
    }

    /**
     * Controller method for retrieval of a JSON representation of the current version of an
     * {@link de.escidocng.model.Entity}
     * 
     * @param id the {@link de.escidocng.model.Entity}'s id
     * @return An Entity object which gets transformed into a JSON response by Spring MVC
     * @throws IOException
     */
    @RequestMapping("/{id}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PostAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.READ),
        @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN, permissionType = PermissionType.READ) })
    public Entity retrieve(@PathVariable("id") final String id) throws IOException {
        return entityService.retrieve(id);
    }

    /**
     * Controller method for retrieval of a JSON representation of a given version of an
     * {@link de.escidocng.model.Entity}
     * 
     * @param id the {@link de.escidocng.model.Entity}'s id
     * @param version the version number of the Entity version to retrieve
     * @return An Entity object which gets transformed into a JSON response by Spring MVC
     * @throws IOException
     */
    @RequestMapping("/{id}/version/{version}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PostAuth(objectType = ObjectType.ENTITY, idIndex = 0, versionIndex = 1, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.READ),
        @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN, permissionType = PermissionType.READ) })
    public Entity retrieve(@PathVariable("id") final String id, @PathVariable("version") final int version)
            throws IOException {
        return entityService.retrieve(id, version);
    }

    /**
     * Controller method for retrieval of a JSON representation of all versions of an
     * {@link de.escidocng.model.Entity}
     * 
     * @param id the {@link de.escidocng.model.Entity}'s id
     * @return An {@link de.escidocng.model.Entities} object which gets transformed into a JSON response by
     *         Spring MVC
     * @throws IOException
     */
    @RequestMapping("/{id}/versions")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN, permissionType = PermissionType.READ) })
    public Entities retrieveVersions(@PathVariable("id") final String id) throws IOException {
        Entities entities = entityService.getOldVersions(id);
        entities.getEntities().add(0, entityService.retrieve(id));
        return entities;
    }

    /**
     * Controller method for creation of a new {@link de.escidocng.model.Entity} using a HTTP POST with the
     * JSON representation of the entity as the request body.<br><br>
     * NOTE: Use this Method only for Entities with small or no binaries/metadtata-files<br>
     * For Entities with bigger binaries/metadata-files use POST with MultipartRequest.
     * 
     * @param src The Stream injected by Spring MVC containing the JSON representation of the Entity to create.
     * @return The id of the created entity.
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "text/plain")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public String create(final InputStream src)
            throws IOException {
        Entity e = mapper.readValue(src, Entity.class);
        String entityId = helpCreate(e);
        return entityId;
    }

    /**
     * Controller method for creation of a new {@link de.escidocng.model.Entity} using a HTTP POST with a
     * Multipart Form Request.<br>
     * Use this Method for Entities for entities containing bigger binaries/metadata-files.<br>
     * Send Entity-JSON with no source + One Multipart-InputStream for each file.<br><br>
     * Naming of the Multipart-InputStreams:<br>
     * Entity-JSON: entity<br>
     * Binary: binary:{binaryName}<br>
     * Metadata: metadata:{metadataName}<br>
     * Binary-Metadata: binary:{binaryName}metadata:{metadataName}<br><br>
     * All Metadatas and Binaries given in the Entity-JSON must have an associated InputStream
     * 
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST,
            consumes = {
                "multipart/form-data",
                "application/x-www-form-urlencoded",
                "application/octet-stream"})
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuth(objectType = ObjectType.BINARY, idIndex = 0, permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE) })
    public void createMultipart(HttpServletRequest request) throws IOException {
        try {
            Collection<Part> parts = request.getParts();
            HashMap<String, InputStream> hashedParts = new HashMap<String, InputStream>();
            for(Part part : parts) {
                hashedParts.put(part.getName(), part.getInputStream());
            }
            if (!hashedParts.containsKey("entity")) {
                throw new NotFoundException("Part named 'entity' not found");
            }
            Entity e = mapper.readValue(hashedParts.get("entity"), Entity.class);
            for (Binary b : e.getBinaries()) {
                if (!hashedParts.containsKey("binary:" + b.getName())) {
                    throw new NotFoundException("Part named 'binary:" + b.getName() + "' not found");
                }
                b.setSource(new InputStreamSource(hashedParts.get("binary:" + b.getName())));
                for (Metadata m : b.getMetadata()) {
                    if (!hashedParts.containsKey("binary:" + b.getName() + "metadata:" + m.getName())) {
                        throw new NotFoundException("Part named 'binary:" + b.getName() + "metadata:" + m.getName() + "' not found");
                    }
                    m.setSource(new InputStreamSource(hashedParts.get("binary:" + b.getName() + "metadata:" + m.getName())));
                }
            }
            for (Metadata m : e.getMetadata()) {
                if (!hashedParts.containsKey("metadata:" + m.getName())) {
                    throw new NotFoundException("Part named 'metadata:" + m.getName() + "' not found");
                }
                m.setSource(new InputStreamSource(hashedParts.get("metadata:" + m.getName())));
            }
            helpCreate(e);
        } catch (IllegalStateException | ServletException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Helper-Method with which authorization doesnt have to read an InputStream.
     * 
     * @param entity
     * @return The id of the created entity.
     * @throws IOException
     */
    @PreAuth(objectType = ObjectType.INPUT_ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE),
        @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN, permissionType = PermissionType.WRITE) })
    public String helpCreate(final Entity entity) throws IOException {
        final String id = this.entityService.create(entity);
        this.entityService.createAuditRecord(AuditRecordHelper.createEntityRecord(id));
        this.messagingService.publishCreateEntity(id);
        return id;
    }

    /**
     * Controller method for updating an {@link de.escidocng.model.Entity} using a HTTP PUT with a JSON
     * entity representation as request body
     * 
     * @param id The is of the Entity to update
     * @param src The Stream injected by Spring MVC containing the JSON representation of the updated Entity
     * @throws IOException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE),
        @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN, permissionType = PermissionType.WRITE) })
    public void update(@PathVariable("id") final String id,
            final InputStream src) throws IOException {
        final Entity e = mapper.readValue(src, Entity.class);
        if (e.getId() == null) {
            e.setId(id);
        }
        else if (!e.getId().equals(id)) {
            throw new IOException("The id of the Entity and the id used in the PUT request are not the same");
        }
        this.entityService.update(e);
        this.entityService.createAuditRecord(AuditRecordHelper.updateEntityRecord(id));
        this.messagingService.publishUpdateEntity(id);
    }

    /**
     * Controller method for deleting an {@link de.escidocng.model.Entity} using a HTTP DELETE request.
     * 
     * @param id The is of the Entity to delete
     * @throws IOException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE),
        @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN, permissionType = PermissionType.WRITE) })
    public void delete(@PathVariable("id") final String id)
            throws IOException {
        this.entityService.delete(id);
        this.messagingService.publishDeleteEntity(id);
    }

    /**
     * Controller method for publishing an {@link de.escidocng.model.Entity}.<br>
     * Sets the state-attribute of the entity to "published".
     * 
     * @param id The id of the Entity to publish.
     * @throws IOException
     */
    @RequestMapping(value = "/{id}/publish", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE),
        @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN, permissionType = PermissionType.WRITE) })
    public void publish(@PathVariable("id") final String id)
            throws IOException {
        this.entityService.publish(id);
        this.entityService.createAuditRecord(AuditRecordHelper.publishEntityRecord(id));
        this.messagingService.publishPublishEntity(id);
    }

    /**
     * Controller method for submitting an {@link de.escidocng.model.Entity}.<br>
     * Sets the state-attribute of the entity to "submitted".
     * 
     * @param id The id of the Entity to submit.
     * @throws IOException
     */
    @RequestMapping(value = "/{id}/submit", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE),
        @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN, permissionType = PermissionType.WRITE) })
    public void submit(@PathVariable("id") final String id)
            throws IOException {
        this.entityService.submit(id);
        this.entityService.createAuditRecord(AuditRecordHelper.submitEntityRecord(id));
        this.messagingService.publishPublishEntity(id);
    }

    /**
     * Controller method for withdrawing an {@link de.escidocng.model.Entity}.<br>
     * Sets the state-attribute of the entity to "withdrawn".
     * 
     * @param id The id of the Entity to withdraw.
     * @throws IOException
     */
    @RequestMapping(value = "/{id}/withdraw", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE),
        @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN, permissionType = PermissionType.WRITE) })
    public void withdraw(@PathVariable("id") final String id)
            throws IOException {
        this.entityService.withdraw(id);
        this.entityService.createAuditRecord(AuditRecordHelper.withdrawEntityRecord(id));
        this.messagingService.publishWithdrawEntity(id);
    }

    /**
     * Controller method for setting the status of an {@link de.escidocng.model.Entity} to PENDING.<br>
     * Sets the state-attribute of the entity to "pending".
     * 
     * @param id The id of the Entity to set to pending.
     * @throws IOException
     */
    @RequestMapping(value = "/{id}/pending", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN),
        @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE),
        @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN, permissionType = PermissionType.WRITE) })
    public void pending(@PathVariable("id") final String id)
            throws IOException {
        this.entityService.pending(id);
        this.entityService.createAuditRecord(AuditRecordHelper.pendingEntityRecord(id));
        this.messagingService.publishWithdrawEntity(id);
    }

}
