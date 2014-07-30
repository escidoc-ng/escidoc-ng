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

package net.objecthunter.larch.service;

import java.io.IOException;

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.model.WorkspacePermissions;

public interface AuthorizationService {

    boolean hasPermission(String username, Workspace ws,
            WorkspacePermissions.Permission... permissionsToCheck) throws IOException;

    boolean hasCurrentUserPermission(Workspace ws, WorkspacePermissions.Permission... permissionsToCheck)
            throws IOException;

    void checkCurrentUserPermission(Workspace ws, WorkspacePermissions.Permission... permissionsToCheck)
            throws IOException;

    void checkCurrentUserPermission(String workspaceId, WorkspacePermissions.Permission... permissionsToCheck)
            throws IOException;

    WorkspacePermissions.Permission metadataReadPermissions(Entity e);

    WorkspacePermissions.Permission[] metadataReadWritePermissions(Entity e);

    WorkspacePermissions.Permission metadataWritePermissions(Entity e);
}
