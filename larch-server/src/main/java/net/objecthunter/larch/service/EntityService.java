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

package net.objecthunter.larch.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import net.objecthunter.larch.model.AuditRecord;
import net.objecthunter.larch.model.AuditRecords;
import net.objecthunter.larch.model.Entities;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityType;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Service definition for CRUD operations on {@link net.objecthunter.larch.model.Entity} objects
 */
public interface EntityService {

    String create(Entity e) throws IOException;

    void update(Entity e) throws IOException;

    Entity retrieve(String id) throws IOException;

    void delete(String id) throws IOException;

    InputStream getContent(String id, String name) throws IOException;

    Entity retrieve(String id, int i) throws IOException;

    void createBinary(String entityId, String name, String contentType, InputStream inputStream)
            throws IOException;

    void patch(String id, JsonNode node) throws IOException;

    void createRelation(String id, String predicate, String object) throws IOException;

    void deleteBinary(String entityId, String name) throws IOException;

    InputStream retrieveBinary(String path) throws IOException;

    void deleteMetadata(String entityId, String mdName) throws IOException;

    void deleteBinaryMetadata(String entityId, String binaryName, String mdName)
            throws IOException;

    void createIdentifier(String entityId, String type, String value) throws IOException;

    void deleteIdentifier(String entityId, String type, String value) throws IOException;

    void submit(String id) throws IOException;

    void publish(String id) throws IOException;

    void withdraw(String id) throws IOException;

    AuditRecords retrieveAuditRecords(String entityId, int offset, int count) throws IOException;

    void createAuditRecord(AuditRecord auditRecord) throws IOException;

    /**
     * Retrieve a {@link net.objecthunter.larch.model.SearchResult} containing all
     * {@link net.objecthunter.larch.model .Entity}s from the index from a given offset with the default number of
     * {@link net.objecthunter.larch.model.Entity}s returned
     * 
     * @param offset the offset from which to return {@link net.objecthunter.larch.model.Entity}s from
     * @return a list of {@link net.objecthunter.larch.model.Entity}s available in the repository
     */
    SearchResult scanEntities(EntityType type, int offset) throws IOException;

    /**
     * Retrieve a {@link net.objecthunter.larch.model.SearchResult} containing all
     * {@link net.objecthunter.larch.model .Entity}s from the index from a given offset with a given maximum number of
     * {@link net.objecthunter.larch.model.Entity}s returned
     * 
     * @param offset the offset from which to return {@link net.objecthunter.larch.model.Entity}s from
     * @param numRecords the number of {@link net.objecthunter.larch.model.Entity}s to return
     * @return a list of {@link net.objecthunter.larch.model.Entity}s available in the repository
     */
    SearchResult scanEntities(EntityType type, int offset, int numRecords) throws IOException;

    SearchResult scanChildEntities(String ancestorId, EntityType type, int offset) throws IOException;

    SearchResult scanChildEntities(String ancestorId, EntityType type, int offset, int numRecords) throws IOException;

    /**
     * Search {@link net.objecthunter.larch.model.Entity}s in the repository.
     * 
     * @param searchFields Map with key: EntitiesSearchField and value searchStrings as array.
     * @return A {@link net.objecthunter.larch.model.SearchResult} containig the search hits
     */
    SearchResult searchEntities(Map<EntitiesSearchField, String[]> searchFields) throws IOException;

    /**
     * Retrieve all old versions of an entity from the version storage
     * 
     * @param id the id of the entity to retrieve
     * @return the requested old versions of the entity as Entities-Object
     */
    Entities getOldVersions(String id) throws IOException;

}
