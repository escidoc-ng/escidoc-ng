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

import java.util.Map;

import net.objecthunter.larch.model.source.Source;

/**
 * A DTO for a larch repository object which can have binary data attached to it. The actual binary content is wrapped
 * in a Source object depending on it's location: For example a {@link net.objecthunter.larch.model.source .UrlSource}
 * for a location reachable by http: {@code http://example.com/image.jpg}
 */
public class Binary {

    private String name;

    private long size;

    private String mimetype;

    private Map<String, Metadata> metadata;

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

    public Map<String, Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Metadata> metadata) {
        this.metadata = metadata;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

}
