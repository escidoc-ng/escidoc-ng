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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.model.WorkspacePermissions;
import net.objecthunter.larch.model.WorkspacePermissions.Permission;
import net.objecthunter.larch.model.security.Group;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.AuthorizationService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchWorkspaceService;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DefaultAuthorizationService implements AuthorizationService {

    @Autowired
    private Client client;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public boolean hasPermission(User user, Workspace ws,
            WorkspacePermissions.Permission... permissionsToCheck) throws IOException {
        if (user.getGroups().contains(Group.ADMINS)) {
            return true;
        }
        final WorkspacePermissions wsp = ws.getPermissions();
        if (wsp == null) {
            return false;
        }
        final Map<String, EnumSet<WorkspacePermissions.Permission>> permissionMap = wsp.getPermissions();
        if (permissionMap == null) {
            return false;
        }
        final EnumSet<WorkspacePermissions.Permission> currentPermissions = permissionMap.get(user.getName());
        if (currentPermissions == null) {
            return false;
        }
        return currentPermissions.containsAll(Arrays.asList(permissionsToCheck));
    }

    @Override
    public boolean hasCurrentUserPermission(Workspace ws, WorkspacePermissions.Permission... permissionsToCheck)
            throws IOException {
        return hasPermission(this.getCurrentUser(), ws, permissionsToCheck);
    }

    @Override
    public void checkCurrentUserPermission(Workspace ws, WorkspacePermissions.Permission... permissionsToCheck)
            throws IOException {
        final User u = this.getCurrentUser();
        if (u.getGroups().contains(Group.ADMINS)) {
            return;
        }
        if (!hasPermission(u, ws, permissionsToCheck)) {
            throw new IOException("Access denied");
        }
    }

    @Override
    public void checkCurrentUserPermission(String workspaceId, WorkspacePermissions.Permission... permissionsToCheck)
            throws IOException {
        final GetResponse get =
                this.client.prepareGet(ElasticSearchWorkspaceService.INDEX_WORKSPACES,
                        ElasticSearchWorkspaceService.INDEX_WORKSPACE_TYPE, workspaceId)
                        .execute()
                        .actionGet();

        /* check if the workspace does exist */
        if (!get.isExists()) {
            throw new FileNotFoundException("The workspace with id '" + workspaceId + "' does not exist");
        }
        final Workspace ws = this.mapper.readValue(get.getSourceAsString(), Workspace.class);
        this.checkCurrentUserPermission(ws, permissionsToCheck);
    }

    @Override
    public WorkspacePermissions.Permission metadataReadPermissions(Entity e) {
        if (e.getState() == null || e.getState().isEmpty() || e.getState().equals(Entity.STATE_PENDING)) {
            return WorkspacePermissions.Permission.READ_PENDING_METADATA;
        } else if (e.getState().equals(Entity.STATE_PUBLISHED)) {
            return WorkspacePermissions.Permission.READ_SUBMITTED_METADATA;
        } else {
            return WorkspacePermissions.Permission.READ_RELEASED_METADATA;
        }
    }

    @Override
    public WorkspacePermissions.Permission[] metadataReadWritePermissions(Entity e) {
        return new WorkspacePermissions.Permission[] { this.metadataReadPermissions(e),
            this.metadataWritePermissions(e) };
    }

    @Override
    public WorkspacePermissions.Permission metadataWritePermissions(Entity e) {
        if (e.getState() == null || e.getState().isEmpty() || e.getState().equals(Entity.STATE_PENDING)) {
            return WorkspacePermissions.Permission.WRITE_PENDING_METADATA;
        } else if (e.getState().equals(Entity.STATE_PUBLISHED)) {
            return WorkspacePermissions.Permission.WRITE_SUBMITTED_METADATA;
        } else {
            return WorkspacePermissions.Permission.WRITE_RELEASED_METADATA;
        }
    }

    @Override
    public List<Workspace> retrieveUserWorkspaces() throws IOException {
        final List<Workspace> userWorkspaces = new ArrayList<>();
        if (!(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof User)) {
            return userWorkspaces;
        }
        String username = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getName();
        SearchResponse search;
        try {
            search = client.prepareSearch(ElasticSearchWorkspaceService.INDEX_WORKSPACES)
                    .setTypes(ElasticSearchWorkspaceService.INDEX_WORKSPACE_TYPE)
                    .setQuery(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
                            FilterBuilders.existsFilter("permissions.permissions." + username)))
                    .addFields("id", "name", "owner", "permissions.permissions." + username)
                    .execute()
                    .actionGet();
            if (search.getHits().getHits().length > 0) {
                for (SearchHit hit : search.getHits().getHits()) {
                    final Workspace workspace = new Workspace();
                    workspace.setId(hit.field("id").getValue());
                    workspace.setName(hit.field("name").getValue());
                    workspace.setOwner(hit.field("owner").getValue());

                    WorkspacePermissions workspacePermissions = new WorkspacePermissions();
                    if (hit.field("permissions.permissions." + username) != null) {
                        for (Object o : hit.field("permissions.permissions." + username).values()) {
                            String permissionName = (String) o;
                            workspacePermissions.addPermissions(username, Permission.valueOf(permissionName));
                        }
                    }
                    workspace.setPermissions(workspacePermissions);
                    userWorkspaces.add(workspace);
                }
            }
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        return userWorkspaces;
    }

    public User getCurrentUser() {
        return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
