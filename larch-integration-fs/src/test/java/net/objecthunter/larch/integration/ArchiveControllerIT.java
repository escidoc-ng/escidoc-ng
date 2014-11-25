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
package net.objecthunter.larch.integration;

import net.objecthunter.larch.model.Archive;
import net.objecthunter.larch.model.ContentModel;
import net.objecthunter.larch.model.Entity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.Test;

import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static net.objecthunter.larch.test.util.Fixtures.LEVEL2_ID;
import static net.objecthunter.larch.test.util.Fixtures.createFixtureEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ArchiveControllerIT extends AbstractLarchIT {

    @Test
    public void testArchive() throws Exception {
        Entity e = this.ingestAndArchive(createFixtureEntity(false));
    }

    @Test
    public void testArchiveAndRetrieve() throws Exception {
        Entity e = this.ingestAndArchive(createFixtureEntity(false));
        Archive a = this.retrieveArchive(e.getId(), e.getVersion(), 200);
        assertNotNull(a);
        assertEquals(e.getId(), a.getEntityId());
        assertEquals(e.getVersion(), a.getEntityVersion());
    }

    @Test
    public void testArchiveNonExisting() throws Exception {
        HttpResponse resp = this.executeAsAdmin(Request.Put(hostUrl + "/archive/foo_entity_NON_EXISTANT/1"));
        assertEquals(404, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrieveNonExisting() throws Exception {
        this.retrieveArchive("FOO_NO_EXIST", -1, 404);
    }

    @Test
    public void testArchiveAndRetrieveNonExistingVersion() throws Exception {
        Entity e = createFixtureEntity(false);
        this.ingestAndArchive(e);
        this.retrieveArchive(e.getId(), Integer.MIN_VALUE, 404);
    }

    @Test
    public void testRetrieveArchiveContent() throws Exception {
        Entity e = this.ingestAndArchive(createFixtureEntity(false));
        ZipInputStream zip = this.retrieveContent(e.getId(), e.getVersion(), 200);
        //try to read the zip file
        assertTrue(zip.available() > 0);
        ZipEntry entry = zip.getNextEntry();
        assertNotNull(entry);
        assertNotNull(entry.getName());
        zip.close();
    }

    @Test
    public void testArchiveWithChildren() throws Exception {
        Entity parent = this.createEntity(Entity.EntityState.PENDING, ContentModel.FixedContentModel.DATA.getName(), LEVEL2_ID, false);
        Entity child_1 = this.createEntity(Entity.EntityState.PENDING, ContentModel.FixedContentModel.DATA.getName(), parent.getId(), false);
        Entity child_1_1 = this.createEntity(Entity.EntityState.PENDING, ContentModel.FixedContentModel.DATA.getName(), child_1.getId(), false);
        Entity child_2 = this.createEntity(Entity.EntityState.PENDING, ContentModel.FixedContentModel.DATA.getName(), parent.getId(), false);
        Entity archived = this.archive(parent);
    }
}
