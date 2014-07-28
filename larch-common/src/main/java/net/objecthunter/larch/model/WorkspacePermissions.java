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

package net.objecthunter.larch.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import net.objecthunter.larch.model.security.User;

public class WorkspacePermissions {

    private Map<String, EnumSet<Permission>> permissions = new HashMap<>();

    public void setPermissions(Map<String, EnumSet<Permission>> permissions) {
        this.permissions = permissions;
    }

    public void setPermissions(String username, EnumSet<Permission> permissionsToSet) {
        this.permissions.put(username, permissionsToSet);
    }

    public void addPermissions(String userName, Permission ... permissionsToSet) {
        EnumSet<Permission> existingPermissions = this.permissions.get(userName);
        if (existingPermissions == null) {
            existingPermissions = EnumSet.noneOf(Permission.class);
        }
        for (Permission p: permissionsToSet) {
            existingPermissions.add(p);
        }
        this.permissions.put(userName, existingPermissions);
    }

    public void removePermission(String userName, Permission ... permissionsToRemove) {
        final EnumSet<Permission> existingPermissions = this.permissions.get(userName);
        if (existingPermissions != null) {
            for (final Permission p: permissionsToRemove) {
                existingPermissions.remove(p);
            }
            this.permissions.put(userName, existingPermissions);
        }
    }

    public EnumSet<Permission> getPermissions(String username) {
        return this.permissions.get(username);
    }

    public Map<String, EnumSet<Permission>> getPermissions() {
        return this.permissions;
    }

    public boolean hasPermissions(final String username, final Permission ... permissionsToCheck) {
        final EnumSet<Permission> currentPermissions = this.getPermissions(username);
        if (currentPermissions == null) {
            return false;
        }
        for (final Permission p : permissionsToCheck) {
            if (!currentPermissions.contains(p)) {
                return false;
            }
        }
        return true;
    }


    public enum Permission {
        READ_PENDING_METADATA,
        READ_SUBMITTED_METADATA,
        READ_RELEASED_METADATA,
        READ_WITHDRAWN_METADATA,
        WRITE_PENDING_METADATA,
        WRITE_SUBMITTED_METADATA,
        WRITE_RELEASED_METADATA,
        WRITE_WITHDRAWN_METADATA,
        READ_PENDING_BINARY,
        READ_SUBMITTED_BINARY,
        READ_RELEASED_BINARY,
        READ_WITHDRAWN_BINARY,
        WRITE_PENDING_BINARY,
        WRITE_SUBMITTED_BINARY,
        WRITE_RELEASED_BINARY,
        WRITE_WITHDRAWN_BINARY,
    }
}
