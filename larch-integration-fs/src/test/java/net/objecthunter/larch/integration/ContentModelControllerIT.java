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

package net.objecthunter.larch.integration;


import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ContentModelControllerIT extends AbstractLarchIT {

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void testCreateContentModel() throws Exception {
        // create content model
        createContentModel(IGNORE, 201);
        // create existing content model
        createContentModel("distinct", 201);
        createContentModel("distinct", 409);
    }

    @Test
    public void testRetrieveContentModel() throws Exception {
        // create content model
        String contentModelId = createContentModel(IGNORE, 201);
        // retrieve content model
        retrieveContentModel(contentModelId, 200);
        // retrieve non-existent content model
        retrieveContentModel("nonexistent", 404);
        // retrieve content model with id = null
        retrieveContentModel(null, 404);
    }

    @Test
    public void testDeleteContentModel() throws Exception {
        // create content model
        String contentModelId = createContentModel(IGNORE, 201);
        // delete content model
        deleteContentModel(contentModelId, 200);
        // delete non-existent content model
        deleteContentModel("nonexistent", 404);
        // delete content model with id = null
        deleteContentModel(null, 404);
    }

}
