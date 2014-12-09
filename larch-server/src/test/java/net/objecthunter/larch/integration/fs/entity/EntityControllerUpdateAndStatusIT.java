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


package net.objecthunter.larch.integration.fs.entity;

import static org.junit.Assert.assertEquals;
import net.objecthunter.larch.integration.fs.AbstractFSLarchIT;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.test.util.Fixtures;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityControllerUpdateAndStatusIT extends AbstractFSLarchIT {

    private static final Logger log = LoggerFactory.getLogger(EntityControllerUpdateAndStatusIT.class);

    @Test
    public void testUpdateEntity() throws Exception {
        Entity entity = createEntity(EntityState.SUBMITTED, FixedContentModel.LEVEL1.getName(), null, false);
        entity = updateEntity(entity, 200);
        assertEquals(EntityState.SUBMITTED, entity.getState());
        assertEquals(2, entity.getVersion());
        assertEquals(FixedContentModel.LEVEL1.getName(), entity.getContentModelId());
        assertEquals(null, entity.getParentId());
        entity.setContentModelId(FixedContentModel.LEVEL2.getName());
        updateEntity(entity, 400);
        entity.setContentModelId(FixedContentModel.LEVEL1.getName());
        entity.setState(EntityState.PUBLISHED);
        updateEntity(entity, 400);
        entity.setState(EntityState.SUBMITTED);
        entity.setLabel("other");
        entity = updateEntity(entity, 200);
        assertEquals(EntityState.SUBMITTED, entity.getState());
        assertEquals(3, entity.getVersion());
        assertEquals(FixedContentModel.LEVEL1.getName(), entity.getContentModelId());
        assertEquals(null, entity.getParentId());
        entity.setId("nonexistent");
        updateEntity(entity, 404);
        
        entity = createEntity(EntityState.PUBLISHED, FixedContentModel.LEVEL2.getName(), Fixtures.LEVEL1_ID, false);
        updateEntity(entity, 400);
        
        entity = createEntity(EntityState.WITHDRAWN, FixedContentModel.LEVEL2.getName(), Fixtures.LEVEL1_ID, false);
        updateEntity(entity, 400);
    }

    @Test
    public void testSetEntityState() throws Exception {
        Entity entity = createEntity(EntityState.SUBMITTED, FixedContentModel.LEVEL1.getName(), null, false);
        assertEquals(EntityState.SUBMITTED, entity.getState());
        setEntityStatus("nonexistent", EntityState.PENDING, 404);
        setEntityStatus("nonexistent", EntityState.SUBMITTED, 404);
        setEntityStatus("nonexistent", EntityState.PUBLISHED, 404);
        setEntityStatus("nonexistent", EntityState.WITHDRAWN, 404);
        setEntityStatus(entity.getId(), EntityState.PENDING, 200);
        assertEquals(EntityState.PENDING, getEntityStatus(entity.getId()));
        setEntityStatus(entity.getId(), EntityState.PENDING, 200);
        assertEquals(EntityState.PENDING, getEntityStatus(entity.getId()));
        setEntityStatus(entity.getId(), EntityState.SUBMITTED, 200);
        assertEquals(EntityState.SUBMITTED, getEntityStatus(entity.getId()));
        updateEntity(entity, 200);
        setEntityStatus(entity.getId(), EntityState.PUBLISHED, 200);
        assertEquals(EntityState.PUBLISHED, getEntityStatus(entity.getId()));
        entity = retrieveEntity(entity.getId(), 200);
        assertEquals(2, entity.getVersion());
        setEntityStatus(entity.getId(), EntityState.WITHDRAWN, 200);
        assertEquals(EntityState.WITHDRAWN, getEntityStatus(entity.getId()));
        updateEntity(entity, 400);
        setEntityStatus(entity.getId(), EntityState.PUBLISHED, 200);
        assertEquals(EntityState.PUBLISHED, getEntityStatus(entity.getId()));
        updateEntity(entity, 400);
        setEntityStatus(entity.getId(), EntityState.SUBMITTED, 400);
        setEntityStatus(entity.getId(), EntityState.PENDING, 400);
        assertEquals(EntityState.PUBLISHED, getEntityStatus(entity.getId()));
        entity = retrieveEntity(entity.getId(), 200);
        assertEquals(2, entity.getVersion());
    }

}
