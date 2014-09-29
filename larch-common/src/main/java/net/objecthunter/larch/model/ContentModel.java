/*
 * Copyright 2014 Michael Hoppe
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

import java.util.List;

/**
 * A DTO for wrapping content model data of a larch repository object.
 */
public class ContentModel {

    private String id;

    private String name;
    
    private List<String> allowedParentContentModels;
    
    
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    
    /**
     * @return the allowedParentContentModels
     */
    public List<String> getAllowedParentContentModels() {
        return allowedParentContentModels;
    }

    
    /**
     * @param allowedParentContentModels the allowedParentContentModels to set
     */
    public void setAllowedParentContentModels(List<String> allowedParentContentModels) {
        this.allowedParentContentModels = allowedParentContentModels;
    }

    public enum FixedContentModel {
        LEVEL1("level1"),
        LEVEL2("level2"),
        DATA("data");
        
        private final String name;
        
        FixedContentModel(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
        
    }

    
}
