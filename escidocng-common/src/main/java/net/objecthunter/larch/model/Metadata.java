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

import com.fasterxml.jackson.databind.JsonNode;

import net.objecthunter.larch.model.source.Source;

/**
 * A DTO for wrapping arbitrary meta data of a larch repository object. The meta data can only be validated if it's in
 * XML format and a schemaUrl is given for the meta data type
 */
public class Metadata {

    private String name;

    private long size;

    private String mimetype;

    private String filename;

    private String checksum;

    private String checksumType;

    private String path;

    private Source source;
    
    private String type;
    
    private boolean indexInline;
    
    private JsonNode jsonData;

    private String utcCreated;

    private String utcLastModified;

    public String getUtcCreated() {
        return utcCreated;
    }

    public void setUtcCreated(String utcCreated) {
        this.utcCreated = utcCreated;
    }

    public String getUtcLastModified() {
        return utcLastModified;
    }

    public void setUtcLastModified(String utcLastModified) {
        this.utcLastModified = utcLastModified;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public String getFilename() {
        if (filename == null || filename.isEmpty()) {
            return (name == null || name.isEmpty()) ? "metadata" : name;
        } else {
            return filename;
        }
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    
    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    
    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    
    /**
     * @return the source
     */
    public Source getSource() {
        return source;
    }

    
    /**
     * @param source the source to set
     */
    public void setSource(Source source) {
        this.source = source;
    }

    
    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    
    /**
     * @param size the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }

    
    /**
     * @return the checksum
     */
    public String getChecksum() {
        return checksum;
    }

    
    /**
     * @param checksum the checksum to set
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    
    /**
     * @return the checksumType
     */
    public String getChecksumType() {
        return checksumType;
    }

    
    /**
     * @param checksumType the checksumType to set
     */
    public void setChecksumType(String checksumType) {
        this.checksumType = checksumType;
    }

    
    /**
     * @return the indexInline
     */
    public boolean isIndexInline() {
        return indexInline;
    }

    
    /**
     * @param indexInline the indexInline to set
     */
    public void setIndexInline(boolean indexInline) {
        this.indexInline = indexInline;
    }

    
    /**
     * @return the jsonData
     */
    public JsonNode getJsonData() {
        return jsonData;
    }

    
    /**
     * @param jsonData the jsonData to set
     */
    public void setJsonData(JsonNode jsonData) {
        this.jsonData = jsonData;
    }

}
