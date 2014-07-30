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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.model.WorkspacePermissions;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.AuthorizationService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchWorkspaceService;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
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
    public boolean hasPermission(String username, Workspace ws,
                                 WorkspacePermissions.Permission... permissionsToCheck) throws IOException {
        final WorkspacePermissions wsp = ws.getPermissions();
        if (wsp == null) {
            return false;
        }
        final Map<String, EnumSet<WorkspacePermissions.Permission>> permissionMap = wsp.getPermissions();
        if (permissionMap == null) {
            return false;
        }
        final EnumSet<WorkspacePermissions.Permission> currentPermissions = permissionMap.get(username);
        if (currentPermissions == null) {
            return false;
        }
        return currentPermissions.containsAll(Arrays.asList(permissionsToCheck));
    }

    @Override
    public boolean hasCurrentUserPermission(Workspace ws, WorkspacePermissions.Permission... permissionsToCheck) throws IOException {
        return hasPermission(this.getCurrentUsername(), ws, permissionsToCheck);
    }

    @Override
    public void checkCurrentUserPermission(Workspace ws, WorkspacePermissions.Permission... permissionsToCheck) throws IOException {
        if (!hasPermission(this.getCurrentUsername(), ws, permissionsToCheck)) {
            throw new IOException("Access denied");
        }
    }

    @Override
    public void checkCurrentUserPermission(String workspaceId, WorkspacePermissions.Permission... permissionsToCheck) throws IOException {
        final GetResponse get = this.client.prepareGet(ElasticSearchWorkspaceService.INDEX_WORKSPACES, ElasticSearchWorkspaceService.INDEX_WORKSPACE_TYPE, workspaceId)
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
        }else if(e.getState().equals(Entity.STATE_PUBLISHED)) {
            return WorkspacePermissions.Permission.READ_SUBMITTED_METADATA;
        } else {
            return WorkspacePermissions.Permission.READ_RELEASED_METADATA;
        }
    }

    @Override
    public WorkspacePermissions.Permission[] metadataReadWritePermissions(Entity e) {
        return new WorkspacePermissions.Permission[] {this.metadataReadPermissions(e), this.metadataWritePermissions(e)};
    }

    @Override
    public WorkspacePermissions.Permission metadataWritePermissions(Entity e) {
        if (e.getState() == null || e.getState().isEmpty() || e.getState().equals(Entity.STATE_PENDING)) {
            return WorkspacePermissions.Permission.WRITE_PENDING_METADATA;
        }else if(e.getState().equals(Entity.STATE_PUBLISHED)) {
            return WorkspacePermissions.Permission.WRITE_SUBMITTED_METADATA;
        } else {
            return WorkspacePermissions.Permission.WRITE_RELEASED_METADATA;
        }
    }

    public String getCurrentUsername() {
        return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getName();
    }
}
