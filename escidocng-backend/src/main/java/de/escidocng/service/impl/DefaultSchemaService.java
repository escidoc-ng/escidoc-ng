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

package de.escidocng.service.impl;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import de.escidocng.model.MetadataType;
import de.escidocng.model.MetadataValidationResult;
import de.escidocng.service.SchemaService;
import de.escidocng.service.backend.BackendSchemaService;

/**
 * Default implementation of a {@link de.escidocng.service.ExportService} which is able to export JSON data
 * to the File system
 */
public class DefaultSchemaService implements SchemaService {

    @Autowired
    private BackendSchemaService schemaService;

    @Override
    public String getSchemUrlForType(String type) throws IOException {
        return schemaService.getSchemUrlForType(type);
    }

    @Override
    public List<MetadataType> getSchemaTypes() throws IOException {
        return schemaService.getSchemaTypes();
    }

    @Override
    public String createSchemaType(MetadataType type) throws IOException {
        return schemaService.createSchemaType(type);
    }

    @Override
    public void deleteMetadataType(String name) throws IOException {
        schemaService.deleteSchemaType(name);
    }

    @Override
    public MetadataValidationResult validate(String id, String metadataName) throws IOException {
        return schemaService.validate(id, metadataName);
    }

    @Override
    public MetadataValidationResult validate(String id, String binaryName, String metadataName) throws IOException {
        return schemaService.validate(id, binaryName, metadataName);
    }

}
