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
package de.escidocng.model;

import de.escidocng.model.Entity.EntityState;

public class Archive {

    private String entityId;

    private int entityVersion;
    
    private String contentModelId;

    private EntityState state;

    private String path;

    private String createdDate;

    private String creator;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public int getEntityVersion() {
        return entityVersion;
    }

    public void setEntityVersion(int entityVersion) {
        this.entityVersion = entityVersion;
    }

    
    /**
     * @return the contentModelId
     */
    public String getContentModelId() {
        return contentModelId;
    }

    
    /**
     * @param contentModelId the contentModelId to set
     */
    public void setContentModelId(String contentModelId) {
        this.contentModelId = contentModelId;
    }

    
    /**
     * @return the state
     */
    public EntityState getState() {
        return state;
    }

    
    /**
     * @param state the state to set
     */
    public void setState(EntityState state) {
        this.state = state;
    }
    
}
