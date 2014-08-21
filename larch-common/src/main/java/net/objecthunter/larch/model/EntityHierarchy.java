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

import java.util.ArrayList;
import java.util.List;

import net.objecthunter.larch.model.Entity.EntityType;

/**
 * Object holds a Hierarchy of an entity.
 */
public class EntityHierarchy {

    private EntityType type;

    private String entityId;

    private String areaId;

    private String permissionId;

    private List<String> ancestorEntityIds = new ArrayList<String>();

    
    
    /**
     * @return the type
     */
    public EntityType getType() {
        return type;
    }


    
    /**
     * @param type the type to set
     */
    public void setType(EntityType type) {
        this.type = type;
    }


    /**
     * @return the entityId
     */
    public String getEntityId() {
        return entityId;
    }

    
    /**
     * @param entityId the entityId to set
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    
    /**
     * @return the areaId
     */
    public String getAreaId() {
        return areaId;
    }

    
    /**
     * @param areaId the areaId to set
     */
    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    
    /**
     * @return the permissionId
     */
    public String getPermissionId() {
        return permissionId;
    }

    
    /**
     * @param permissionId the permissionId to set
     */
    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }

    
    /**
     * @return the ancestorEntityIds
     */
    public List<String> getAncestorEntityIds() {
        return ancestorEntityIds;
    }

    
    /**
     * @param ancestorEntityIds the ancestorEntityIds to set
     */
    public void setAncestorEntityIds(List<String> ancestorEntityIds) {
        if (ancestorEntityIds != null) {
            this.ancestorEntityIds = ancestorEntityIds;
        } else {
            this.ancestorEntityIds = new ArrayList<String>();
        }
    }

}
