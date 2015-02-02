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

package de.escidocng.service;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;

import de.escidocng.model.AuditRecord;
import de.escidocng.model.AuditRecords;
import de.escidocng.model.Binary;
import de.escidocng.model.Entities;
import de.escidocng.model.Entity;
import de.escidocng.model.Metadata;
import de.escidocng.model.SearchResult;

/**
 * Service definition for CRUD operations on {@link de.escidocng.model.Entity} objects
 */
public interface EntityService {

    /**
     * Create a new entity
     * @param e the entity to create
     * @return the entity's identifier as a String
     * @throws IOException
     */
    String create(Entity e) throws IOException;

    /**
     * Update an existing entity
     * @param e the entity to update
     * @throws IOException
     */
    void update(Entity e) throws IOException;

    /**
     * Fetch an entity from the repository
     * @param id the entity's id
     * @return the requested entity object
     * @throws IOException
     */
    Entity retrieve(String id) throws IOException;

    /**
     * Delete an existing entity
     * @param id the entity's id
     * @throws IOException
     */
    void delete(String id) throws IOException;

    /**
     * Patch an existing entity. I.e. Only update fields that are set in the supplied Entity data.
     * Service method to implement the HTTP Patch method. See http://tools.ietf.org/html/rfc5789
     * @param id the is of the entity
     * @param node the JsonNode containing the information to be patched
     * @throws IOException
     */
    void patch(String id, JsonNode node) throws IOException;

    /**
     * Retrieve a specific version of an entity
     * @param id the entity's id
     * @param version the entity's version
     * @return
     * @throws IOException
     */
    Entity retrieve(String id, int version) throws IOException;

    /**
     * Create a new binary in the repository
     * @param entityId the id of the binary's parent entity
     * @param binary the binary object containing all the relevant information
     * @throws IOException
     */
    void createBinary(String entityId, Binary binary)
            throws IOException;

    /**
     * Delete a binary in the repository
     * @param entityId the entity's id
     * @param name the name of the binary
     * @throws IOException
     */
    void deleteBinary(String entityId, String name) throws IOException;

    /**
     * Retrieve a binary from the repository
     * @param path the binary's path in the storage layer
     * @return
     * @throws IOException
     */
    InputStream retrieveBinary(String path) throws IOException;

    /**
     * Create a metadata object for an entity in the repository
     * @param entityId the id of the metadata's parent entity
     * @param metadata the metadata object containing the data to be stored
     * @throws IOException
     */
    void createMetadata(String entityId, Metadata metadata)
            throws IOException;

    /**
     * Create a metadata object for a binary in the repository
     * @param entityId
     * @param binaryName
     * @param metadata
     * @throws IOException
     */
    void createBinaryMetadata(String entityId, String binaryName, Metadata metadata)
            throws IOException;

    /**
     * Retrieve the raw stream containing metadata from the storage layer
     * @param path the path of the metadata object
     * @return an InputStream with the metadata's content
     * @throws IOException
     */
    InputStream retrieveMetadataContent(String path) throws IOException;

    /**
     * Delete the metadata of an entity in the repository
     * @param entityId the entity's id
     * @param mdName the metadata name
     * @throws IOException
     */
    void deleteMetadata(String entityId, String mdName) throws IOException;

    /**
     * Delete the metadata of a binary in the repository
     * @param entityId the entity's id
     * @param binaryName the binary's name
     * @param mdName the metadata's name
     * @throws IOException
     */
    void deleteBinaryMetadata(String entityId, String binaryName, String mdName)
            throws IOException;

    /**
     * Set the state of an entity to SUBMITTED
     * @param id the id of the entity
     * @throws IOException
     */
    void submit(String id) throws IOException;

    /**
     * Set the entity's state to PUBLISHED
     * @param id the id of the entity
     * @throws IOException
     */
    void publish(String id) throws IOException;

    /**
     * Set the entity's state to WITHDRAWN
     * @param id the entity's id
     * @throws IOException
     */
    void withdraw(String id) throws IOException;

    /**
     * Set the entity's state to PENDING
     * @param id
     * @throws IOException
     */
    void pending(String id) throws IOException;

    /**
     * Retrieve audit records for an entity
     * @param entityId the entity's id
     * @param offset the offset for the result
     * @param count the result count
     * @return a object containing all the audit records for the given constraints
     * @throws IOException
     */
    AuditRecords retrieveAuditRecords(String entityId, int offset, int count) throws IOException;

    /**
     * Create an audit record in the repository
     * @param auditRecord the audit record to store
     * @throws IOException
     */
    void createAuditRecord(AuditRecord auditRecord) throws IOException;

    /**
     * Create a semantic triple in the repository
     * @param id the id that will be used as the triple's subject
     * @param predicate the triple's predicate
     * @param object the triple's object
     * @throws IOException
     */
    void createRelation(String id, String predicate, String object) throws IOException;

    /**
     * Create an Identifier in the repository
     * @param entityId the entity's id
     * @param type the type of the identifier
     * @param value the identifers's value
     * @throws IOException
     */
    void createIdentifier(String entityId, String type, String value) throws IOException;

    /**
     * Delete an Identifier from the repository
     * @param entityId the entity's id
     * @param type the type of the identifier
     * @param value the identifier's value
     * @throws IOException
     */
    void deleteIdentifier(String entityId, String type, String value) throws IOException;

    /**
     * Search {@link de.escidocng.model.Entity}s in the repository.
     * 
     * @param searchFields Map with key: EntitiesSearchField and value searchStrings as array.
     * @param offset the offset
     * @return A {@link de.escidocng.model.SearchResult} containig the search hits
     */
    SearchResult searchEntities(String query, int offset) throws IOException;

    /**
     * Search {@link de.escidocng.model.Entity}s in the repository.
     * 
     * @param query query as String.
     * @param offset the offset
     * @param maxRecords maxRecords to return
     * @return A {@link de.escidocng.model.SearchResult} containig the search hits
     */
    SearchResult searchEntities(String query, int offset, int maxRecords) throws IOException;

    /**
     * Retrieve all old versions of an entity from the version storage
     * 
     * @param id the id of the entity to retrieve
     * @return the requested old versions of the entity as Entities-Object
     */
    Entities getOldVersions(String id) throws IOException;

}
