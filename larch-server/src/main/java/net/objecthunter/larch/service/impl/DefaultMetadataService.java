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
package net.objecthunter.larch.service.impl;

import java.io.IOException;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.service.AuthorizationService;
import net.objecthunter.larch.service.MetadataService;
import net.objecthunter.larch.service.backend.BackendMetadataService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;
import net.objecthunter.larch.service.backend.elasticsearch.queryrestriction.QueryRestrictionFactory;
import net.objecthunter.larch.service.backend.elasticsearch.queryrestriction.RoleQueryRestriction;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The default implementation of {@link net.objecthunter.larch.service.MetadataService} responsible for searching in metadata
 */
public class DefaultMetadataService implements MetadataService {

    private static final Logger log = LoggerFactory.getLogger(DefaultMetadataService.class);

    @Autowired
    private BackendMetadataService backendMetadataService;

    @Autowired
    private AuthorizationService defaultAuthorizationService;

    @PostConstruct
    public void init() {
    }

    @Override
    public SearchResult searchEntities(String query, int offset)
            throws IOException {
        // add user restriction
        StringBuilder queryBuilder = new StringBuilder("");
        if (StringUtils.isNotBlank(query)) {
            queryBuilder.append("(").append(query).append(") AND ");
        }
        queryBuilder.append(getMetadatasUserRestrictionQuery());
        return backendMetadataService.searchMetadata(queryBuilder.toString(), offset);
    }

    @Override
    public SearchResult searchEntities(String query, int offset, int maxRecords)
            throws IOException {
        // add user restriction
        StringBuilder queryBuilder = new StringBuilder("");
        if (StringUtils.isNotBlank(query)) {
            queryBuilder.append("(").append(query).append(") AND ");
        }
        queryBuilder.append(getMetadatasUserRestrictionQuery());
        return backendMetadataService.searchMetadata(queryBuilder.toString(), offset, maxRecords);
    }

    /**
     * Get Query that restricts a search to metadata the user may see.
     * 
     * @return QueryBuilder with user-restriction query
     */
    private String getMetadatasUserRestrictionQuery() throws IOException {
        User currentUser = defaultAuthorizationService.getCurrentUser();
        StringBuilder restrictionQueryBuilder = new StringBuilder("(");
        if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().isEmpty()) {
            // restrict to nothing
            restrictionQueryBuilder.append(EntitiesSearchField.STATE.getFieldName()).append(":NONEXISTING");
            restrictionQueryBuilder.append(")");
            return restrictionQueryBuilder.toString();
        } else {
            int counter = 0;
            for (Role role : currentUser.getRoles()) {
                if (counter > 0) {
                    restrictionQueryBuilder.append(" OR ");
                }
                RoleQueryRestriction roleQueryRestriction = QueryRestrictionFactory.getRoleQueryRestriction(role);
                restrictionQueryBuilder.append(roleQueryRestriction.getEntitiesRestrictionQuery());
                counter++;
            }
        }
        restrictionQueryBuilder.append(")");
        return restrictionQueryBuilder.toString();
    }

}
