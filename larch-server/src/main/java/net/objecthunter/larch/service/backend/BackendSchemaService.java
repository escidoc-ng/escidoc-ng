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
import java.util.List;

import net.objecthunter.larch.model.MetadataType;
import net.objecthunter.larch.model.MetadataValidationResult;

/**
 * Service definition for getting the associated schemaUrl for a given meta data type
 */
public interface BackendSchemaService {

    /**
     * Get the XML schema's url for a given {@link net.objecthunter.larch.model.MetadataType} name
     * 
     * @param type The name of the {@link net.objecthunter.larch.model.MetadataType}
     * @return A String contianing the schema's url
     * @throws IOException
     */
    String getSchemUrlForType(String type) throws IOException;

    /**
     * Retrieve the known Schema types from the repository
     * 
     * @return a list of {@link net.objecthunter.larch.model.MetadataType}s
     * @throws IOException
     */
    List<MetadataType> getSchemaTypes() throws IOException;

    /**
     * Create a new {@link net.objecthunter.larch.model.MetadataType} in the repository
     * 
     * @param type the {@link net.objecthunter.larch.model.MetadataType} to store
     * @return the id of the stored {@link net.objecthunter.larch.model.MetadataType}
     * @throws IOException
     */
    String createSchemaType(MetadataType type) throws IOException;

    /**
     * Delete a {@link net.objecthunter.larch.model.MetadataType} from the repository. <b>Implementaitions of this
     * method have to make sure that the {@link net.objecthunter.larch.model.MetadataType} is not used anymore by any
     * {@link net.objecthunter.larch.model.Entity} or {@link net.objecthunter.larch.model.Binary}</b>
     * 
     * @param name the name of the {@link net.objecthunter.larch.model.MetadataType} to delete
     * @throws IOException
     */
    void deleteMetadataType(String name) throws IOException;

    /**
     * Retrieve the validation result for a given {@link net.objecthunter.larch.model.Metadata} of an
     * {@link net.objecthunter.larch.model.Entity}
     * 
     * @param id The id of the {@link net.objecthunter.larch.model.Entity}
     * @param metadataName The name of the {@link net.objecthunter.larch.model.Metadata}
     * @return a {@link net.objecthunter.larch.model.MetadataValidationResult} containing the result of the validation
     * @throws IOException
     */
    net.objecthunter.larch.model.MetadataValidationResult validate(String id, String metadataName) throws IOException;

    MetadataValidationResult validate(String id, String binaryName, String metadataName) throws IOException;

}
