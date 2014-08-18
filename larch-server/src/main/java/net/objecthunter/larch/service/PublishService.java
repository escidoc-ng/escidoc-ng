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
import java.util.Map;

import net.objecthunter.larch.model.Entities;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

/**
 * Service definition for CRUD operations on {@link net.objecthunter.larch.model.Entity} objects
 */
public interface PublishService {

    Entity retrieve(String id) throws IOException;

    Entities retrievePublishedEntities(String entityId) throws IOException;

    /**
     * Retrieve a {@link net.objecthunter.larch.model.SearchResult} containing all
     * {@link net.objecthunter.larch.model .Entity}s from the publish-index from a given offset with the default
     * number of {@link net.objecthunter.larch.model.Entity}s returned
     * 
     * @param offset the offset from which to return {@link net.objecthunter.larch.model.Entity}s from
     * @return a list of {@link net.objecthunter.larch.model.Entity}s available in the published repository
     */
    SearchResult scanEntities(int offset) throws IOException;

    /**
     * Retrieve a {@link net.objecthunter.larch.model.SearchResult} containing all
     * {@link net.objecthunter.larch.model .Entity}s from the publish index from a given offset with a given maximum
     * number of {@link net.objecthunter.larch.model.Entity}s returned
     * 
     * @param offset the offset from which to return {@link net.objecthunter.larch.model.Entity}s from
     * @param numRecords the number of {@link net.objecthunter.larch.model.Entity}s to return
     * @return a list of {@link net.objecthunter.larch.model.Entity}s available in the publish repository
     */
    SearchResult scanEntities(int offset, int numRecords) throws IOException;

    /**
     * Retrieve a {@link net.objecthunter.larch.model.SearchResult} containing all
     * {@link net.objecthunter.larch.model .Entity}s belonging to the given workspace from the publish-index from a
     * given offset with the default number of {@link net.objecthunter.larch.model.Entity}s returned
     * 
     * @param offset the offset from which to return {@link net.objecthunter.larch.model.Entity}s from
     * @return a list of {@link net.objecthunter.larch.model.Entity}s available in the published repository
     */
    SearchResult scanWorkspaceEntities(String workspaceId, int offset) throws IOException;

    /**
     * Retrieve a {@link net.objecthunter.larch.model.SearchResult} containing all
     * {@link net.objecthunter.larch.model .Entity}s belonging to the given workspace from the publish index from a
     * given offset with a given maximum number of {@link net.objecthunter.larch.model.Entity}s returned
     * 
     * @param offset the offset from which to return {@link net.objecthunter.larch.model.Entity}s from
     * @param numRecords the number of {@link net.objecthunter.larch.model.Entity}s to return
     * @return a list of {@link net.objecthunter.larch.model.Entity}s available in the publish repository
     */
    SearchResult scanWorkspaceEntities(String workspaceId, int offset, int numRecords) throws IOException;

    /**
     * Search {@link net.objecthunter.larch.model.Entity}s in the publish repository.
     * 
     * @param searchFields Map with key: EntitiesSearchField and value searchStrings as array.
     * @return A {@link net.objecthunter.larch.model.SearchResult} containig the search hits
     */
    SearchResult searchEntities(Map<EntitiesSearchField, String[]> searchFields) throws IOException;

}
