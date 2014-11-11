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
 * Model class to hold version information
 */
public class Version {

    private String entityId;

    private int versionNumber;

    private String path;

    public Version() {
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getPath() {
        return path;
    }
}
