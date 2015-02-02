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
import java.io.InputStream;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import net.objecthunter.larch.model.Archive;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.service.ArchiveService;
import net.objecthunter.larch.service.AuthorizationService;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.backend.BackendArchiveBlobService;
import net.objecthunter.larch.service.backend.BackendArchiveIndexService;
import net.objecthunter.larch.service.backend.BackendEntityService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchArchiveIndexService.ArchivesSearchField;
import net.objecthunter.larch.service.backend.elasticsearch.queryrestriction.QueryRestrictionFactory;
import net.objecthunter.larch.service.backend.elasticsearch.queryrestriction.RoleQueryRestriction;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

public class DefaultArchiveService implements ArchiveService {

    @Autowired
    private BackendArchiveBlobService archiveBlobStore;

    @Autowired
    private BackendArchiveIndexService archiveIndex;

    @Autowired
    private BackendEntityService backendEntityService;

    @Autowired
    private EntityService entityService;

    @Autowired
    private AuthorizationService defaultAuthorizationService;

    @Override
    public void archive(final String entityId, final int version) throws IOException {
        final Entity e = entityService.retrieve(entityId, version);
        final String path = archiveBlobStore.saveOrUpdate(e);
        final String userName = ((User) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal()).getName();
        Archive a = new Archive();
        a.setEntityId(entityId);
        a.setEntityVersion(version);
        a.setContentModelId(e.getContentModelId());
        a.setState(e.getState());
        a.setCreatedDate(ZonedDateTime.now(ZoneOffset.UTC).toString());
        a.setPath(path);
        a.setCreator(userName);
        archiveIndex.saveOrUpdate(a, backendEntityService.getHierarchy(e));
    }

    @Override
    public boolean isArchived(final String entityId, final int version) throws IOException {
        return archiveIndex.exists(entityId, version);
    }

    @Override
    public InputStream retrieveData(final String entityId, final int version) throws IOException {
        final Archive a = archiveIndex.retrieve(entityId, version);
        return archiveBlobStore.retrieve(a.getPath());
    }

    @Override
    public void delete(final String entityId, final int version) throws IOException {
        final Archive a = archiveIndex.retrieve(entityId, version);
        archiveIndex.delete(entityId, version);
        archiveBlobStore.delete(a.getPath());
    }

    @Override
    public long sizeof(String entityId, int version) throws IOException {
        Archive a = archiveIndex.retrieve(entityId, version);
        return archiveBlobStore.sizeOf(a.getPath());
    }

    @Override
    public Archive retrieve(String entityId, int version) throws IOException {
        return archiveIndex.retrieve(entityId, version);
    }

    @Override
    public SearchResult searchArchives(String query, int offset, int maxRecords)
            throws IOException {
        // add user restriction
        StringBuilder queryBuilder = new StringBuilder("");
        if (StringUtils.isNotBlank(query)) {
            queryBuilder.append("(").append(query).append(") AND ");
        }
        queryBuilder.append(getArchivesUserRestrictionQuery());
        return archiveIndex.searchArchives(queryBuilder.toString(), offset, maxRecords);
    }

    /**
     * Get Query that restricts a search to archives the user may see.
     * 
     * @return QueryBuilder with user-restriction query
     */
    private String getArchivesUserRestrictionQuery() throws IOException {
        User currentUser = defaultAuthorizationService.getCurrentUser();
        StringBuilder restrictionQueryBuilder = new StringBuilder("(");
        if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().isEmpty()) {
            // restrict to nothing
            restrictionQueryBuilder.append(ArchivesSearchField.STATE.getFieldName()).append(":NONEXISTING");
            restrictionQueryBuilder.append(")");
            return restrictionQueryBuilder.toString();
        } else {
            int counter = 0;
            for (Role role : currentUser.getRoles()) {
                if (counter > 0) {
                    restrictionQueryBuilder.append(" OR ");
                }
                RoleQueryRestriction roleQueryRestriction = QueryRestrictionFactory.getRoleQueryRestriction(role);
                restrictionQueryBuilder.append(roleQueryRestriction.getArchivesRestrictionQuery());
                counter++;
            }
        }
        restrictionQueryBuilder.append(")");
        return restrictionQueryBuilder.toString();
    }

}
