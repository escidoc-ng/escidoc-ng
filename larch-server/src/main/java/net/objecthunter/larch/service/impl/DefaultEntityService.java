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

package net.objecthunter.larch.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.exceptions.AlreadyExistsException;
import net.objecthunter.larch.exceptions.InvalidParameterException;
import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.helpers.SizeCalculatingDigestInputStream;
import net.objecthunter.larch.model.*;
import net.objecthunter.larch.model.source.UrlSource;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.ExportService;
import net.objecthunter.larch.service.backend.*;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

/**
 * The default implementation of {@link net.objecthunter.larch.service.EntityService} responsible for perofrming CRUD
 * operations of {@link net.objecthunter.larch.model.Entity} objects
 */
public class DefaultEntityService implements EntityService {

    private static final Logger log = LoggerFactory.getLogger(DefaultEntityService.class);

    @Autowired
    private BackendAuditService backendAuditService;

    @Autowired
    private BackendBlobstoreService backendBlobstoreService;

    @Autowired
    private BackendVersionService backendVersionService;

    @Autowired
    private BackendEntityService backendEntityService;

    @Autowired
    private BackendWorkspaceService backendWorkspaceService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ExportService exportService;

    @Autowired
    private BackendPublishService backendPublishService;

    @Autowired
    private BackendSchemaService backendSchemaService;

    @Autowired
    private Environment env;

    private boolean autoExport;

    @PostConstruct
    public void init() {
        final String val = env.getProperty("larch.export.auto");
        autoExport = val == null ? false : Boolean.valueOf(val);
    }

    @Override
    public String create(String workspaceId, Entity e) throws IOException {
        e.setWorkspaceId(workspaceId);
        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        if (e.getId() == null || e.getId().isEmpty()) {
            e.setId(generateId());
        } else {
            if (this.backendEntityService.exists(e.getId())) {
                throw new AlreadyExistsException("Entity with id " + e.getId()
                        + " could not be created because it already exists in the index");
            }
        }
        if (e.getMetadata() != null) {
            for (final Metadata md : e.getMetadata().values()) {
                md.setUtcCreated(now);
                md.setUtcLastModified(now);
            }
        }
        if (e.getLabel() == null || e.getLabel().isEmpty()) {
            e.setLabel("Unnamed entity");
        }
        if (e.getBinaries() != null) {
            for (final Binary b : e.getBinaries().values()) {
                createAndMutateBinary(e.getId(), b);
            }
        }
        e.setState(Entity.STATE_PENDING);
        e.setVersion(1);
        e.setUtcCreated(now);
        e.setUtcLastModified(now);
        final String id = this.backendEntityService.create(e);
        log.debug("finished creating Entity {}", id);

        // export the created entity
        if (autoExport) {
            exportService.export(e);
            log.debug("exported entity {} ", id);
        }

        return id;
    }

    private void createAndMutateBinary(String entityId, Binary b) throws IOException {
        if (b.getSource() == null) {
            log.warn("No source is set for binary '{}' of entity '{}' nothing to ingest", b.getName(), entityId);
            return;
        }
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        try (final SizeCalculatingDigestInputStream src =
                new SizeCalculatingDigestInputStream(b.getSource().getInputStream(), digest)) {
            final ZonedDateTime created = ZonedDateTime.now(ZoneOffset.UTC);
            final String path = this.backendBlobstoreService.create(src);
            final String checksum = new BigInteger(1, digest.digest()).toString(16);
            b.setChecksum(checksum);
            b.setSize(src.getCalculatedSize());
            b.setChecksumType(digest.getAlgorithm());
            b.setPath(path);
            b.setSource(new UrlSource(URI.create("http://localhost:8080/entity/" + entityId + "/binary/" +
                    b.getName()
                    + "/content"), true));
            final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
            b.setUtcCreated(now);
            b.setUtcLastModified(now);
            if (b.getMetadata() != null) {
                for (final Metadata md : b.getMetadata().values()) {
                    md.setUtcCreated(now);
                    md.setUtcLastModified(now);
                }
            }
        }
    }

    private String generateId() throws IOException {
        String generated;
        do {
            generated = RandomStringUtils.randomAlphabetic(16);
        } while (backendEntityService.exists(generated));
        return generated;
    }

