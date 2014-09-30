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

package net.objecthunter.larch.service.impl;

import java.io.IOException;

import net.objecthunter.larch.exceptions.AlreadyExistsException;
import net.objecthunter.larch.exceptions.InvalidParameterException;
import net.objecthunter.larch.model.ContentModel;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.service.ContentModelService;
import net.objecthunter.larch.service.backend.BackendContentModelService;
import net.objecthunter.larch.service.backend.BackendEntityService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The default implementation of {@link net.objecthunter.larch.service.ContentModelService} responsible for perofrming
 * CRUD operations of {@link net.objecthunter.larch.model.ContentModel} objects
 */
public class DefaultContentModelService implements ContentModelService {

    private static final Logger log = LoggerFactory.getLogger(DefaultContentModelService.class);

    @Autowired
    private BackendContentModelService backendContentModelService;

    @Autowired
    private BackendEntityService backendEntityService;

    @Override
    public String create(ContentModel c) throws IOException {
        if (c.getId() == null || c.getId().isEmpty()) {
            c.setId(generateId());
        } else {
            if (this.backendContentModelService.exists(c.getId())) {
                throw new AlreadyExistsException("ContentModel with id " + c.getId()
                        + " could not be created because it already exists in the index");
            }
        }
        if (c.getAllowedParentContentModels() == null || c.getAllowedParentContentModels().isEmpty()) {
            throw new InvalidParameterException("AllowedParentContentModels may not be empty");
        }
        if (c.getAllowedParentContentModels().contains("") ||
                c.getAllowedParentContentModels().contains(FixedContentModel.LEVEL1)) {
            throw new InvalidParameterException("AllowedParentContentModels may not contain level1 or empty string");
        }
        final String id = this.backendContentModelService.create(c);
        log.debug("finished creating ContentModel {}", id);

        return id;
    }

    private String generateId() throws IOException {
        String generated;
        do {
            generated = RandomStringUtils.randomAlphabetic(16);
        } while (backendContentModelService.exists(generated));
        return generated;
    }

    @Override
    public ContentModel retrieve(String id) throws IOException {
        ContentModel c = backendContentModelService.retrieve(id);
        return c;
    }

    @Override
    public void delete(String id) throws IOException {
        // check if content-model is used by any entity
        SearchResult searchResult =
                backendEntityService.searchEntities(EntitiesSearchField.CONTENT_MODEL + ":" + id, 0);
        if (searchResult.getHits() > 0) {
            throw new InvalidParameterException("ContentModel " + id + " is used by entities");
        }
        // delete
        backendContentModelService.delete(id);
    }

}
