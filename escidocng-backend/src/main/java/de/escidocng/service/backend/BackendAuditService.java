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

import de.escidocng.model.AuditRecord;
import de.escidocng.model.AuditRecords;

/**
 * Service definition for interactions of {@link de.escidocng.model.AuditRecord} objects
 */
public interface BackendAuditService {

    /**
     * Retrieve a list of {@link de.escidocng.helpers.AuditRecord} form the repository for a given Entity
     * 
     * @param entityId The id of the {@link de.escidocng.model.Entity}
     * @param offset The offset from which to get {@link de.escidocng.helpers.AuditRecord}s from
     * @param numRecords The number of AuditRecords to return
     * @return A list of AuditRecords for the given Entity
     * @throws IOException
     */
    AuditRecords retrieve(String entityId, int offset, int numRecords) throws IOException;

    /**
     * Create a new {@link de.escidocng.model.AuditRecord} and store it in the repository
     * 
     * @param rec The AuditRecord to store
     * @return The id of the stored AuditRecord
     * @throws IOException
     */
    String create(AuditRecord rec) throws IOException;

    /**
     * Delete all AuditRecords of the Entity with the given entityId
     * 
     * @param entityId The entityId
     * @throws IOException
     */
    void deleteAll(String entityId) throws IOException;
}
