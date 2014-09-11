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

package net.objecthunter.larch.model.security;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Rights {

    private Map<String, Set<Right>> rights = new HashMap<>();

    public void setRights(Map<String, Set<Right>> rights) {
        this.rights = rights;
    }

    public void setRights(String entityId, Set<Right> rightsToSet) {
        if (rightsToSet == null || rightsToSet.isEmpty()) {
            this.rights.remove(entityId);
        } else {
            this.rights.put(entityId, rightsToSet);
        }
    }

    public void addRights(String entityId, Right ... rightsToSet) {
        Set<Right> existingRights = this.rights.get(entityId);
        if (existingRights == null) {
            existingRights = new HashSet<Right>();
        }
        for (Right p: rightsToSet) {
            existingRights.add(p);
        }
        this.rights.put(entityId, existingRights);
    }

    public void removeRights(String entityId, Right ... rightsToRemove) {
        final Set<Right> existingRights = this.rights.get(entityId);
        if (existingRights != null) {
            for (final Right p: rightsToRemove) {
                existingRights.remove(p);
            }
            this.rights.put(entityId, existingRights);
        }
    }

    public Set<Right> getRights(String entityId) {
        return this.rights.get(entityId);
    }

    public Map<String, Set<Right>> getRights() {
        return this.rights;
    }

    public boolean hasRights(final String entityId, final Right ... rightsToCheck) {
        final Set<Right> currentRights = this.getRights(entityId);
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

}
