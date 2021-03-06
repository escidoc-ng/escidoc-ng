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

package de.escidocng.service.backend;

import java.io.IOException;
import java.util.List;

import de.escidocng.model.Entity;
import de.escidocng.model.EntityHierarchy;
import de.escidocng.model.SearchResult;
import de.escidocng.model.state.IndexState;

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
     * Search {@link de.escidocng.model.Entity}s in the repository.
     * 
     * @param query query as String.
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
     * Get Information about level1- and level2-id of the entity with the given entityId
     * @param entityId
     * @return EntityHierarchy EntityHierarchy
     * @throws IOException
     */
    EntityHierarchy getHierarchy(String entityId) throws IOException;

    /**
     * Get Information about level1- and level2-id of the given entity
     * @param entityId
     * @return EntityHierarchy EntityHierarchy
     * @throws IOException
     */
    EntityHierarchy getHierarchy(Entity entity) throws IOException;

}
