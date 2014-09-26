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

/**
 * DTO class for the type of a Metadata object
 */
public class MetadataType {

    private String name;

    private String schemaUrl;

    public MetadataType(String name, String schemaUrl) {
        this.name = name;
        this.schemaUrl = schemaUrl;
    }

    public MetadataType() {
    }

    /**
     * Get the name of the metadata type
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the metadata type
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the schema URL from the meta data type
     * 
     * @return the schema url
     */
    public String getSchemaUrl() {
        return schemaUrl;
    }

    /**
     * Set the schema URL for the meta data type
     * 
     * @param schemaUrl the url to set
     */
    public void setSchemaUrl(String schemaUrl) {
        this.schemaUrl = schemaUrl;
    }
}
