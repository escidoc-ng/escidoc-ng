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

import net.objecthunter.larch.model.WorkspacePermissions;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public interface AuthorizationService {
    boolean hasPermission(String username, String workspaceId, WorkspacePermissions.Permission... permissionsToCheck) throws IOException;
    void addPermissions(String username, String workspaceId, WorkspacePermissions.Permission ... permissionsToAdd) throws IOException;
    void removePermissions(String username, String workspaceId, WorkspacePermissions.Permission ... permissionsToRemove) throws IOException;
}
