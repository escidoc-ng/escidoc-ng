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

import de.escidocng.model.Archive;
import de.escidocng.model.EntityHierarchy;
import de.escidocng.model.SearchResult;

public interface BackendArchiveIndexService {
    Archive retrieve(String id, int version) throws IOException;

    void saveOrUpdate(Archive a, EntityHierarchy hierarchy) throws IOException;

    void delete(String entityId, int version) throws IOException;

    boolean exists(String id, int version) throws IOException;

    /**
     * Search {@link de.escidocng.model.Archive}s in the repository.
     * 
     * @param query query as String.
     * @param offset the offset
     * @param maxRecords maxRecords to return
     * @return A {@link de.escidocng.model.SearchResult} containig the search hits
     */
    SearchResult searchArchives(String query, int offset, int maxRecords) throws IOException;

}
