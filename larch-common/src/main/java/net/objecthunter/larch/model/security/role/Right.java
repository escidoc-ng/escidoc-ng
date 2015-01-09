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
package net.objecthunter.larch.model.security.role;

import java.util.List;

import net.objecthunter.larch.model.security.role.Role.RoleRight;

/**
 * @author mih Class holds attributes for a relation.
 */
public class Right {

    private String anchorId;

    private List<RoleRight> roleRights;

    public Right() {
    }

    public Right(String anchorId, List<RoleRight> roleRights) {
        this.anchorId = anchorId;
        this.roleRights = roleRights;
    }

    
    /**
     * @return the anchorId
     */
    public String getAnchorId() {
        return anchorId;
    }

    
    /**
     * @param anchorId the anchorId to set
     */
    public void setAnchorId(String anchorId) {
        this.anchorId = anchorId;
    }

    
    /**
     * @return the roleRights
     */
    public List<RoleRight> getRoleRights() {
        return roleRights;
    }

    
    /**
     * @param roleRights the roleRights to set
     */
    public void setRoleRights(List<RoleRight> roleRights) {
        this.roleRights = roleRights;
    }

}