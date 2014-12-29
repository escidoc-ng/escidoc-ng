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

package net.objecthunter.larch.service;

import net.objecthunter.larch.model.Archive;
import net.objecthunter.larch.model.SearchResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Archive interface for storing AIPs in the repository
 */
public interface ArchiveService {

    /**
     * Archive a specific version of an entity
     * @param entityId the id of the entity to archive
     * @param version the entity version to archive
     * @throws IOException
     */
    void archive(String entityId, int version) throws IOException;

    /**
     * Check if a specific version of an entity is archived
     * @param entityId the is of the entity to check
     * @param version the version of the entity to check
     * @return true if the version is already archived, otherwise false
     * @throws IOException
     */
    boolean isArchived(String entityId, int version) throws IOException;

    /**
     * Fetch the archive (AIP) from the storage layer
     * @param entityId the entity's id
     * @param version the entity's version
     * @return an InputStream containing the AIP's binary data
     * @throws IOException
     */
    InputStream retrieveData(String entityId, int version) throws IOException;

    /**
     * Delete an archive (AIP) in the archive storage layer
     * @param entityId the entity's id
     * @param version the entity's version
     * @throws IOException
     */
    void delete(String entityId, int version) throws IOException;

    /**
     * Retrieve the size of an archive (AIP) from the storage layer
     * @param entityId the entity's id
     * @param version the entity's version
     * @return the size of the stored AIP
     * @throws IOException
     */
    long sizeof(String entityId, int version) throws IOException;

    /**
     * Retrieve the archive's metadata from the storage layer
     * @param entityId the entity's id
     * @param version the entity's version
     * @return A object containing the archive's metadata
     * @throws IOException
     */
    Archive retrieve(String entityId, int version) throws IOException;

    /**
     * Search the storage layer for a given archive
     * @param query the query to use for the search
     * @param offset the offset for the search
     * @param count the number of results the search should maximally return
     * @return a SearchResult object containing the search results
     * @throws IOException
     */
    SearchResult searchArchives(String query, int offset, int count) throws IOException;
}
