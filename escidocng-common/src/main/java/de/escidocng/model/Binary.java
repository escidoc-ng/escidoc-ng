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

import java.util.List;

import de.escidocng.model.source.Source;

/**
 * A DTO for a escidocng repository object which can have binary data attached to it. The actual binary content is wrapped
 * in a Source object depending on it's location: For example a {@link de.escidocng.model.source .UrlSource}
 * for a location reachable by http: {@code http://example.com/image.jpg}
 */
public class Binary {

    private String name;

    private long size;

    private String mimetype;

    private List<Metadata> metadata;

    private String filename;

    private String checksum;

    private String checksumType;

    private String path;

    private Source source;

    private String utcCreated;

    private String utcLastModified;

    public String getUtcLastModified() {
        return utcLastModified;
    }

    public void setUtcLastModified(String utcLastModified) {
        this.utcLastModified = utcLastModified;
    }

    public String getUtcCreated() {
        return utcCreated;
    }

    public void setUtcCreated(String utcCreated) {
        this.utcCreated = utcCreated;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getChecksumType() {
        return checksumType;
    }

    public void setChecksumType(String checksumType) {
        this.checksumType = checksumType;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
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

    public String getFilename() {
        if (filename == null || filename.isEmpty()) {
            return (name == null || name.isEmpty()) ? "binary" : name;
        } else {
            return filename;
        }
    }

    public void setFilename(String filename) {
        this.filename = filename;
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
    
}
