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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.model.source.UrlSource;
import net.objecthunter.larch.service.impl.DefaultEntityService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultWeedFsEntityServiceIT extends AbstractWeedFsLarchIT {

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
        // assertNotEquals(orig.getUtcLastModified(), fetched.getUtcLastModified());
        // assertEquals(orig.getUtcCreated(), fetched.getUtcCreated());
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
        // assertEquals(orig.getUtcLastModified(), fetched.getUtcLastModified());
        // assertEquals(orig.getUtcCreated(), fetched.getUtcCreated());
        assertEquals(1, orig.getVersion());
        assertEquals(1, fetched.getVersion());
    }

    private Entity createFixtureEntity() throws Exception {
        Binary bin1 = new Binary();
        bin1.setMimetype("image/png");
        bin1.setFilename("image_1.png");
        bin1.setSource(new UrlSource(this.getClass().getClassLoader().getResource("fixtures/image_1.png").toURI()));
        bin1.setName("image-1");
        Binary bin2 = new Binary();
        bin2.setMimetype("image/png");
        bin2.setFilename("image_2.png");
        bin2.setSource(new UrlSource(this.getClass().getClassLoader().getResource("fixtures/image_1.png").toURI()));
        bin2.setName("image-2");
        Map<String, Binary> binaries = new HashMap<>();
        binaries.put(bin1.getName(), bin1);
        binaries.put(bin2.getName(), bin2);
        Entity e = new Entity();
        e.setLabel("My Label");
        e.setTags(Arrays.asList("test", "integration-test"));
        e.setType("Book");
        e.setBinaries(binaries);
        e.setWorkspaceId(Workspace.DEFAULT);
        return e;
    }
}
