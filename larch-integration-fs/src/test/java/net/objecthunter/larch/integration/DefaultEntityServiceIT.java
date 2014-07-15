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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package net.objecthunter.larch.integration;

import static net.objecthunter.larch.test.util.Fixtures.createFixtureCollectionEntity;
import static net.objecthunter.larch.test.util.Fixtures.createFixtureEntity;
import static net.objecthunter.larch.test.util.Fixtures.createSimpleFixtureEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.service.impl.DefaultEntityService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultEntityServiceIT extends AbstractLarchIT {

    @Autowired
    private DefaultEntityService entityService;

    @Test
    public void testCreateAndGetEntityAndContent() throws Exception {
        Entity e = createFixtureEntity();
        entityService.create(Workspace.DEFAULT, e);
        Entity fetched = entityService.retrieve(Workspace.DEFAULT, e.getId());
        assertEquals(e.getId(), fetched.getId());
        assertEquals(e.getLabel(), fetched.getLabel());
        assertEquals(e.getBinaries().size(), fetched.getBinaries().size());
        assertEquals(1, fetched.getVersion());
        fetched.getBinaries().values().forEach(b -> {
            assertNotNull(b.getChecksum());
            assertNotNull(b.getChecksumType());
            assertNotNull(b.getFilename());
            assertNotNull(b.getMimetype());
            assertNotNull(b.getPath());
            try (final InputStream src = entityService.getContent(Workspace.DEFAULT, e.getId(), b.getName())) {
                assertTrue(src.available() > 0);
            } catch (IOException e1) {
                fail("IOException: " + e1.getLocalizedMessage());
            }
        });
    }

    @Test
    public void testCreateAndGetEntityWithChildren() throws Exception {
        Entity e = createFixtureCollectionEntity();
        String parentId = entityService.create(Workspace.DEFAULT, e);
        for (int i = 0; i < 2; i++) {
            Entity child = createSimpleFixtureEntity();
            child.setParentId(parentId);
            entityService.create(Workspace.DEFAULT, child);
        }
        Entity fetched = entityService.retrieve(Workspace.DEFAULT, parentId);
        assertEquals(2, fetched.getChildren().size());
    }

    @Test
    public void testCreateAndUpdate() throws Exception {
        Entity e = createFixtureEntity();
        String id = entityService.create(Workspace.DEFAULT, e);
        Entity orig = entityService.retrieve(Workspace.DEFAULT, id);
        Entity update = createFixtureEntity();
        update.setId(id);
        update.setLabel("My updated label");
        entityService.update(Workspace.DEFAULT, update);
        Entity fetched = entityService.retrieve(Workspace.DEFAULT, e.getId());
        assertEquals(update.getLabel(), fetched.getLabel());
        assertEquals(orig.getUtcCreated(), fetched.getUtcCreated());
    }

    @Test
    public void testCreateUpdateAndFetchOldVersion() throws Exception {
        Entity e = createFixtureEntity();
        String id = entityService.create(Workspace.DEFAULT, e);
        Entity orig = entityService.retrieve(Workspace.DEFAULT, id);
        Entity update = createFixtureEntity();
        update.setId(id);
        update.setLabel("My updated label");
        entityService.update(Workspace.DEFAULT, update);
        Entity fetched = entityService.retrieve(Workspace.DEFAULT, e.getId(), 1);
        assertEquals(orig.getLabel(), fetched.getLabel());
        assertEquals(1, orig.getVersion());
        assertEquals(1, fetched.getVersion());
    }
}
