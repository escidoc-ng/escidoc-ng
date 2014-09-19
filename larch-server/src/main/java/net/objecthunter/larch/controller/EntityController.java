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

import net.objecthunter.larch.helpers.AuditRecordHelper;
import net.objecthunter.larch.model.AlternativeIdentifier;
import net.objecthunter.larch.model.Entities;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.security.ObjectType;
import net.objecthunter.larch.model.security.PermissionType;
import net.objecthunter.larch.model.security.annotation.Permission;
import net.objecthunter.larch.model.security.annotation.PostAuth;
import net.objecthunter.larch.model.security.annotation.PreAuth;
import net.objecthunter.larch.model.security.role.Role.RoleName;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.MessagingService;
import net.objecthunter.larch.service.SchemaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web controller responsible for interactions on the entity level
 */
@Controller
@RequestMapping("/entity")
public class EntityController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Controller method for patching an {@link net.objecthunter.larch.model.Entity} stored in the repository. The
     * patch method allows only a set of given fields to be updated and therefore allowing for more efficient resource
     * usage
     * 
     * @param id The id of the {@link net.objecthunter.larch.model.Entity} to patch
     * @param src The JSON representation of the subset of fields which should get updated on the Entity
     * @throws IOException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ADMIN),
        @Permission(rolename = RoleName.USER, permissionType = PermissionType.WRITE),
        @Permission(rolename = RoleName.AREA_ADMIN, permissionType = PermissionType.WRITE)})
    public void patch(@PathVariable("id") final String id,
            final InputStream src) throws IOException {
        final JsonNode node = mapper.readTree(src);
        this.entityService.patch(id, node);
        this.entityService.createAuditRecord(AuditRecordHelper.updateEntityRecord(id));
        this.messagingService.publishUpdateEntity(id);
    }

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
    @PostAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ADMIN),
        @Permission(rolename = RoleName.USER, permissionType = PermissionType.READ),
        @Permission(rolename = RoleName.AREA_ADMIN, permissionType = PermissionType.READ) })
    public Entity
            retrieve(@PathVariable("id") final String id)
                    throws IOException {
        return entityService.retrieve(id);
    }

    /**
     * Controller method for retrieval of a HTML view of the current version of an
     * {@link net.objecthunter.larch.model.Entity}
     * 
     * @param id The is of the {@link net.objecthunter.larch.model.Entity} to retrieve
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView} for rendering the HTML view
     * @throws IOException
     */
    @RequestMapping(value = "/{id}", produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView retrieveHtml(@PathVariable("id") final String id) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("entity", retrieve(id));
        model.addAttribute("metadataTypes", this.schemaService.getSchemaTypes());
        model.addAttribute("identifierTypes", AlternativeIdentifier.IdentifierType.values());
        return new ModelAndView("entity", model);
    }

    /**
     * Controller method for retrieval of a JSON representation of a given version of an
     * {@link net.objecthunter.larch.model.Entity}
     * 
     * @param id the {@link net.objecthunter.larch.model.Entity}'s id
     * @param version the version number of the Entity version to retrieve
     * @return An Entity object which gets transformed into a JSON response by Spring MVC
     * @throws IOException
     */
    @RequestMapping("/{id}/version/{version}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PostAuth(objectType = ObjectType.ENTITY, idIndex = 0, versionIndex = 1, permissions = {
        @Permission(rolename = RoleName.ADMIN),
        @Permission(rolename = RoleName.USER, permissionType = PermissionType.READ),
        @Permission(rolename = RoleName.AREA_ADMIN, permissionType = PermissionType.READ) })
    public Entity retrieve(@PathVariable("id") final String id, @PathVariable("version") final int version)
            throws IOException {
        return entityService.retrieve(id, version);
    }

    /**
     * Controller method for retrieval of a HTML view of a given version of an
     * {@link net.objecthunter.larch.model.Entity}
     * 
     * @param id the {@link net.objecthunter.larch.model.Entity}'s id
     * @param version the version number of the Entity version to retrieve
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView} for rendering the HTML view
     * @throws IOException
     */
    @RequestMapping(value = "/{id}/version/{version}", produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView retrieveHtml(@PathVariable("id") final String id, @PathVariable("version") final int version)
            throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("entity", retrieve(id, version));
        return new ModelAndView("entity", model);
    }

    /**
     * Controller method for retrieval of a JSON representation of all versions of an
     * {@link net.objecthunter.larch.model.Entity}
     * 
     * @param id the {@link net.objecthunter.larch.model.Entity}'s id
     * @return An Entities object which gets transformed into a JSON response by Spring MVC
     * @throws IOException
     */
    @RequestMapping("/{id}/versions")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ADMIN),
        @Permission(rolename = RoleName.AREA_ADMIN, permissionType = PermissionType.READ) })
            public Entities retrieveVersions(@PathVariable("id") final String id) throws IOException {
        Entities entities = entityService.getOldVersions(id);
        entities.getEntities().add(0, entityService.retrieve(id));
        return entities;
    }

    /**
     * Controller method for retrieval of a HTML view of all versions of an
     * {@link net.objecthunter.larch.model.Entity}
     * 
     * @param id the {@link net.objecthunter.larch.model.Entity}'s id
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView} for rendering the HTML view
     * @throws IOException
     */
    @RequestMapping(value = "/{id}/versions", produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView retrieveVersionsHtml(@PathVariable("id") final String id) throws IOException {
        final ModelMap model = new ModelMap();
        Entities entities = retrieveVersions(id);
        model.addAttribute("entities", entities);
        return new ModelAndView("versions", model);
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
    public String create(final InputStream src)
            throws IOException {
        Entity e = mapper.readValue(src, Entity.class);
        if (e.getState() == null) {
            e.setState(EntityState.PENDING);
        }
        String entityId = create(e);
        return entityId;
    }

    /**
     * Helper-Method with which authorization doesnt have to read an InputStream.
     * 
     * @param entity
     * @return The id of the created entity.
     * @throws IOException
     */
    @PreAuth(objectType = ObjectType.INPUT_ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ADMIN),
        @Permission(rolename = RoleName.USER, permissionType = PermissionType.WRITE),
        @Permission(rolename = RoleName.AREA_ADMIN, permissionType = PermissionType.WRITE)})
    public String create(final Entity entity) throws IOException {
        final String id = this.entityService.create(entity);
        this.entityService.createAuditRecord(AuditRecordHelper.createEntityRecord(id));
        this.messagingService.publishCreateEntity(id);
        return id;
    }

    /**
     * Controller method for updating an {@link net.objecthunter.larch.model.Entity} using a HTTP PUT with a JSON
     * entity representation as request body
     * 
     * @param id The is of the Entity to update
     * @param src The Stream injected by Spring MVC containing the JSON representation of the updated Entity
     * @throws IOException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ADMIN),
        @Permission(rolename = RoleName.USER, permissionType = PermissionType.WRITE),
        @Permission(rolename = RoleName.AREA_ADMIN, permissionType = PermissionType.WRITE) })
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
     * Controller method for deleting an {@link net.objecthunter.larch.model.Entity} using a HTTP DELETE request.
     * 
     * @param id The is of the Entity to delete
     * @throws IOException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ADMIN),
        @Permission(rolename = RoleName.USER, permissionType = PermissionType.WRITE),
        @Permission(rolename = RoleName.AREA_ADMIN, permissionType = PermissionType.WRITE) })
    public void delete(@PathVariable("id") final String id)
            throws IOException {
        this.entityService.delete(id);
        this.messagingService.publishDeleteEntity(id);
    }

    @RequestMapping(value = "/{id}/publish", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ADMIN),
        @Permission(rolename = RoleName.USER, permissionType = PermissionType.WRITE),
        @Permission(rolename = RoleName.AREA_ADMIN, permissionType = PermissionType.WRITE) })
    public void publish(@PathVariable("id") final String id)
            throws IOException {
        this.entityService.publish(id);
        this.entityService.createAuditRecord(AuditRecordHelper.publishEntityRecord(id));
        this.messagingService.publishPublishEntity(id);
    }

    @RequestMapping(value = "/{id}/publish", method = RequestMethod.PUT, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView publishHtml(@PathVariable("id") final String id) throws IOException {
        this.publish(id);
        return this.retrieveHtml(id);
    }

    @RequestMapping(value = "/{id}/submit", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ADMIN),
        @Permission(rolename = RoleName.USER, permissionType = PermissionType.WRITE),
        @Permission(rolename = RoleName.AREA_ADMIN, permissionType = PermissionType.WRITE) })
    public void submit(@PathVariable("id") final String id)
            throws IOException {
        this.entityService.submit(id);
        this.entityService.createAuditRecord(AuditRecordHelper.submitEntityRecord(id));
        this.messagingService.publishPublishEntity(id);
    }

    @RequestMapping(value = "/{id}/submit", method = RequestMethod.PUT, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView submitHtml(@PathVariable("id") final String id) throws IOException {
        this.submit(id);
        return this.retrieveHtml(id);
    }

    @RequestMapping(value = "/{id}/withdraw", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, permissions = {
        @Permission(rolename = RoleName.ADMIN),
        @Permission(rolename = RoleName.USER, permissionType = PermissionType.WRITE),
        @Permission(rolename = RoleName.AREA_ADMIN, permissionType = PermissionType.WRITE) })
    public void withdraw(@PathVariable("id") final String id)
            throws IOException {
        this.entityService.withdraw(id);
        this.entityService.createAuditRecord(AuditRecordHelper.publishEntityRecord(id));
        this.messagingService.publishWithdrawEntity(id);
    }

    @RequestMapping(value = "/{id}/withdraw", method = RequestMethod.PUT, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView withdrawHtml(@PathVariable("id") final String id) throws IOException {
        this.withdraw(id);
        return this.retrieveHtml(id);
    }

}
