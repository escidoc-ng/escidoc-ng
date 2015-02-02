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
package net.objecthunter.larch.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A DTO for a top level larch repository object. Creating any object in the larch repository normally starts with
 * creating an {@link Entity} and adding content/metadata to it.
 */
public class Entity {

    private String id;

    private String parentId;

    private String contentModelId;

    private EntityState state;

    private int version;

    private List<String> children;

    private String label;

    private String utcCreated;

    private String utcLastModified;

    private List<String> tags;

    private List<Metadata> metadata;

    private List<Binary> binaries;

    private List<AlternativeIdentifier> alternativeIdentifiers;

    private List<Relation> relations;

    
    /**
     * @return the relations
     */
    public List<Relation> getRelations() {
        return relations;
    }

    
    /**
     * @param relations the relations to set
     */
    public void setRelations(List<Relation> relations) {
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
     * @return the metadata
     */
    public List<Metadata> getMetadata() {
        return metadata;
    }


    
    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(List<Metadata> metadata) {
        this.metadata = metadata;
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
     * @return the binaries
     */
    public List<Binary> getBinaries() {
        return binaries;
    }


    
    /**
     * @param binaries the binaries to set
     */
    public void setBinaries(List<Binary> binaries) {
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
     * Get the state of the entity
     * 
     * @return the state
     */
    public EntityState getState() {
        return state;
    }

    /**
     * Set the state of the entity
     * 
     * @param state the state to set
     */
    public void setState(EntityState state) {
        this.state = state;
    }
    
    /**
     * Check if metadata-list contains Metadata with given name.
     * 
     * @param name
     * @return boolean true|false
     */
    public boolean hasMetadata(String name) {
        if (name != null && metadata != null) {
            for(Metadata m : metadata) {
                if (name.equals(m.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Check if binary-list contains Binary with given name.
     * 
     * @param name
     * @return boolean true|false
     */
    public boolean hasBinary(String name) {
        if (name != null && binaries != null) {
            for(Binary b : binaries) {
                if (name.equals(b.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Check if relation-list contains Relation with given predicate.
     * 
     * @param predicate
     * @return boolean true|false
     */
    public boolean hasRelation(String predicate) {
        if (predicate != null && relations != null) {
            for(Relation r : relations) {
                if (predicate.equals(r.getPredicate())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * get Metadata with given name.
     * 
     * @param name
     * @return Metadata
     */
    public Metadata getMetadata(String name) {
        if (name != null && metadata != null) {
            for(Metadata m : metadata) {
                if (name.equals(m.getName())) {
                    return m;
                }
            }
        }
        return null;
    }
    
    /**
     * get Binary with given name.
     * 
     * @param name
     * @return Binary
     */
    public Binary getBinary(String name) {
        if (name != null && binaries != null) {
            for(Binary b : binaries) {
                if (name.equals(b.getName())) {
                    return b;
                }
            }
        }
        return null;
    }
    
    /**
     * get Relation with given predicate.
     * 
     * @param predicate
     * @return Relation
     */
    public Relation getRelation(String predicate) {
        if (predicate != null && relations != null) {
            for(Relation r : relations) {
                if (predicate.equals(r.getPredicate())) {
                    return r;
                }
            }
        }
        return null;
    }
    
    /**
     * remove Metadata with given name.
     * 
     * @param name
     */
    public void removeMetadata(String name) {
        int index = -1;
        if (name != null && metadata != null) {
            for (int i = 0; i < metadata.size(); i++) {
                if (name.equals(metadata.get(i).getName())) {
                    index = i;
                    break;
                }
            }
        }
        if (index > -1) {
            metadata.remove(index);
        }
    }
    
    /**
     * remove Binary with given name.
     * 
     * @param name
     */
    public void removeBinary(String name) {
        int index = -1;
        if (name != null && binaries != null) {
            for (int i = 0; i < binaries.size(); i++) {
                if (name.equals(binaries.get(i).getName())) {
                    index = i;
                    break;
                }
            }
        }
        if (index > -1) {
            binaries.remove(index);
        }
    }
    
    public enum EntityState {
        PENDING,
        SUBMITTED,
        PUBLISHED,
        WITHDRAWN;

        public String getName() {
            return this.toString();
        }
        
    }

}
