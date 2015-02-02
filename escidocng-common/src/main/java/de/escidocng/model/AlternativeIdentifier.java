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

import java.io.IOException;

/**
 * @author mih Class holds attributes for alternative identifiers.
 */
public class AlternativeIdentifier {

    // type of the identifier
    private String type;

    // value of the identifier
    private String value;

    public AlternativeIdentifier() {

    }

    public AlternativeIdentifier(String type, String value) throws IOException {
        IdentifierType.valueOf(type);
        this.type = type;
        this.value = value;
    }

    /**
     * @return IdentifierType
     */
    public String getType() {
        return type;
    }

    /**
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        IdentifierType.valueOf(type);
        this.type = type;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * List of supported Identifier-Types.
     */
    public enum IdentifierType {
        DOI("DOI"), URN("URN");

        public final String name;

        IdentifierType(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }

    }

}
