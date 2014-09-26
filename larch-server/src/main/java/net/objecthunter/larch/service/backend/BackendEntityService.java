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
 * WITHOUT WARRANTIES OR CONDITIONS OF ROLE_ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package net.objecthunter.larch.service.backend;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityType;
import net.objecthunter.larch.model.EntityHierarchy;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.model.state.IndexState;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

/**
 * Service definition for CRUD operations on index operations
 */
public interface BackendEntityService {

    String create(Entity e) throws IOException;

    void update(Entity e) throws IOException;

    Entity retrieve(String entityId) throws IOException;

    void delete(String id) throws IOException;

    IndexState status() throws IOException;

    boolean exists(String id) throws IOException;

    List<String> fetchChildren(String id) throws IOException;

    /**
     * Search {@link net.objecthunter.larch.model.Entity}s in the repository.
     * 
     * @param searchFields Map with key: EntitiesSearchField and value searchStrings as array.
     * @param offset the offset
     * @return A {@link net.objecthunter.larch.model.SearchResult} containig the search hits
     */
    SearchResult searchEntities(Map<EntitiesSearchField, String[]> searchFields, int offset) throws IOException;

    /**
     * Search {@link net.objecthunter.larch.model.Entity}s in the repository.
     * 
     * @param searchFields Map with key: EntitiesSearchField and value searchStrings as array.
     * @param offset the offset
     * @param maxRecords maxRecords to return
     * @return A {@link net.objecthunter.larch.model.SearchResult} containig the search hits
     */
    SearchResult searchEntities(Map<EntitiesSearchField, String[]> searchFields, int offset, int maxRecords) throws IOException;

    EntityHierarchy getHierarchy(String entityId) throws IOException;

}
