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

/**
 * A DTO for wrapping arbitrary meta data of a larch repository object. The meta data can only be validated if it's in
 * XML format and a schemaUrl is given for the meta data type
 */
public class Metadata {

    private String name;

    private String type;

    private String data;

    private String mimetype;

    private String originalFilename;

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

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
