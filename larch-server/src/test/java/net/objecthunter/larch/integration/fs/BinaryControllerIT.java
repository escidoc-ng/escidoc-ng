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


package net.objecthunter.larch.integration.fs;

import static net.objecthunter.larch.test.util.Fixtures.LEVEL2_ID;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BinaryControllerIT extends AbstractLarchIT {

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void testCreateBinaryStream() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), LEVEL2_ID, false);
        // add binary
        entity = addBinaryStream(entity, IGNORE, IGNORE, IGNORE, 201);
        // add binary twice
        entity = addBinaryStream(entity, "distinct", IGNORE, IGNORE, 201);
        entity = addBinaryStream(entity, "distinct", IGNORE, IGNORE, 409);
        // add binary wrong mimetype
        entity = addBinaryStream(entity, IGNORE, "notsupported", IGNORE, 201);
        entity = addBinaryStream(entity, IGNORE, "text/xml", IGNORE, 201);
        // name null
        entity = addBinaryStream(entity, null, IGNORE, IGNORE, 400);
        // mimetype null
        entity = addBinaryStream(entity, IGNORE, null, IGNORE, 400);
        // resource null
        entity = addBinaryStream(entity, IGNORE, IGNORE, null, 500);
        // wrong entityId
        entity.setId("nonexistent");
        entity = addBinaryStream(entity, IGNORE, IGNORE, IGNORE, 404);
    }

    @Test
    public void testCreateBinaryMultipart() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), LEVEL2_ID, false);
        // add binary
        entity = addBinaryMultipart(entity, IGNORE, IGNORE, IGNORE, 201);
        // add binary twice
        entity = addBinaryMultipart(entity, "distinct", IGNORE, IGNORE, 201);
        entity = addBinaryMultipart(entity, "distinct", IGNORE, IGNORE, 409);
        // add binary wrong mimetype
        entity = addBinaryMultipart(entity, IGNORE, "notsupported", IGNORE, 201);
        entity = addBinaryMultipart(entity, IGNORE, "text/xml", IGNORE, 201);
        // name null
        entity = addBinaryMultipart(entity, null, IGNORE, IGNORE, 400);
        // resource null
        entity = addBinaryMultipart(entity, IGNORE, IGNORE, null, 400);
        // wrong entityId
        entity.setId("nonexistent");
        entity = addBinaryMultipart(entity, IGNORE, IGNORE, IGNORE, 404);
    }

    @Test
    public void testRetrieveBinary() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), LEVEL2_ID, false);
        // add binary
        entity = addBinaryMultipart(entity, "distinct", IGNORE, IGNORE, 201);
        // retrieve Binary
        retrieveBinary(entity, "distinct", 200);
        // retrieve Binary wrong name
        retrieveBinary(entity, "wrong", 404);
        // retrieve Binary null name
        retrieveBinary(entity, null, 404);
        // retrieve Binary wrong entityId
        entity.setId("nonexistent");
        retrieveBinary(entity, "distinct", 404);
    }

    @Test
    public void testDownloadBinaryContent() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), LEVEL2_ID, false);
        // add binary
        entity = addBinaryMultipart(entity, "distinct", IGNORE, IGNORE, 201);
        // download Binary Content
        downloadBinaryContent(entity, "distinct", 200);
        // download Binary Content wrong name
        downloadBinaryContent(entity, "wrong", 404);
        // download Binary Content null name
        downloadBinaryContent(entity, null, 404);
        // download Binary Content wrong entityId
        entity.setId("nonexistent");
        downloadBinaryContent(entity, "distinct", 404);
    }

    @Test
    public void testDeleteBinary() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), LEVEL2_ID, false);
        // add binary
        entity = addBinaryMultipart(entity, "distinct", IGNORE, IGNORE, 201);
        // delete Binary
        removeBinary(entity, "distinct", 200);
        // delete Binary wrong name
        removeBinary(entity, "wrong", 404);
        // delete Binary null name
        removeBinary(entity, null, 404);
        // delete Binary wrong entityId
        entity.setId("nonexistent");
        removeBinary(entity, "distinct", 404);
    }

}
