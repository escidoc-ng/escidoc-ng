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

package net.objecthunter.larch.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.exceptions.AlreadyExistsException;
import net.objecthunter.larch.exceptions.InvalidParameterException;
import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.helpers.SizeCalculatingDigestInputStream;
import net.objecthunter.larch.model.AlternativeIdentifier;
import net.objecthunter.larch.model.AuditRecord;
import net.objecthunter.larch.model.AuditRecords;
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entities;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.EntityHierarchy;
import net.objecthunter.larch.model.LarchConstants;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.Relation;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.model.source.UrlSource;
import net.objecthunter.larch.service.AuthorizationService;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.EntityValidatorService;
import net.objecthunter.larch.service.ExportService;
import net.objecthunter.larch.service.backend.BackendAuditService;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;
import net.objecthunter.larch.service.backend.BackendCredentialsService;
import net.objecthunter.larch.service.backend.BackendEntityService;
import net.objecthunter.larch.service.backend.BackendSchemaService;
import net.objecthunter.larch.service.backend.BackendVersionService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;
import net.objecthunter.larch.service.backend.elasticsearch.queryrestriction.QueryRestrictionFactory;
import net.objecthunter.larch.service.backend.elasticsearch.queryrestriction.RoleQueryRestriction;
import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

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
 * The default implementation of {@link net.objecthunter.larch.service.EntityService} responsible for performing CRUD
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
    private BackendCredentialsService backendCredentialsService;

    @Autowired
    private EntityValidatorService defaultEntityValidatorService;

    @Autowired
    private XMLSerializer serializer;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ExportService exportService;

    @Autowired
    private AuthorizationService defaultAuthorizationService;

    @Autowired
    private BackendSchemaService backendSchemaService;

    @Autowired
    private Environment env;

    private boolean autoExport;
    
    @PostConstruct
    public void init() {
        final String val = env.getProperty("escidocng.export.auto");
        autoExport = val == null ? false : Boolean.valueOf(val);
    }

    @Override
    public String create(Entity e) throws IOException {
        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        if (e.getId() == null || e.getId().isEmpty()) {
            e.setId(generateId());
        } else {
            if (this.backendEntityService.exists(e.getId())) {
                throw new AlreadyExistsException("Entity with id " + e.getId()
                        + " could not be created because it already exists in the index");
            }
        }
        if (e.getState() == null) {
            e.setState(EntityState.PENDING);
        }

        // validate
        defaultEntityValidatorService.validate(e);

        if (e.getMetadata() != null) {
            for (final Metadata md : e.getMetadata()) {
                if (md.getSource() == null) {
                    log.warn("No source on metadata '{}' of entity '{}'", md.getName(), e.getId());
                    continue;
                }
                createAndMutateMetadata(e.getId(), null, md);
            }
        }
        if (e.getLabel() == null || e.getLabel().isEmpty()) {
            e.setLabel("Unnamed entity");
        }
        if (e.getBinaries() != null) {
            for (final Binary b : e.getBinaries()) {
                createAndMutateBinary(e.getId(), b);
            }
        }
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
        if (b == null) {
            throw new InvalidParameterException("binary may not be null");
        }
        if (StringUtils.isBlank(b.getName())) {
            throw new InvalidParameterException("name of binary may not be null or empty");
        }
        if (StringUtils.isBlank(b.getMimetype())) {
            throw new InvalidParameterException("contentType of binary may not be null or empty");
        }
        if (b.getSource() == null) {
            throw new InvalidParameterException("source of binary may not be null or empty");
        }
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        try (final SizeCalculatingDigestInputStream src =
                new SizeCalculatingDigestInputStream(b.getSource().getInputStream(), digest)) {
            final String path = this.backendBlobstoreService.create(src);
            final String checksum = new BigInteger(1, digest.digest()).toString(16);
            b.setChecksum(checksum);
            b.setSize(src.getCalculatedSize());
            b.setChecksumType(digest.getAlgorithm());
            b.setPath(path);
            b.setSource(new UrlSource(URI.create("/entity/" + entityId + "/binary/" +
                    b.getName()
                    + "/content"), true));
            final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
            b.setUtcCreated(now);
            b.setUtcLastModified(now);
            if (b.getMetadata() != null) {
                for (final Metadata md : b.getMetadata()) {
                    if (md.getSource() == null) {
                        log.warn("No source on binary '{}' of entity '{}'", b.getName(), entityId);
                        continue;
                    }
                    createAndMutateMetadata(entityId, b.getName(), md);
                }
            }
        }
    }

    private void createAndMutateMetadata(String entityId, String binaryName, Metadata md) throws IOException {
        if (md == null) {
            throw new InvalidParameterException("metadata may not be null");
        }
        if (StringUtils.isBlank(md.getName())) {
            throw new InvalidParameterException("name of metadata may not be null or empty");
        }
        if (StringUtils.isBlank(md.getMimetype())) {
            throw new InvalidParameterException("contentType of metadata may not be null or empty");
        }
        if (StringUtils.isBlank(md.getType())) {
            throw new InvalidParameterException("type of metadata may not be null or empty");
        }
        if (md.getSource() == null) {
            throw new InvalidParameterException("source of metadata may not be null or empty");
        }
        backendSchemaService.getSchemUrlForType(md.getType());
        if (!md.getSource().isInternal()) {
            if (md.getSource().getInputStream() == null) {
                throw new InvalidParameterException("inputStream of metadata may not be null or empty");
            }
            final MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new IOException(e);
            }
            try (final SizeCalculatingDigestInputStream src =
                    new SizeCalculatingDigestInputStream(md.getSource().getInputStream(), digest)) {
                final String path = this.backendBlobstoreService.create(src);
                final String checksum = new BigInteger(1, digest.digest()).toString(16);
                md.setChecksum(checksum);
                md.setSize(src.getCalculatedSize());
                md.setChecksumType(digest.getAlgorithm());
                md.setPath(path);
                String uri = "/entity/" + entityId + "/metadata/" + md.getName() + "/content";
                if (binaryName != null) {
                    uri = "/entity/" + entityId + "/binary/" + binaryName + "/metadata/" + md.getName() + "/content";
                }
                md.setSource(new UrlSource(URI.create(uri), true));
            }
        }
        if (md.isIndexInline()) {
            // Write Metadata-XML as JSON in Entity
            try (final InputStream src = this.backendBlobstoreService.retrieve(md.getPath())) {
                JSON mdJson = serializer.readFromStream(src);
                md.setJsonData(mapper.readValue(mdJson.toString(), JsonNode.class));
            }
        } else {
            md.setJsonData(null);
        }
        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        md.setUtcCreated(now);
        md.setUtcLastModified(now);
    }

    private String generateId() throws IOException {
        String generated;
        do {
            generated = RandomStringUtils.randomAlphabetic(16);
        } while (backendEntityService.exists(generated));
        return generated;
    }

    @Override
    public void update(Entity e) throws IOException {
        final Entity oldVersion = retrieve(e.getId());
        // check
        if (EntityState.PUBLISHED.equals(oldVersion.getState()) ||
                EntityState.WITHDRAWN.equals(oldVersion.getState())) {
            throw new InvalidParameterException("Cannot update entity in state " + oldVersion.getState());
        }
        checkNonUpdateableFields(e, oldVersion);

        this.backendVersionService.addOldVersion(oldVersion);
        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        e.setVersion(oldVersion.getVersion() + 1);
        if (e.getMetadata() != null) {
            for (final Metadata md : e.getMetadata()) {
                if (md.getSource() == null) {
                    log.warn("No source on metadata '{}' of entity '{}'", md.getName(), e.getId());
                    continue;
                }
                if (md.getSource().isInternal() && oldVersion.hasMetadata(md.getName()) && 
                        md.isIndexInline() == oldVersion.getMetadata(md.getName()).isIndexInline()) {
                    Metadata oldMd = oldVersion.getMetadata(md.getName());
                    md.setJsonData(oldMd.getJsonData());
                    md.setUtcLastModified(oldMd.getUtcLastModified());
                    md.setUtcCreated(oldMd.getUtcCreated());
                }
                else {
                    createAndMutateMetadata(e.getId(), null, md);
                }
            }
        }
        e.setUtcCreated(oldVersion.getUtcCreated());
        e.setUtcLastModified(now);
        if (e.getLabel() == null || e.getLabel().isEmpty()) {
            e.setLabel("Unnamed entity");
        }
        if (e.getBinaries() != null) {
            for (final Binary b : e.getBinaries()) {
                if (b.getSource() == null) {
                    log.warn("No source on binary '{}' of entity '{}'", b.getName(), e.getId());
                    continue;
                }
                if (b.getSource().isInternal()) {
                    Binary oldBin = oldVersion.getBinary(b.getName());
                    if (oldBin != null) {
                        b.setUtcLastModified(oldBin.getUtcLastModified());
                        b.setUtcCreated(oldBin.getUtcCreated());
                    }
                    if (b.getMetadata() != null) {
                        for (final Metadata md : b.getMetadata()) {
                            if (md.getSource() == null) {
                                log.warn("No source on metadata '{}' of binary '{}'", md.getName(), b.getName());
                                continue;
                            }
                            Metadata oldMd = oldBin.getMetadata(md.getName());
                            if (oldMd != null && md.getSource().isInternal() &&
                                    md.isIndexInline() == oldMd.isIndexInline()) {
                                md.setJsonData(oldMd.getJsonData());
                                md.setUtcLastModified(oldMd.getUtcLastModified());
                                md.setUtcCreated(oldMd.getUtcCreated());
                            }
                            else {
                                createAndMutateMetadata(e.getId(), b.getName(), md);
                            }
                        }
                    }
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
    public Entity retrieve(String id) throws IOException {
        Entity e = backendEntityService.retrieve(id);
        e.setChildren(backendEntityService.fetchChildren(id));
        return e;
    }

    @Override
    public void delete(String id) throws IOException {
        if (!backendEntityService.exists(id)) {
            throw new NotFoundException("Entity with id " + id + " was not found");
        }
        // check if entity is published. If yes, throw error.
        if (isPublished(id)) {
            throw new InvalidParameterException("Entity with id " + id + " is already published");
        }

        // check if children are published. If yes, throw error.
        if (hasPublishedChildren(id)) {
            throw new InvalidParameterException("Entity with id " + id + " has published children.");
        }

        // delete
        deleteRecursively(id);
    }

    @Override
    public Entity retrieve(String id, int version) throws IOException {
        final Entity e = retrieve(id);
        if (version == e.getVersion()) {
            return e; // the current version
        }
        return this.backendVersionService.getOldVersion(id, version);
    }

    @Override
    public void createBinary(String entityId, Binary binary)
            throws IOException {
        final Entity e = retrieve(entityId);
        if (EntityState.PUBLISHED.equals(e.getState()) || EntityState.WITHDRAWN.equals(e.getState())) {
            throw new InvalidParameterException("Cannot update entity in state " + e.getState());
        }

        if (e.hasBinary(binary.getName())) {
            throw new AlreadyExistsException("binary with name " + binary.getName() + " already exists in entity with id " +
                    e.getId());
        }
        this.backendVersionService.addOldVersion(e);
        createAndMutateBinary(entityId, binary);

        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        if (e.getBinaries() == null) {
            e.setBinaries(new ArrayList<>(1));
        }
        e.getBinaries().add(binary);
        e.setVersion(e.getVersion() + 1);
        e.setUtcLastModified(now);
        this.backendEntityService.update(e);
        if (autoExport) {
            exportService.export(e);
            log.debug("exported entity {} ", e.getId());
        }
    }

    @Override
    public void createMetadata(String entityId, Metadata metadata)
            throws IOException {
        final Entity e = retrieve(entityId);
        if (EntityState.PUBLISHED.equals(e.getState()) || EntityState.WITHDRAWN.equals(e.getState())) {
            throw new InvalidParameterException("Cannot update entity in state " + e.getState());
        }

        if (e.hasMetadata(metadata.getName())) {
            throw new AlreadyExistsException("metadata with name " + metadata.getName() + " already exists in entity with id " +
                    e.getId());
        }
        this.backendVersionService.addOldVersion(e);
        createAndMutateMetadata(entityId, null, metadata);

        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        if (e.getMetadata() == null) {
            e.setMetadata(new ArrayList<>(1));
        }
        e.getMetadata().add(metadata);
        e.setVersion(e.getVersion() + 1);
        e.setUtcLastModified(now);
        this.backendEntityService.update(e);
        if (autoExport) {
            exportService.export(e);
            log.debug("exported entity {} ", e.getId());
        }
    }

    @Override
    public void createBinaryMetadata(String entityId, String binaryName, Metadata metadata)
            throws IOException {
        final Entity e = retrieve(entityId);
        if (EntityState.PUBLISHED.equals(e.getState()) || EntityState.WITHDRAWN.equals(e.getState())) {
            throw new InvalidParameterException("Cannot update entity in state " + e.getState());
        }

        if (!e.hasBinary(binaryName)) {
            throw new FileNotFoundException("The binary " + binaryName + " does not exist on the entity " + entityId);
        }
        final Binary bin = e.getBinary(binaryName);
        if (bin.getMetadata() == null) {
            bin.setMetadata(new ArrayList<>());
        }
        if (bin.hasMetadata(metadata.getName())) {
            throw new IOException("The metadata " + metadata.getName() + " already exists on the binary " + binaryName +
                    " of the entity " + entityId);
        }

        this.backendVersionService.addOldVersion(e);
        createAndMutateMetadata(entityId, binaryName, metadata);

        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        bin.getMetadata().add(metadata);
        e.setVersion(e.getVersion() + 1);
        e.setUtcLastModified(now);
        this.backendEntityService.update(e);

        if (autoExport) {
            exportService.export(e);
            log.debug("exported entity {} ", e.getId());
        }
    }

    @Override
    public void patch(final String id, final JsonNode node) throws IOException {
        final Entity e = retrieve(id);
        if (EntityState.PUBLISHED.equals(e.getState()) || EntityState.WITHDRAWN.equals(e.getState())) {
            throw new InvalidParameterException("Cannot update entity in state " + e.getState());
        }
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
            case "parentId":
                final String parentId = field.getValue().asText();
                if (parentId.equals(id)) {
                    throw new IOException("Can not add a parent relation to itself");
                }
                e.setParentId(parentId);
                break;
            case "state":
                final String state = field.getValue().asText();
                e.setState(EntityState.valueOf(state));
                break;
            default:
                throw new IOException("Unable to update field " + field.getKey());
            }
        }
        if (e.getLabel() == null || e.getLabel().isEmpty()) {
            e.setLabel("Unnamed Entity");
        }
        update(e);
    }

    @Override
    public void createRelation(String id, String predicate, String object) throws IOException {
        if (object.startsWith("<" + LarchConstants.NAMESPACE_LARCH)) {
            // the object is an internal entity
            final String objId = object.substring(1 + LarchConstants.NAMESPACE_LARCH.length(), object.length() - 1);
            if (!this.backendEntityService.exists(objId)) {
                throw new NotFoundException("The entity " + object
                        + " referenced in the object of the relation does not exist in the repository");
            }
        }
        final Entity oldVersion = retrieve(id);
        if (EntityState.PUBLISHED.equals(oldVersion.getState()) ||
                EntityState.WITHDRAWN.equals(oldVersion.getState())) {
            throw new InvalidParameterException("Cannot update entity in state " + oldVersion.getState());
        }
        this.backendVersionService.addOldVersion(oldVersion);
        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        final Entity newVersion = oldVersion;
        newVersion.setUtcLastModified(now);
        newVersion.setVersion(oldVersion.getVersion() + 1);
        if (newVersion.getRelations() == null) {
            newVersion.setRelations(new ArrayList<>());
        }
        if (newVersion.getRelation(predicate) == null) {
            newVersion.getRelations().add(new Relation(predicate, new ArrayList<String>()));
        } else if (newVersion.getRelation(predicate).getObjects() == null) {
            newVersion.getRelation(predicate).setObjects(new ArrayList<String>());
        }
        newVersion.getRelation(predicate).getObjects().add(object);
        this.backendEntityService.update(newVersion);
    }

    @Override
    public void deleteBinary(String entityId, String name) throws IOException {
        final Entity e = retrieve(entityId);
        if (EntityState.PUBLISHED.equals(e.getState()) || EntityState.WITHDRAWN.equals(e.getState())) {
            throw new InvalidParameterException("Cannot update entity in state " + e.getState());
        }
        if (e.getBinary(name) == null) {
            throw new NotFoundException("Binary " + name + " does not exist on entity " + entityId);
        }
        // delete metadata from filesystem
        if (e.getBinary(name).getMetadata() != null) {
            for (Metadata md : e.getBinary(name).getMetadata()) {
                this.backendBlobstoreService.delete(md.getPath());
            }
        }
        // delete binary from filesystem
        this.backendBlobstoreService.delete(e.getBinary(name).getPath());
        e.removeBinary(name);
        this.update(e);
    }

    @Override
    public InputStream retrieveBinary(String path) throws IOException {
        return backendBlobstoreService.retrieve(path);
    }

    @Override
    public InputStream retrieveMetadataContent(String path) throws IOException {
        return backendBlobstoreService.retrieve(path);
    }

    @Override
    public void deleteMetadata(String entityId, String mdName) throws IOException {
        final Entity e = retrieve(entityId);
        if (EntityState.PUBLISHED.equals(e.getState()) || EntityState.WITHDRAWN.equals(e.getState())) {
            throw new InvalidParameterException("Cannot update entity in state " + e.getState());
        }
        if (e.getMetadata(mdName) == null) {
            throw new NotFoundException("Meta data " + mdName + " does not exist on entity " + entityId);
        }
        this.backendBlobstoreService.delete(e.getMetadata(mdName).getPath());
        e.removeMetadata(mdName);
        this.update(e);
    }

    @Override
    public void deleteBinaryMetadata(String entityId, String binaryName, String mdName)
            throws IOException {
        final Entity e = retrieve(entityId);
        if (EntityState.PUBLISHED.equals(e.getState()) || EntityState.WITHDRAWN.equals(e.getState())) {
            throw new InvalidParameterException("Cannot update entity in state " + e.getState());
        }
        if (!e.hasBinary(binaryName)) {
            throw new NotFoundException("The binary " + binaryName + " does not exist in the entity " + entityId);
        }
        final Binary bin = e.getBinary(binaryName);
        if (!bin.hasMetadata(mdName)) {
            throw new NotFoundException("Meta data " + mdName + " does not exist on binary " + binaryName
                    + " of entity " + entityId);
        }
        this.backendBlobstoreService.delete(bin.getMetadata(mdName).getPath());
        bin.removeMetadata(mdName);
        this.update(e);
    }

    @Override
    public void createIdentifier(String entityId, String type, String value) throws IOException {
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
        final Entity oldVersion = retrieve(entityId);
        if (EntityState.PUBLISHED.equals(oldVersion.getState()) ||
                EntityState.WITHDRAWN.equals(oldVersion.getState())) {
            throw new InvalidParameterException("Cannot update entity in state " + oldVersion.getState());
        }
        this.backendVersionService.addOldVersion(oldVersion);
        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        final Entity newVersion = oldVersion;
        newVersion.setUtcLastModified(now);
        newVersion.setVersion(oldVersion.getVersion() + 1);
        newVersion.getAlternativeIdentifiers().add(new AlternativeIdentifier(type, value));

        this.backendEntityService.update(newVersion);
    }

    @Override
    public void deleteIdentifier(String entityId, String type, String value) throws IOException {
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
        final Entity oldVersion = retrieve(entityId);
        if (EntityState.PUBLISHED.equals(oldVersion.getState()) ||
                EntityState.WITHDRAWN.equals(oldVersion.getState())) {
            throw new InvalidParameterException("Cannot update entity in state " + oldVersion.getState());
        }
        this.backendVersionService.addOldVersion(oldVersion);
        final String now = ZonedDateTime.now(ZoneOffset.UTC).toString();
        final Entity newVersion = oldVersion;
        newVersion.setUtcLastModified(now);
        newVersion.setVersion(oldVersion.getVersion() + 1);
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
    public void submit(String id) throws IOException {
        final Entity e = retrieve(id);
        if (EntityState.WITHDRAWN.equals(e.getState()) || EntityState.PUBLISHED.equals(e.getState())) {
            throw new InvalidParameterException("entity is in state " + e.getState() + " and may not get submitted");
        }
        if (!EntityState.SUBMITTED.equals(e.getState())) {
            e.setState(EntityState.SUBMITTED);
            this.backendEntityService.update(e);
        }
    }

    @Override
    public void publish(String id) throws IOException {
        final Entity e = retrieve(id);
        if (!EntityState.PUBLISHED.equals(e.getState())) {
            e.setState(EntityState.PUBLISHED);
            this.backendEntityService.update(e);
        }
    }

    @Override
    public void withdraw(String id) throws IOException {
        final Entity e = retrieve(id);
        if (!EntityState.WITHDRAWN.equals(e.getState())) {
            e.setState(EntityState.WITHDRAWN);
            this.backendEntityService.update(e);
        }
    }

    @Override
    public void pending(String id) throws IOException {
        final Entity e = retrieve(id);
        if (EntityState.WITHDRAWN.equals(e.getState()) || EntityState.PUBLISHED.equals(e.getState())) {
            throw new InvalidParameterException("entity is in state " + e.getState() + " and may not set to pending");
        }
        if (!EntityState.PENDING.equals(e.getState())) {
            e.setState(EntityState.PENDING);
            this.backendEntityService.update(e);
        }
    }

    @Override
    public AuditRecords retrieveAuditRecords(String entityId, int offset, int count)
            throws IOException {
        return backendAuditService.retrieve(entityId, offset, count);
    }

    @Override
    public void createAuditRecord(AuditRecord auditRecord) throws IOException {
        backendAuditService.create(auditRecord);
    }

    @Override
    public SearchResult searchEntities(String query, int offset)
            throws IOException {
        // add user restriction
        StringBuilder queryBuilder = new StringBuilder("");
        if (StringUtils.isNotBlank(query)) {
            queryBuilder.append("(").append(query).append(") AND ");
        }
        queryBuilder.append(getEntitesUserRestrictionQuery());
        return backendEntityService.searchEntities(queryBuilder.toString(), offset);
    }

    @Override
    public SearchResult searchEntities(String query, int offset, int maxRecords)
            throws IOException {
        // add user restriction
        StringBuilder queryBuilder = new StringBuilder("");
        if (StringUtils.isNotBlank(query)) {
            queryBuilder.append("(").append(query).append(") AND ");
        }
        queryBuilder.append(getEntitesUserRestrictionQuery());
        return backendEntityService.searchEntities(queryBuilder.toString(), offset, maxRecords);
    }

    @Override
    public Entities getOldVersions(String id) throws IOException {
        return backendVersionService.getOldVersions(id);
    }

    /**
     * Get Query that restricts a search to entities the user may see.
     * 
     * @return QueryBuilder with user-restriction query
     */
    private String getEntitesUserRestrictionQuery() throws IOException {
        User currentUser = defaultAuthorizationService.getCurrentUser();
        StringBuilder restrictionQueryBuilder = new StringBuilder("(");
        if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().isEmpty()) {
            // restrict to nothing
            restrictionQueryBuilder.append(EntitiesSearchField.STATE.getFieldName()).append(":NONEXISTING");
            restrictionQueryBuilder.append(")");
            return restrictionQueryBuilder.toString();
        } else {
            int counter = 0;
            for (Role role : currentUser.getRoles()) {
                if (counter > 0) {
                    restrictionQueryBuilder.append(" OR ");
                }
                RoleQueryRestriction roleQueryRestriction = QueryRestrictionFactory.getRoleQueryRestriction(role);
                restrictionQueryBuilder.append(roleQueryRestriction.getEntitiesRestrictionQuery());
                counter++;
            }
        }
        restrictionQueryBuilder.append(")");
        return restrictionQueryBuilder.toString();
    }

    /**
     * checks non updateable Fields.
     * 
     * @param newVersion newVersion of Entity
     * @param oldVersion oldVersion of Entity
     * @throws IOException
     */
    private void checkNonUpdateableFields(Entity newVersion, Entity oldVersion) throws IOException {
        if (!newVersion.getContentModelId().equals(oldVersion.getContentModelId())) {
            throw new InvalidParameterException("entity may not be moved to different content-model");
        }
        if (!oldVersion.getState().equals(newVersion.getState())) {
            throw new InvalidParameterException("entity may not be set to a different state with this method.");
        }
        if (StringUtils.isBlank(newVersion.getParentId()) && StringUtils.isBlank(oldVersion.getParentId())) {
            return;
        }
        if (StringUtils.isNotBlank(newVersion.getParentId()) && StringUtils.isNotBlank(oldVersion.getParentId())) {
            if (!newVersion.getParentId().equals(oldVersion.getParentId())) {
                EntityHierarchy oldHierarchy = this.backendEntityService.getHierarchy(oldVersion.getParentId());
                EntityHierarchy newHierarchy = this.backendEntityService.getHierarchy(newVersion.getParentId());
                if (!oldHierarchy.getLevel1Id().equals(newHierarchy.getLevel1Id())) {
                    throw new InvalidParameterException("entity may not be moved to different level1");
                }
                if (StringUtils.isBlank(oldHierarchy.getLevel2Id()) &&
                        StringUtils.isBlank(newHierarchy.getLevel2Id())) {
                    return;
                }
                if ((StringUtils.isBlank(oldHierarchy.getLevel2Id()) && StringUtils.isNotBlank(newHierarchy
                        .getLevel2Id())) ||
                        (StringUtils.isNotBlank(oldHierarchy.getLevel2Id()) && StringUtils.isBlank(newHierarchy
                                .getLevel2Id())) || !oldHierarchy.getLevel2Id().equals(newHierarchy.getLevel2Id())) {
                    throw new InvalidParameterException("entity may not be moved to different level2");
                }
            }
        } else {
            throw new InvalidParameterException("parentId may not get changed from/to null");
        }
    }

    /**
     * checks if the entity with the given id has a published version.
     * 
     * @param id
     * @return boolean true or false
     * @throws IOException
     */
    private boolean isPublished(String id) throws IOException {
        boolean isPublished = false;
        try {
            Entity e = retrieve(id);
            if (EntityState.PUBLISHED.equals(e.getState())) {
                isPublished = true;
            }
        } catch (NotFoundException e) {
        }
        return isPublished;
    }

    /**
     * checks if some child or childchild.. is published.
     * 
     * @param id
     * @return boolean true or false
     * @throws IOException
     */
    private boolean hasPublishedChildren(String id) throws IOException {
        final Entity e = retrieve(id);
        if (e.getChildren() == null) {
            return false;
        } else {
            for (String childId : e.getChildren()) {
                if (isPublished(childId)) {
                    return true;
                } else {
                    return hasPublishedChildren(childId);
                }
            }
        }
        return false;
    }

    /**
     * delete entity, all children, childchilds....
     * 
     * @param id
     * @throws IOException
     */
    private void deleteRecursively(String id) throws IOException {
        final Entity e = retrieve(id);
        if (e.getChildren() != null) {
            for (String childId : e.getChildren()) {
                deleteRecursively(childId);
            }
        }

        List<String> alreadyDeletedFiles = new ArrayList<String>();
        deleteFiles(e, alreadyDeletedFiles);

        // delete audit-records
        this.backendAuditService.deleteAll(id);

        // delete Versions
        Entities entities = this.backendVersionService.getOldVersions(id);
        if (entities != null) {
            for (Entity entity : entities.getEntities()) {
                deleteFiles(entity, alreadyDeletedFiles);
            }
        }
        this.backendVersionService.deleteOldVersions(id);

        // delete entity
        this.backendEntityService.delete(id);

        // delete rights having this entity as anchorId
        this.backendCredentialsService.deleteRights(id);

    }

    /**
     * Delete Files with backendBlobstoreService. remember deleted paths to not try deleting again.
     * 
     * @param e Emtity
     * @param alreadyDeletedFiles
     * @throws IOException
     */
    private void deleteFiles(Entity e, List<String> alreadyDeletedFiles) throws IOException {
        // delete binaries
        if (e.getBinaries() != null) {
            for (Binary b : e.getBinaries()) {
                if (b.getPath() != null && !b.getPath().isEmpty()) {
                    if (!alreadyDeletedFiles.contains(b.getPath())) {
                        try {
                            this.backendBlobstoreService.delete(b.getPath());
                        } catch (Exception ex) {
                            log.warn(ex.toString());
                        }
                        alreadyDeletedFiles.add(b.getPath());
                    }
                }
                if (b.getMetadata() != null) {
                    for (Metadata md : b.getMetadata()) {
                        if (!alreadyDeletedFiles.contains(md.getPath())) {
                            try {
                                this.backendBlobstoreService.delete(md.getPath());
                            } catch (Exception ex) {
                                log.warn(ex.toString());
                            }
                            alreadyDeletedFiles.add(md.getPath());
                        }
                    }
                }
            }
        }

        // delete metadata from filesystem
        if (e.getMetadata() != null) {
            for (Metadata md : e.getMetadata()) {
                if (!alreadyDeletedFiles.contains(md.getPath())) {
                    try {
                        this.backendBlobstoreService.delete(md.getPath());
                    } catch (Exception ex) {
                        log.warn(ex.toString());
                    }
                    alreadyDeletedFiles.add(md.getPath());
                }
            }
        }
    }
    
}
