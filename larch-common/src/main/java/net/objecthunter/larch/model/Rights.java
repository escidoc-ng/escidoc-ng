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

public class Rights {

    private Map<String, EnumSet<Right>> rights = new HashMap<>();

    public void setRights(Map<String, EnumSet<Right>> rights) {
        this.rights = rights;
    }

    public void setRights(String username, EnumSet<Right> rightsToSet) {
        if (rightsToSet == null || rightsToSet.isEmpty()) {
            this.rights.remove(username);
        } else {
            this.rights.put(username, rightsToSet);
        }
    }

    public void addRights(String userName, Right ... rightsToSet) {
        EnumSet<Right> existingRights = this.rights.get(userName);
        if (existingRights == null) {
            existingRights = EnumSet.noneOf(Right.class);
        }
        for (Right p: rightsToSet) {
            existingRights.add(p);
        }
        this.rights.put(userName, existingRights);
    }

    public void removeRights(String userName, Right ... rightsToRemove) {
        final EnumSet<Right> existingRights = this.rights.get(userName);
        if (existingRights != null) {
            for (final Right p: rightsToRemove) {
                existingRights.remove(p);
            }
            this.rights.put(userName, existingRights);
        }
    }

    public EnumSet<Right> getRights(String username) {
        return this.rights.get(username);
    }

    public Map<String, EnumSet<Right>> getRights() {
        return this.rights;
    }

    public boolean hasRights(final String username, final Right ... rightsToCheck) {
        final EnumSet<Right> currentRights = this.getRights(username);
        if (currentRights == null) {
            return false;
        }
        for (final Right p : rightsToCheck) {
            if (!currentRights.contains(p)) {
                return false;
            }
        }
        return true;
    }


    public enum Right {
        READ_PENDING_METADATA,
        READ_SUBMITTED_METADATA,
        READ_PUBLISHED_METADATA,
        READ_WITHDRAWN_METADATA,
        WRITE_PENDING_METADATA,
        WRITE_SUBMITTED_METADATA,
        WRITE_PUBLISHED_METADATA,
        WRITE_WITHDRAWN_METADATA,
        READ_PENDING_BINARY,
        READ_SUBMITTED_BINARY,
        READ_PUBLISHED_BINARY,
        READ_WITHDRAWN_BINARY,
        WRITE_PENDING_BINARY,
        WRITE_SUBMITTED_BINARY,
        WRITE_PUBLISHED_BINARY,
        WRITE_WITHDRAWN_BINARY,
        READ_PERMISSION,
        WRITE_PERMISSION
    }

    public static Rights getDefaultRights(String userName) {
        final Rights p = new Rights();
        p.addRights(userName,
                Right.READ_PENDING_METADATA,
                Right.READ_SUBMITTED_METADATA,
                Right.READ_PUBLISHED_METADATA,
                Right.READ_WITHDRAWN_METADATA,
                Right.READ_PENDING_BINARY,
                Right.READ_SUBMITTED_BINARY,
                Right.READ_PUBLISHED_BINARY,
                Right.READ_WITHDRAWN_BINARY,
                Right.READ_PERMISSION,
                Right.WRITE_PENDING_METADATA,
                Right.WRITE_PENDING_BINARY,
                Right.WRITE_PERMISSION);
        return p;
    }
}
