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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import de.escidocng.exceptions.InvalidParameterException;
import de.escidocng.model.ContentModel;
import de.escidocng.model.Entity;
import de.escidocng.service.EntityValidatorService;
import de.escidocng.service.backend.BackendContentModelService;
import de.escidocng.service.backend.BackendEntityService;

/**
 * Service that validates entity against ContentModel.
 * 
 * @author mih
 */
public class DefaultEntityValidatorService implements EntityValidatorService {

    @Autowired
    private BackendEntityService backendEntityService;

    @Autowired
    private BackendContentModelService backendContentModelService;

    @Override
    public void validate(Entity entity) throws IOException {
        validateHierarchy(entity);
    }

    /**
     * Validate if contentModelId is != null and if hierarchy is correct.
     * Retrieve ContentModel and read allowedParentContentModels to validate.
     * 
     * @param entity
     * @throws IOException
     */
    private void validateHierarchy(Entity entity) throws IOException {
        if (StringUtils.isBlank(entity.getContentModelId())) {
            throw new InvalidParameterException("contentModelId may not be null");
        }
        ContentModel contentModel = backendContentModelService.retrieve(entity.getContentModelId());
        List<String> allowedParentContentModels = contentModel.getAllowedParentContentModels();

        if (allowedParentContentModels != null && !allowedParentContentModels.isEmpty() &&
                StringUtils.isNotBlank(entity.getParentId())) {
            Entity parent = backendEntityService.retrieve(entity.getParentId());
            if (allowedParentContentModels.contains(parent.getContentModelId())) {
                return;
            }
        } else if ((allowedParentContentModels == null || allowedParentContentModels.isEmpty()) &&
                StringUtils.isBlank(entity.getParentId())) {
            return;
        }
        throw new InvalidParameterException("invalid entity");
    }
}
