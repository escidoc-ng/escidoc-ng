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
import java.util.*;

import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.model.WorkspacePermissions;
import net.objecthunter.larch.service.AuthorizationService;
import net.objecthunter.larch.service.backend.BackendWorkspaceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultAuthorizationService implements AuthorizationService {

    @Autowired
    private BackendWorkspaceService workspaceService;

    @Override
    public boolean hasPermission(String username, String workspaceId,
            WorkspacePermissions.Permission... permissionsToCheck) throws IOException {
        final Workspace ws = workspaceService.retrieveWorkspace(workspaceId);
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
    public void addPermissions(String userName, String workspaceId,
            WorkspacePermissions.Permission... permissionsToSet) throws IOException {
        final Workspace ws = workspaceService.retrieveWorkspace(workspaceId);
        WorkspacePermissions wsp = ws.getPermissions();
        if (wsp == null) {
            wsp = new WorkspacePermissions();
            ws.setPermissions(wsp);
        }
        Map<String, EnumSet<WorkspacePermissions.Permission>> permissionMap = wsp.getPermissions();
        if (permissionMap == null) {
            permissionMap = new HashMap<>(1);
            wsp.setPermissions(permissionMap);
        }
        EnumSet<WorkspacePermissions.Permission> permissions = permissionMap.get(userName);
        if (permissions == null) {
            permissions = EnumSet.noneOf(WorkspacePermissions.Permission.class);
        }
        permissions.addAll(Arrays.asList(permissionsToSet));
        workspaceService.updateWorkspace(ws);
    }

    @Override
    public void removePermissions(String userName, String workspaceId,
            WorkspacePermissions.Permission... permissionsToRemove) throws IOException {
        final Workspace ws = workspaceService.retrieveWorkspace(workspaceId);
        final WorkspacePermissions wsp = ws.getPermissions();
        if (wsp == null) {
            return;
        }
        final Map<String, EnumSet<WorkspacePermissions.Permission>> permissionMap = wsp.getPermissions();
        if (permissionMap == null) {
            return;
        }
        final EnumSet<WorkspacePermissions.Permission> permissions = permissionMap.get(userName);
        if (permissions == null) {
            return;
        }
        for (WorkspacePermissions.Permission p : permissionsToRemove) {
            permissions.removeAll(Arrays.asList(permissionsToRemove));
        }
    }

}
