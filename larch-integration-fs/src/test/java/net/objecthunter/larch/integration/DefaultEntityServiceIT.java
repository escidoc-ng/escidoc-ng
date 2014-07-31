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
import java.util.ArrayList;
import java.util.List;

import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.model.AuditRecords;
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entities;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchVersionService;
import net.objecthunter.larch.service.impl.DefaultEntityService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultEntityServiceIT extends AbstractLarchIT {

    @Autowired
    private DefaultEntityService entityService;

    @Autowired
    private ElasticSearchVersionService versionService;

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

    @Test
    public void testDeleteEntity() throws Exception {
        // create hierarchy
        String id = null;
        List<String> ids = new ArrayList<String>();
        List<String> binaryPaths = new ArrayList<String>();
        String parentId = null;
        for (int i = 0; i < 5; i++) {
            Entity child = createFixtureEntity();
            child.setParentId(id);
            String newId = entityService.create(child);
            if (id == null) {
                parentId = newId;
            }
            id = newId;
            ids.add(newId);
            child = entityService.retrieve(newId);
            for (Binary binary : child.getBinaries().values()) {
                binaryPaths.add(binary.getPath());
            }
        }

        // check Binaries
        for (String binaryPath : binaryPaths) {
            InputStream in = entityService.retrieveBinary(binaryPath);
            assertNotNull(in);
        }

        // delete parent
        entityService.delete(parentId);

        // Check Audit-Records
        for (String checkId : ids) {
            AuditRecords auditRecords = entityService.retrieveAuditRecords(checkId, 0, 100);
            assertNotNull(auditRecords);
            assertEquals(0, auditRecords.getAuditRecords().size());
        }
        // Check Versions
        for (String checkId : ids) {
            Entities entities = entityService.getOldVersions(checkId);
            assertNotNull(entities);
            assertEquals(0, entities.getEntities().size());
        }
        // Check Binaries
        for (String binaryPath : binaryPaths) {
            try {
                entityService.retrieveBinary(binaryPath);
            } catch (NotFoundException e) {
                continue;
            }
            throw new Exception("Binary with path " + binaryPath + " was found after delete");
        }
        // Check Entities
        for (String checkId : ids) {
            try {
                entityService.retrieve(checkId);
            } catch (NotFoundException e) {
                continue;
            }
            throw new Exception("Entity with id " + checkId + " was found after delete");
        }

    }

    @Test
    public void testDeleteBinary() throws Exception {
        List<String> binaryPaths = new ArrayList<String>();
        // create entity
        Entity entity = createFixtureEntity();
        String newId = entityService.create(entity);

        // retrieve entity
        entity = entityService.retrieve(newId);
        assertNotNull(entity.getBinaries());
        assertEquals(2, entity.getBinaries().size());
        for (Binary binary : entity.getBinaries().values()) {
            binaryPaths.add(binary.getPath());
        }
        String name = entity.getBinaries().keySet().iterator().next();

        // delete binary
        assertNotNull(entityService.retrieveBinary(entity.getBinaries().get(name).getPath()));
        entityService.deleteBinary(newId, name);

        // retrieve entity
        entity = entityService.retrieve(newId);
        assertNotNull(entity.getBinaries());
        assertEquals(1, entity.getBinaries().size());
        name = entity.getBinaries().keySet().iterator().next();

        // delete binary
        assertNotNull(entityService.retrieveBinary(entity.getBinaries().get(name).getPath()));
        entityService.deleteBinary(newId, name);

        // retrieve entity
        entity = entityService.retrieve(newId);
        assertNotNull(entity.getBinaries());
        assertEquals(0, entity.getBinaries().size());

        // Check Binaries
        for (String binaryPath : binaryPaths) {
            try {
                entityService.retrieveBinary(binaryPath);
            } catch (NotFoundException e) {
                continue;
            }
            throw new Exception("Binary with path " + binaryPath + " was found after delete");
        }
    }

    @Test
    public void testDeleteMetadata() throws Exception {
        // create entity
        Entity entity = createFixtureEntity();
        String newId = entityService.create(entity);

        // retrieve entity
        entity = entityService.retrieve(newId);
        assertNotNull(entity.getMetadata());
        assertEquals(1, entity.getMetadata().size());
        String name = entity.getMetadata().keySet().iterator().next();

        // delete metadata
        entityService.deleteMetadata(newId, name);

        // retrieve entity
        entity = entityService.retrieve(newId);
        assertNotNull(entity.getMetadata());
        assertEquals(0, entity.getMetadata().size());
    }

    @Test
    public void testDeleteBinaryMetadata() throws Exception {
        // create entity
        Entity entity = createFixtureEntity();
        String newId = entityService.create(entity);

        // retrieve entity
        entity = entityService.retrieve(newId);
        assertNotNull(entity.getBinaries());
        assertEquals(2, entity.getBinaries().size());
        String name = entity.getBinaries().keySet().iterator().next();
        assertNotNull(entity.getBinaries().get(name).getMetadata());
        assertEquals(1, entity.getBinaries().get(name).getMetadata().size());
        String mdName = entity.getBinaries().get(name).getMetadata().keySet().iterator().next();

        // delete binary metadata
        assertNotNull(entityService.retrieveBinary(entity.getBinaries().get(name).getPath()));
        entityService.deleteBinaryMetadata(newId, name, mdName);

        // retrieve entity
        entity = entityService.retrieve(newId);
        assertNotNull(entity.getBinaries().get(name).getMetadata());
        assertEquals(0, entity.getBinaries().get(name).getMetadata().size());
    }

}
