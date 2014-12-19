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

import java.util.List;

/**
 * @author mih Class holds attributes for a relation.
 */
public class Relation {

    private String predicate;

    private List<String> objects;

    public Relation() {

    }

    public Relation(String predicate, List<String> objects) {
        this.predicate = predicate;
        this.objects = objects;
    }

    
    /**
     * @return the predicate
     */
    public String getPredicate() {
        return predicate;
    }

    
    /**
     * @param predicate the predicate to set
     */
    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    
    /**
     * @return the object
     */
    public List<String> getObjects() {
        return objects;
    }

    
    /**
     * @param object the object to set
     */
    public void setObjects(List<String> objects) {
        this.objects = objects;
    }

    
}
