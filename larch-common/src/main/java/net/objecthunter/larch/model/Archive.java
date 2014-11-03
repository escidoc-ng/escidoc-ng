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

import java.time.ZonedDateTime;

public class Archive {

    private String entityId;

    private int entityVersion;

    private String path;

    private ZonedDateTime lastModifedDate;

    private ZonedDateTime createdDate;

    private String creator;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ZonedDateTime getLastModifedDate() {
        return lastModifedDate;
    }

    public void setLastModifedDate(ZonedDateTime lastModifedDate) {
        this.lastModifedDate = lastModifedDate;
    }

    public ZonedDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(ZonedDateTime createdDate) {
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
}
