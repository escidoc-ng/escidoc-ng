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

import java.util.ArrayList;
import java.util.List;

/**
 * Object holds a List of Entity-Objects.
 */
public class Entities {

    private List<Entity> entities = new ArrayList<Entity>();

    /**
     * @return the entities
     */
    public List<Entity> getEntities() {
        return entities;
    }

    /**
     * @param entities the entities to set
     */
    public void setEntities(List<Entity> entities) {
        if (entities == null) {
            this.entities = new ArrayList<Entity>();
        }
        else {
            this.entities = entities;
        }
    }

}
