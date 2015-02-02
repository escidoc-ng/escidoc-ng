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
import java.util.List;

import de.escidocng.model.MetadataType;
import de.escidocng.model.MetadataValidationResult;

/**
 * Service definition for the schema service which allows to handle schemas
 */
public interface SchemaService {

    /**
     * Get the XML schema's url for a given {@link de.escidocng.model.MetadataType} name
     * 
     * @param type The name of the {@link de.escidocng.model.MetadataType}
     * @return A String contianing the schema's url
     * @throws IOException
     */
    String getSchemUrlForType(String type) throws IOException;

    /**
     * Retrieve the known Schema types from the repository
     * 
     * @return a list of {@link de.escidocng.model.MetadataType}s
     * @throws IOException
     */
    List<MetadataType> getSchemaTypes() throws IOException;

    /**
     * Create a new {@link de.escidocng.model.MetadataType} in the repository
     * 
     * @param type the {@link de.escidocng.model.MetadataType} to store
     * @return the id of the stored {@link de.escidocng.model.MetadataType}
     * @throws IOException
     */
    String createSchemaType(MetadataType type) throws IOException;

    /**
     * Delete a {@link de.escidocng.model.MetadataType} from the repository. <b>Implementaitions of this
     * method have to make sure that the {@link de.escidocng.model.MetadataType} is not used anymore by any
     * {@link de.escidocng.model.Entity} or {@link de.escidocng.model.Binary}</b>
     * 
     * @param name the name of the {@link de.escidocng.model.MetadataType} to delete
     * @throws IOException
     */
    void deleteMetadataType(String name) throws IOException;

    /**
     * Retrieve the validation result for a given {@link de.escidocng.model.Metadata} of an
     * {@link de.escidocng.model.Entity}
     * 
     * @param id The id of the {@link de.escidocng.model.Entity}
     * @param metadataName The name of the {@link de.escidocng.model.Metadata}
     * @return a {@link de.escidocng.model.MetadataValidationResult} containing the result of the validation
     * @throws IOException
     */
    MetadataValidationResult validate(String id, String metadataName) throws IOException;

    /**
     * Retrieve the validation result for a given {@link de.escidocng.model.Metadata} of an
     * {@link de.escidocng.model.Entity}
     *
     * @param id The id of the {@link de.escidocng.model.Entity}
     * @param binaryName the name of the binary
     * @param metadataName The name of the {@link de.escidocng.model.Metadata}
     * @return a {@link de.escidocng.model.MetadataValidationResult} containing the result of the validation
     * @throws IOException
     */
    MetadataValidationResult validate(String id, String binaryName, String metadataName) throws IOException;

}
