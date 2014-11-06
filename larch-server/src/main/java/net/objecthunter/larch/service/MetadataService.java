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

package net.objecthunter.larch.service;

import java.io.IOException;

import net.objecthunter.larch.model.SearchResult;

/**
 * Service definition for metadata search
 */
public interface MetadataService {

    /**
     * Search for metadata in the repository.
     * 
     * @param query Search Query.
     * @param offset the offset
     * @return A {@link net.objecthunter.larch.model.SearchResult} containig the search hits
     */
    SearchResult searchEntities(String query, int offset) throws IOException;

    /**
     * Search for metadata in the repository.
     * 
     * @param query query as String.
     * @param offset the offset
     * @param maxRecords maxRecords to return
     * @return A {@link net.objecthunter.larch.model.SearchResult} containig the search hits
     */
    SearchResult searchEntities(String query, int offset, int maxRecords) throws IOException;

}