    @Override
    public void update(String workspaceId, Entity e) throws IOException {
        final Entity oldVersion = this.backendEntityService.retrieve(workspaceId, e.getId());
        this.backendVersionService.addOldVersion(oldVersion);
        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        e.setVersionAndResetState(oldVersion.getVersion() + 1);
        if (e.getMetadata() != null) {
            for (final Metadata md : e.getMetadata().values()) {
                if (md.getUtcCreated() == null) {
                    md.setUtcCreated(now);
                }
                md.setUtcLastModified(now);
            }
        }
        e.setUtcCreated(oldVersion.getUtcCreated());
        e.setUtcLastModified(now);
        if (e.getLabel() == null || e.getLabel().isEmpty()) {
            e.setLabel("Unnamed entity");
        }
        if (e.getBinaries() != null) {
            for (final Binary b : e.getBinaries().values()) {
                if (b.getSource() == null) {
                    log.warn("No source on binary '{}' of entity '{}'", b.getName(), e.getId());
                    continue;
                }
                if (b.getSource().isInternal()) {
                    b.setUtcLastModified(oldVersion.getBinaries().get(b.getName()).getUtcLastModified());
                    b.setUtcCreated(oldVersion.getBinaries().get(b.getName()).getUtcCreated());
                }
                else {
                    createAndMutateBinary(e.getId(), b);
                }
            }
        }
        this.backendEntityService.update(e);
        if (autoExport) {
            exportService.export(e);
            log.debug("exported entity {} ", e.getId());
        }
    }

    @Override
    public Entity retrieve(String workspaceId, String id) throws IOException {
        return backendEntityService.retrieve(workspaceId, id);
    }

    @Override
    public void delete(String workspaceId, String id) throws IOException {
        final Entity e = backendEntityService.retrieve(workspaceId, id);
        for (Binary b : e.getBinaries().values()) {
            if (b.getPath() != null && !b.getPath().isEmpty()) {
                backendBlobstoreService.delete(b.getPath());
            }
        }
        this.backendEntityService.delete(id);
    }

    @Override
    public InputStream getContent(String workspaceId, String id, String name) throws IOException {
        final Entity e = backendEntityService.retrieve(workspaceId, id);
        final Binary b = e.getBinaries().get(name);
        return backendBlobstoreService.retrieve(b.getPath());
    }

    @Override
    public Entity retrieve(String workspaceId, String id, int i) throws IOException {
        final Entity e = this.backendEntityService.retrieve(workspaceId, id);
        if (i == e.getVersion()) {
            return e; // the current version
        }
        return this.backendVersionService.getOldVersion(id, i);
    }

