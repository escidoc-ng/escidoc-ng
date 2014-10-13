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

package net.objecthunter.larch.service;

import java.io.IOException;

import net.objecthunter.larch.model.ContentModel;

/**
 * Service definition for CRUD operations on {@link net.objecthunter.larch.model.ContentModel} objects
 */
public interface ContentModelService {

    /**
     * retrieve ContentModel.
     * 
     * @param contentModelId
     * @return
     * @throws IOException
     */
    ContentModel retrieve(String contentModelId) throws IOException;
    
    /**
     * Create a new ContentModel
     * 
     * @param c
     * @return contentModelId
     * @throws IOException
     */
    String create(ContentModel c) throws IOException;
    
    /**
     * Delete ContentModel.
     * Check if unused, otherwise throw Exception.
     * 
     * @param id
     * @throws IOException
     */
    void delete(String id) throws IOException;
    
    

}