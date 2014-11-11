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

package net.objecthunter.larch.service.backend;

import java.io.IOException;
import java.io.InputStream;

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.state.BlobstoreState;

/**
 * Service definition for CRUD operations on Blobs
 */
public interface BackendBlobstoreService {

    /**
     * Store the contents of an {@link java.io.InputStream} in the blob store
     * 
     * @param src The {@link java.io.InputStream} containing the data to be stored
     * @return The path to the stored data
     * @throws IOException
     */
    String create(InputStream src) throws IOException;

    /**
     * Retrieve a blob as an {@link java.io.InputStream} form the blob store
     * 
     * @param path The path to the requested blob
     * @return An {@link java.io.InputStream} of the blob's content
     * @throws IOException
     */
    InputStream retrieve(String path) throws IOException;

    /**
     * Delete a blob in the blob store
     * 
     * @param path the path of the blob to delete
     * @throws IOException
     */
    void delete(String path) throws IOException;

    /**
     * Update the contents of a blob in the blob store
     * 
     * @param path the path to the blob to update
     * @param src the {@link java.io.InputStream} containing the new content
     * @throws IOException
     */
    void update(String path, InputStream src) throws IOException;

    /**
     * Retrieve a {@link net.objecthunter.larch.model.state.BlobstoreState} containing information about the current
     * state of the blob store
     * 
     * @return the current state wrappend in a {@link net.objecthunter.larch.model.state.BlobstoreState} object
     * @throws IOException
     */
    BlobstoreState status() throws IOException;

    /**
     * Create a new blob for an old version of an {@link net.objecthunter.larch.model.Entity}. Old versions get
     * removed from the ElasticSearchIndex and the plain JSON is stored as blob in the blob store
     * 
     * @param oldVersion The {@link net.objecthunter.larch.model.Entity} representing the old version for storage
     * @return the path to the old version blob
     * @throws IOException
     */
    String createOldVersionBlob(Entity oldVersion) throws IOException;

    /**
     * Retrieve an old version of an {@link net.objecthunter.larch.model.Entity} stored as a blob in the store
     * 
     * @param path the path to the old version
     * @return An {@link java.io.InputStream} for the {@link net.objecthunter.larch.model.Entity}'s old version blob
     * @throws IOException
     */
    InputStream retrieveOldVersionBlob(String path) throws IOException;
}
