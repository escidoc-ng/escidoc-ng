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
import java.util.List;
import java.util.Map;

import net.objecthunter.larch.model.*;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Service definition for CRUD operations on {@link net.objecthunter.larch.model.Entity} objects
 */
public interface EntityService {

    String create(String workspaceId, Entity e) throws IOException;

    void update(String workspaceId, Entity e) throws IOException;

    Entity retrieve(String workspaceId, String id) throws IOException;

    void delete(String workspaceId, String id) throws IOException;

    InputStream getContent(String workspaceId, String id, String name) throws IOException;

    Entity retrieve(String workspaceId, String id, int i) throws IOException;

    void createBinary(String workspaceId, String entityId, String name, String contentType, InputStream inputStream)
            throws IOException;

    void patch(String workspaceId, String id, JsonNode node) throws IOException;

    void createRelation(String workspaceId, String id, String predicate, String object) throws IOException;

    void deleteBinary(String workspaceId, String entityId, String name) throws IOException;

    InputStream retrieveBinary(String path) throws IOException;

    void deleteMetadata(String workspaceId, String entityId, String mdName) throws IOException;

    void deleteBinaryMetadata(String workspaceId, String entityId, String binaryName, String mdName)
            throws IOException;

    void createIdentifier(String workspaceId, String entityId, String type, String value) throws IOException;

    void deleteIdentifier(String workspaceId, String entityId, String type, String value) throws IOException;

    String publish(String workspaceId, String id) throws IOException;

    List<AuditRecord> retrieveAuditRecords(String workspaceId, String entityId, int offset, int count)
            throws IOException;

    void createAuditRecord(AuditRecord auditRecord) throws IOException;

    /**
     * Retrieve a {@link net.objecthunter.larch.model.SearchResult} containing all
     * {@link net.objecthunter.larch.model .Entity}s from the index from a given offset with the default number of
     * {@link net.objecthunter.larch.model.Entity}s returned
     * 
     * @param offset the offset from which to return {@link net.objecthunter.larch.model.Entity}s from
     * @return a list of {@link net.objecthunter.larch.model.Entity}s available in the repository
     */
    SearchResult scanIndex(int offset) throws IOException;

    /**
     * Retrieve a {@link net.objecthunter.larch.model.SearchResult} containing all
     * {@link net.objecthunter.larch.model .Entity}s from the index from a given offset with a given maximum number of
     * {@link net.objecthunter.larch.model.Entity}s returned
     * 
     * @param offset the offset from which to return {@link net.objecthunter.larch.model.Entity}s from
     * @param numRecords the number of {@link net.objecthunter.larch.model.Entity}s to return
     * @return a list of {@link net.objecthunter.larch.model.Entity}s available in the repository
     */
    SearchResult scanIndex(int offset, int numRecords) throws IOException;

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
    Entities getOldVersions(String workspaceId, String id) throws IOException;

    String createWorkspace(Workspace workspace) throws IOException;

    Workspace retrieveWorkspace(String id) throws IOException;

    void updateWorkspace(Workspace workspace) throws IOException;

    void patchWorkspace(Workspace workspace) throws IOException;
}