    @Override
    public void createBinary(String workspaceId, String entityId, String name, String contentType,
            InputStream inputStream)
            throws IOException {
        final Entity e = backendEntityService.retrieve(workspaceId, entityId);
        this.backendVersionService.addOldVersion(e);
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e1) {
            throw new IOException(e1);
        }
        try (final SizeCalculatingDigestInputStream src = new SizeCalculatingDigestInputStream(inputStream, digest)) {
            final String path = backendBlobstoreService.create(src);
            final Binary b = new Binary();
            final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
            b.setUtcCreated(now);
            b.setUtcLastModified(now);
            b.setName(name);
            b.setMimetype(contentType);
            b.setChecksum(new BigInteger(1, digest.digest()).toString(16));
            b.setChecksumType("MD5");
            b.setSize(src.getCalculatedSize());
            b.setSource(new UrlSource(URI.create("http://localhost:8080/entity/" + entityId + "/binary/" + name
                    + "/content"), true));
            b.setPath(path);
            b.setUtcCreated(now);
            b.setUtcLastModified(now);
            if (e.getBinaries() == null) {
                e.setBinaries(new HashMap<>(1));
            }
            e.getBinaries().put(name, b);
            e.setVersionAndResetState(e.getVersion() + 1);
            e.setUtcLastModified(now);
            this.backendEntityService.update(e);
        }
        if (autoExport) {
            exportService.export(e);
            log.debug("exported entity {} ", e.getId());
        }
    }

    @Override
    public void patch(String workspaceId, final String id, final JsonNode node) throws IOException {
        final Entity e = this.backendEntityService.retrieve(workspaceId, id);
        final Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            final Map.Entry<String, JsonNode> field = fields.next();
            if (field.getValue().getNodeType() != JsonNodeType.STRING) {
                throw new IOException("The patch data is invalid");
            }
            switch (field.getKey()) {
            case "label":
                e.setLabel(field.getValue().asText());
                break;
            case "type":
                e.setType(field.getValue().asText());
                break;
            case "parentId":
                final String parentId = field.getValue().asText();
                if (parentId.equals(id)) {
                    throw new IOException("Can not add a parent relation to itself");
                }
                e.setParentId(parentId);
                break;
            case "state":
                final String state = field.getValue().asText();
                e.setState(state);
                break;
            default:
                throw new IOException("Unable to update field " + field.getKey());
            }
        }
        if (e.getLabel() == null || e.getLabel().isEmpty()) {
            e.setLabel("Unnamed Entity");
        }
        update(workspaceId, e);
    }

    @Override
    public void createRelation(String workspaceId, String id, String predicate, String object) throws IOException {
        if (object.startsWith("<" + LarchConstants.NAMESPACE_LARCH)) {
            // the object is an internal entity
            final String objId = object.substring(1 + LarchConstants.NAMESPACE_LARCH.length(), object.length() - 1);
            if (!this.backendEntityService.exists(objId)) {
                throw new NotFoundException("The entity " + object
                        + " referenced in the object of the relation does not exist in the repository");
            }
        }
        final Entity oldVersion = this.backendEntityService.retrieve(workspaceId, id);
        this.backendVersionService.addOldVersion(oldVersion);
        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        final Entity newVersion = oldVersion;
        newVersion.setUtcLastModified(now);
        newVersion.setVersionAndResetState(oldVersion.getVersion() + 1);
        if (newVersion.getRelations() == null) {
            newVersion.setRelations(new HashMap<>());
        }
        if (newVersion.getRelations().get(predicate) == null) {
            newVersion.getRelations().put(predicate, new ArrayList<>(1));
        }
        newVersion.getRelations().get(predicate).add(object);
        this.backendEntityService.update(newVersion);
    }

    @Override
    public void deleteBinary(String workspaceId, String entityId, String name) throws IOException {
        final Entity e = this.backendEntityService.retrieve(workspaceId, entityId);
        if (e.getBinaries().get(name) == null) {
            throw new NotFoundException("Binary " + name + " does not exist on entity " + entityId);
        }
        e.getBinaries().remove(name);
        this.update(workspaceId, e);
    }

    @Override
    public InputStream retrieveBinary(String path) throws IOException {
        return backendBlobstoreService.retrieve(path);
    }

    @Override
    public void deleteMetadata(String workspaceId, String entityId, String mdName) throws IOException {
        final Entity e = this.backendEntityService.retrieve(workspaceId, entityId);
        if (e.getMetadata().get(mdName) == null) {
            throw new NotFoundException("Meta data " + mdName + " does not exist on entity " + entityId);
        }
        e.getMetadata().remove(mdName);
        this.update(workspaceId, e);
    }

    @Override
    public void deleteBinaryMetadata(String workspaceId, String entityId, String binaryName, String mdName)
            throws IOException {
        final Entity e = this.backendEntityService.retrieve(workspaceId, entityId);
        if (e.getBinaries() == null || !e.getBinaries().containsKey(binaryName)) {
            throw new NotFoundException("The binary " + binaryName + " does not exist in the entity " + entityId);
        }
        final Binary bin = e.getBinaries().get(binaryName);
        if (bin.getMetadata() == null || !bin.getMetadata().containsKey(mdName)) {
            throw new NotFoundException("Meta data " + mdName + " does not exist on binary " + binaryName
                    + " of entity " + entityId);
        }
        bin.getMetadata().remove(mdName);
        this.update(workspaceId, e);
    }

    @Override
    public void createIdentifier(String workspaceId, String entityId, String type, String value) throws IOException {
        if (!this.backendEntityService.exists(entityId)) {
            throw new NotFoundException("The entity-id " + entityId + " does not exist in the repository");
        }
        if (StringUtils.isBlank(type) || StringUtils.isBlank(value)) {
            throw new InvalidParameterException("empty type or value given");
        }
        try {
            AlternativeIdentifier.IdentifierType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new InvalidParameterException("wrong type given");
        }
        final Entity oldVersion = this.backendEntityService.retrieve(workspaceId, entityId);
        this.backendVersionService.addOldVersion(oldVersion);
        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        final Entity newVersion = oldVersion;
        newVersion.setUtcLastModified(now);
        newVersion.setVersionAndResetState(oldVersion.getVersion() + 1);
        newVersion.getAlternativeIdentifiers().add(new AlternativeIdentifier(type, value));

        this.backendEntityService.update(newVersion);
    }

    @Override
    public void deleteIdentifier(String workspaceId, String entityId, String type, String value) throws IOException {
        if (!this.backendEntityService.exists(entityId)) {
            throw new NotFoundException("The entity-id " + entityId + " does not exist in the repository");
        }
        if (StringUtils.isBlank(type) || StringUtils.isBlank(value)) {
            throw new InvalidParameterException("empty type or value given");
        }
        try {
            AlternativeIdentifier.IdentifierType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new InvalidParameterException("wrong type given");
        }
        final Entity oldVersion = this.backendEntityService.retrieve(workspaceId, entityId);
        this.backendVersionService.addOldVersion(oldVersion);
        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        final Entity newVersion = oldVersion;
        newVersion.setUtcLastModified(now);
        newVersion.setVersionAndResetState(oldVersion.getVersion() + 1);
        boolean found = false;
        for (AlternativeIdentifier alternativeIdentifier : newVersion.getAlternativeIdentifiers()) {
            if (alternativeIdentifier.getType().equals(type) && alternativeIdentifier.getValue().equals(value)) {
                found = true;
                newVersion.getAlternativeIdentifiers().remove(alternativeIdentifier);
                break;
            }
        }
        if (!found) {
            throw new NotFoundException("Identifier of type " + type + " with value " + value + " not found");
        }

        this.backendEntityService.update(newVersion);
    }

    @Override
    public String publish(String workspaceId, String id) throws IOException {
        final Entity e = this.backendEntityService.retrieve(workspaceId, id);
        // if (e.getState().equals(Entity.STATE_PUBLISHED)) {
        // throw new IOException("The entity with the id " + id + " is already published");
        // }
        e.setState(Entity.STATE_PUBLISHED);
        this.backendEntityService.update(e);
        return this.backendPublishService.publish(e);
    }

    @Override
    public List<AuditRecord> retrieveAuditRecords(String workspaceId, String entityId, int offset, int count)
            throws IOException {
        return backendAuditService.retrieve(entityId, offset, count);
    }

    @Override
    public void createAuditRecord(AuditRecord auditRecord) throws IOException {
        backendAuditService.create(auditRecord);
    }

    @Override
    public SearchResult scanEntities(int offset) throws IOException {
        return backendEntityService.scanIndex(offset);
    }

    @Override
    public SearchResult scanEntities(int offset, int numRecords) throws IOException {
        return backendEntityService.scanIndex(offset, numRecords);
    }

    @Override
    public SearchResult searchEntities(Map<EntitiesSearchField, String[]> searchFields) throws IOException {
        return backendEntityService.searchEntities(searchFields);
    }

    @Override
    public Entities getOldVersions(String workspaceId, String id) throws IOException {
        return backendVersionService.getOldVersions(id);
    }

    @Override
    public String createWorkspace(Workspace workspace) throws IOException {
        return this.backendWorkspaceService.createWorkspace(workspace);
    }

    @Override
    public Workspace retrieveWorkspace(String id) throws IOException {
        return this.backendWorkspaceService.retrieveWorkspace(id);
    }

    @Override
    public List<Workspace> scanWorkspaces(String owner, int offset, int numRecords) throws IOException {
        return backendWorkspaceService.scanIndex(owner, offset, numRecords);
    }

    @Override
    public void updateWorkspace(Workspace workspace) throws IOException {
        this.backendWorkspaceService.updateWorkspace(workspace);
    }

    @Override
    public void patchWorkspace(Workspace workspace) throws IOException {
        this.backendWorkspaceService.patchWorkSpace(workspace);
    }

    @Override
    public SearchResult scanWorkspace(String workspaceId, int offset, int numRecords) throws IOException {
        return backendEntityService.scanWorkspace(workspaceId, offset, numRecords);
    }
}
