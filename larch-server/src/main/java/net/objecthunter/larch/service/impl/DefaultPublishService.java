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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package net.objecthunter.larch.service.impl;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.model.Entities;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.service.PublishService;
import net.objecthunter.larch.service.backend.BackendPublishService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 * The default implementation of {@link net.objecthunter.larch.service.EntityService} responsible for perofrming CRUD
 * operations of {@link net.objecthunter.larch.model.Entity} objects
 */
public class DefaultPublishService implements PublishService {

    private static final Logger log = LoggerFactory.getLogger(DefaultPublishService.class);

    @Autowired
    private BackendPublishService backendPublishService;

    @Autowired
    private Environment env;

    private boolean autoExport;

    @PostConstruct
    public void init() {
        final String val = env.getProperty("larch.export.auto");
        autoExport = val == null ? false : Boolean.valueOf(val);
    }

    @Override
    public Entity retrieve(String id) throws IOException {
        return backendPublishService.retrievePublishedEntity(id);
    }

    @Override
    public Entities retrievePublishedEntities(String entityId) throws IOException {
        return backendPublishService.retrievePublishedEntities(entityId);
    }

    @Override
    public SearchResult scanEntities(int offset) throws IOException {
        return backendPublishService.scanIndex(offset);
    }

    @Override
    public SearchResult scanEntities(int offset, int numRecords) throws IOException {
        return backendPublishService.scanIndex(offset, numRecords);
    }

    @Override
    public SearchResult scanWorkspaceEntities(String workspaceId, int offset) throws IOException {
        return backendPublishService.scanWorkspace(workspaceId, offset);
    }

    @Override
    public SearchResult scanWorkspaceEntities(String workspaceId, int offset, int numRecords) throws IOException {
        return backendPublishService.scanWorkspace(workspaceId, offset, numRecords);
    }

    @Override
    public SearchResult searchEntities(Map<EntitiesSearchField, String[]> searchFields) throws IOException {
        return backendPublishService.searchEntities(searchFields);
    }

}
