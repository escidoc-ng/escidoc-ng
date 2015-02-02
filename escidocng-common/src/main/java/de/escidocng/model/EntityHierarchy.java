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


/**
 * Objects holds the hierarchy-information for an entity
 */
public class EntityHierarchy {

    private String level1Id;

    private String level2Id;

    
    /**
     * @return the level1Id
     */
    public String getLevel1Id() {
        return level1Id;
    }

    
    /**
     * @param level1Id the level1Id to set
     */
    public void setLevel1Id(String level1Id) {
        this.level1Id = level1Id;
    }

    
    /**
     * @return the level2Id
     */
    public String getLevel2Id() {
        return level2Id;
    }

    
    /**
     * @param level2Id the level2Id to set
     */
    public void setLevel2Id(String level2Id) {
        this.level2Id = level2Id;
    }

}
