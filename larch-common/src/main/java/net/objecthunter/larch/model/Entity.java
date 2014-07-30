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
import java.util.Map;

/**
 * A DTO for a top level larch repository object. Creating any object in the larch repository normally starts with
 * creating an {@link Entity} and adding content/metadata to it.
 */
public class Entity {

    public static final String STATE_PUBLISHED = "published";

    public static final String STATE_ARCHIVED = "archived";

    public static final String STATE_PENDING = "ingested";

    private int version;

    private String id;

    private String workspaceId;

    private String publishId;

    private String label;

    private String type;

    private String parentId;

    private String state;

    private String utcCreated;

    private String utcLastModified;

    private List<String> tags;

    private List<String> children;

    private Map<String, Metadata> metadata;

    private Map<String, Binary> binaries;

    private List<AlternativeIdentifier> alternativeIdentifiers;

    private Map<String, List<String>> relations;

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    /**
     * Get the relations of an {@link Entity}
     * 
     * @return the Relations of the entity
     */
    public Map<String, List<String>> getRelations() {
        return relations;
    }

    /**
     * Set the entity's relations
     * 
     * @param relations the relations to set
     */
    public void setRelations(Map<String, List<String>> relations) {
        this.relations = relations;
    }

    /**
     * Get the child entities of this entity
     * 
     * @return the child entities
     */
    public List<String> getChildren() {
        return children;
    }

    /**
     * Set the child entities of this entity
     * 
     * @param children the child entities to set
     */
    public void setChildren(List<String> children) {
        this.children = children;
    }

    /**
     * Get the last modified timestamp
     * 
     * @return a UTC timestamp
     */
    public String getUtcLastModified() {
        return utcLastModified;
    }

    /**
     * Set the last modified timestamp
     * 
     * @param utcLastModified the UTC timestamp to set
     */
    public void setUtcLastModified(String utcLastModified) {
        this.utcLastModified = utcLastModified;
    }

    /**
     * Get the created timestamp
     * 
     * @return the timestamp
     */
    public String getUtcCreated() {
        return utcCreated;
    }

    /**
     * Set the created timestamp
     * 
     * @param utcCreated the UTC timestamp to set
     */
    public void setUtcCreated(String utcCreated) {
        this.utcCreated = utcCreated;
    }

    /**
     * Get the id of the entity
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Set the if of the entity
     * 
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the publishId
     */
    public String getPublishId() {
        return publishId;
    }

    /**
     * @param publishId the publishId to set
     */
    public void setPublishId(String publishId) {
        this.publishId = publishId;
    }

    /**
     * Get the entity's label
     * 
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set the entity's label
     * 
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Get the entity's tags
     * 
     * @return the tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Set the entity's tags
     * 
     * @param tags the tags to set
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Get the parent id of the entity
     * 
     * @return the parent id
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * Set the parent id of the entity
     * 
     * @param parentId the parent id
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * Get the metadata of an entity
     * 
     * @return the metadata of the entity
     */
    public Map<String, Metadata> getMetadata() {
        return metadata;
    }

    /**
     * Set the metadata of an entity
     * 
     * @param metadata the metadata to set
     */
    public void setMetadata(Map<String, Metadata> metadata) {
        this.metadata = metadata;
    }

    /**
     * Get the entity's type
     * 
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Set the entity's type
     * 
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get the entity's binaries
     * 
     * @return the binaries
     */
    public Map<String, Binary> getBinaries() {
        return binaries;
    }

    /**
     * Set the entity's binaries
     * 
     * @param binaries the binaries to set
     */
    public void setBinaries(Map<String, Binary> binaries) {
        this.binaries = binaries;
    }

    /**
     * @return the alternativeIdentifiers
     */
    public List<AlternativeIdentifier> getAlternativeIdentifiers() {
        if (alternativeIdentifiers == null) {
            alternativeIdentifiers = new ArrayList<AlternativeIdentifier>();
        }
        return alternativeIdentifiers;
    }

    /**
     * @param alternativeIdentifiers the alternativeIdentifiers to set
     */
    public void setAlternativeIdentifiers(List<AlternativeIdentifier> alternativeIdentifiers) {
        this.alternativeIdentifiers = alternativeIdentifiers;
    }

    /**
     * Get the version number of the entity
     * 
     * @return the version number
     */
    public int getVersion() {
        return version;
    }

    /**
     * Set the version number of an entity
     * 
     * @param version the version number to set
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Set the version number of an entity and set it's state to INGESTED
     * 
     * @param version the version number to set
     */
    public void setVersionAndResetState(int version) {
        this.version = version;
        this.state = STATE_PENDING;
    }

    /**
     * Get the state of the entity
     * 
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * Set the state of the entity
     * 
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }
}
