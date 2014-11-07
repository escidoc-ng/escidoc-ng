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

package net.objecthunter.larch.integration.entity;

import net.objecthunter.larch.integration.AbstractLarchIT;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.test.util.Fixtures;
import static net.objecthunter.larch.test.util.Fixtures.createFixtureEntity;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityControllerCreateIT extends AbstractLarchIT {

    private static final Logger log = LoggerFactory.getLogger(EntityControllerCreateIT.class);

    @Test
    public void testCreateEntityStatus() throws Exception {
        // Level1
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null);
        assertEquals(EntityState.PENDING, entity.getState());
        assertEquals(1, entity.getVersion());
        assertEquals(FixedContentModel.LEVEL1.getName(), entity.getContentModelId());
        assertEquals(null, entity.getParentId());
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.LEVEL1.getName(), null);
        assertEquals(EntityState.SUBMITTED, entity.getState());
        assertEquals(1, entity.getVersion());
        assertEquals(FixedContentModel.LEVEL1.getName(), entity.getContentModelId());
        assertEquals(null, entity.getParentId());
        entity = createEntity(EntityState.PUBLISHED, FixedContentModel.LEVEL1.getName(), null);
        assertEquals(EntityState.PUBLISHED, entity.getState());
        assertEquals(1, entity.getVersion());
        assertEquals(FixedContentModel.LEVEL1.getName(), entity.getContentModelId());
        assertEquals(null, entity.getParentId());
        entity = createEntity(EntityState.WITHDRAWN, FixedContentModel.LEVEL1.getName(), null);
        assertEquals(EntityState.WITHDRAWN, entity.getState());
        assertEquals(1, entity.getVersion());
        assertEquals(FixedContentModel.LEVEL1.getName(), entity.getContentModelId());
        assertEquals(null, entity.getParentId());
        entity = createFixtureEntity();
        entity.setContentModelId(FixedContentModel.LEVEL1.getName());
        entity.setParentId(null);
        entity.setState(null);
        entity = createEntity(entity, 201);
        assertEquals(EntityState.PENDING, entity.getState());
        
        // Level2
        Entity level1 = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null);
        entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL2.getName(), level1.getId());
        assertEquals(EntityState.PENDING, entity.getState());
        assertEquals(1, entity.getVersion());
        assertEquals(FixedContentModel.LEVEL2.getName(), entity.getContentModelId());
        assertEquals(level1.getId(), entity.getParentId());
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.LEVEL2.getName(), level1.getId());
        assertEquals(EntityState.SUBMITTED, entity.getState());
        assertEquals(1, entity.getVersion());
        assertEquals(FixedContentModel.LEVEL2.getName(), entity.getContentModelId());
        assertEquals(level1.getId(), entity.getParentId());
        entity = createEntity(EntityState.PUBLISHED, FixedContentModel.LEVEL2.getName(), level1.getId());
        assertEquals(EntityState.PUBLISHED, entity.getState());
        assertEquals(1, entity.getVersion());
        assertEquals(FixedContentModel.LEVEL2.getName(), entity.getContentModelId());
        assertEquals(level1.getId(), entity.getParentId());
        entity = createEntity(EntityState.WITHDRAWN, FixedContentModel.LEVEL2.getName(), level1.getId());
        assertEquals(EntityState.WITHDRAWN, entity.getState());
        assertEquals(1, entity.getVersion());
        assertEquals(FixedContentModel.LEVEL2.getName(), entity.getContentModelId());
        assertEquals(level1.getId(), entity.getParentId());
        entity = createFixtureEntity();
        entity.setContentModelId(FixedContentModel.LEVEL2.getName());
        entity.setParentId(Fixtures.LEVEL1_ID);
        entity.setState(null);
        entity = createEntity(entity, 201);
        assertEquals(EntityState.PENDING, entity.getState());

        // Data
        level1 = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null);
        Entity level2 = createEntity(EntityState.PENDING, FixedContentModel.LEVEL2.getName(), level1.getId());
        entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2.getId());
        assertEquals(EntityState.PENDING, entity.getState());
        assertEquals(1, entity.getVersion());
        assertEquals(FixedContentModel.DATA.getName(), entity.getContentModelId());
        assertEquals(level2.getId(), entity.getParentId());
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2.getId());
        assertEquals(EntityState.SUBMITTED, entity.getState());
        assertEquals(1, entity.getVersion());
        assertEquals(FixedContentModel.DATA.getName(), entity.getContentModelId());
        assertEquals(level2.getId(), entity.getParentId());
        entity = createEntity(EntityState.PUBLISHED, FixedContentModel.DATA.getName(), level2.getId());
        assertEquals(EntityState.PUBLISHED, entity.getState());
        assertEquals(1, entity.getVersion());
        assertEquals(FixedContentModel.DATA.getName(), entity.getContentModelId());
        assertEquals(level2.getId(), entity.getParentId());
        entity = createEntity(EntityState.WITHDRAWN, FixedContentModel.DATA.getName(), level2.getId());
        assertEquals(EntityState.WITHDRAWN, entity.getState());
        assertEquals(1, entity.getVersion());
        assertEquals(FixedContentModel.DATA.getName(), entity.getContentModelId());
        assertEquals(level2.getId(), entity.getParentId());
        entity = createFixtureEntity();
        entity.setState(null);
        entity = createEntity(entity, 201);
        assertEquals(EntityState.PENDING, entity.getState());
        
    }

    @Test
    public void testCreateEntityWrongContentModel() throws Exception {
        Entity e = createFixtureEntity();
        e.setContentModelId("wrong");
        createEntity(e, 404);

        e = createFixtureEntity();
        e.setContentModelId(null);
        createEntity(e, 400);

        e = createFixtureEntity();
        e.setParentId(Fixtures.LEVEL1_ID);
        createEntity(e, 400);

        e = createFixtureEntity();
        e.setParentId(null);
        createEntity(e, 400);

        e = createFixtureEntity();
        e.setContentModelId(FixedContentModel.LEVEL2.getName());
        createEntity(e, 400);

        e = createFixtureEntity();
        e.setContentModelId(FixedContentModel.LEVEL1.getName());
        createEntity(e, 400);
    }

}
