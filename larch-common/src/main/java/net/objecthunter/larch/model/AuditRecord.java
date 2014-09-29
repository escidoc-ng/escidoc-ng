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
 * WITHOUT WARRANTIES OR CONDITIONS OF ROLE_ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package net.objecthunter.larch.model;

/**
 * DTO class for wrapping audit information
 */
public class AuditRecord {

    public static final String EVENT_CREATE_ENTITY = "Create entity";

    public static final String EVENT_UPDATE_ENTITY = "Update entity";

    public static final String EVENT_SUBMIT_ENTITY = "Submit entity";

    public static final String EVENT_PUBLISH_ENTITY = "Publish entity";

    public static final String EVENT_CREATE_BINARY = "Create binary";

    public static final String EVENT_DELETE_BINARY = "Delete binary";

    public static final String EVENT_CREATE_METADATA = "Create metadata";

    public static final String EVENT_CREATE_BINARY_METADATA = "Create metadata for binary";

    public static final String EVENT_DELETE_METADATA = "Delete metadata";

    public static final String EVENT_DELETE_BINARY_METADATA = "Delete metadata for binary";

    public static final String EVENT_CREATE_RELATION = "Add relation";

    public static final String EVENT_DELETE_RELATION = "Delete relation";

    public static final String EVENT_DELETE_ENTITY = "Delete entity";

    public static final String EVENT_UPDATE_BINARY = "Update binary";

    public static final String EVENT_UPDATE_RELATION = "Update relation";

    public static final String EVENT_UPDATE_METADATA = "Update metadata";

    public static final String EVENT_UPDATE_BINARY_METADATA = "Update metadata for binary";

    public static final String EVENT_CREATE_IDENTIFIER = "Create identifier";

    public static final String EVENT_DELETE_IDENTIFIER = "Delete identifier";

    private String id;

    private String level2Id;

    private String entityId;

    private String agentName;

    private String action;

    private String timestamp;

    /**
     * Get the id of the AuditRecord
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id of the AuditRecord
     * 
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the Entity id
     * 
     * @return the id ofthe Entity
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Ste the entity if of the AuditRecord
     * 
     * @param entityId the entity id to set
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    /**
     * Get the agent's name from the AuditRecord
     * 
     * @return the agent's name
     */
    public String getAgentName() {
        return agentName;
    }

    /**
     * Set the agent's name
     * 
     * @param agentName the name to set
     */
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    /**
     * Get the Action of this AuditRecord e.g. "Create Entity"
     * 
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * Set the Action of the AuditRecord
     * 
     * @param action the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Get the timestamp from the AuditRecord
     * 
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Set the timespatmp of the AuditRecord
     * 
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLevel2Id() {
        return level2Id;
    }

    public void setLevel2Id(String level2Id) {
        this.level2Id = level2Id;
    }

    @Override
    public String toString() {
        return "ID:" + this.id + ",WS: " + this.level2Id + " TS:" + this.timestamp + ", EVENT:" + this.action +
                ", ENTITY:" + this.entityId;
    }
}
