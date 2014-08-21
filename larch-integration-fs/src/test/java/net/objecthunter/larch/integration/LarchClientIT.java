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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;

import net.objecthunter.larch.client.LarchClient;
import net.objecthunter.larch.model.*;
import net.objecthunter.larch.model.state.LarchState;
import net.objecthunter.larch.test.util.Fixtures;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static net.objecthunter.larch.test.util.Fixtures.*;

public class LarchClientIT extends AbstractLarchIT {

    private LarchClient client = new LarchClient(URI.create("http://localhost:8080"), "admin", "admin");

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testPostEntity() throws Exception {
        Entity e = Fixtures.createFixtureEntity();
        e.setId(RandomStringUtils.randomAlphabetic(16));
        client.postEntity(PERMISSION_ID, e);
    }

    @Test
    public void testRetrieveEntity() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        client.postEntity(PERMISSION_ID, e);
        Entity fetched = client.retrieveEntity(PERMISSION_ID, e.getId());
        assertEquals(e.getId(), fetched.getId());
        assertEquals(e.getLabel(), fetched.getLabel());
        assertEquals(e.getBinaries().size(), fetched.getBinaries().size());
    }

    @Test
    public void testRetrieveState() throws Exception {
        LarchState state = this.client.retrieveState();
        assertNotNull(state);
        assertNotNull(state.getIndexState().getName());
    }

    @Test
    public void testRetrieveDescribe() throws Exception {
        Describe desc = this.client.retrieveDescribe();
        assertNotNull(desc);
        assertEquals(1, desc.getEsNumDataNodes());
        assertNotNull(desc.getEsMasterNodeName());
    }

    @Test
    public void testRetrieveMetadata() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(PERMISSION_ID, e);
        Metadata orig = e.getMetadata().entrySet().iterator().next().getValue();
        Metadata md = this.client.retrieveMetadata(PERMISSION_ID, e.getId(), orig.getName());
        assertNotNull(md);
        assertEquals(orig.getType(), md.getType());
        assertEquals(orig.getName(), md.getName());
        assertNotNull(md.getUtcCreated());
        assertNotNull(md.getUtcLastModified());
    }

    @Test
    public void testRetrieveBinaryMetadata() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(PERMISSION_ID, e);
        Binary bin = e.getBinaries().entrySet().iterator().next().getValue();
        Metadata orig = bin.getMetadata().entrySet().iterator().next().getValue();
        Metadata md = this.client.retrieveBinaryMetadata(PERMISSION_ID, e.getId(), bin.getName(), orig.getName());
        assertNotNull(md);
        assertEquals(orig.getType(), md.getType());
        assertEquals(orig.getName(), md.getName());
        assertNotNull(md.getUtcCreated());
        assertNotNull(md.getUtcLastModified());
    }

    @Test
    public void testPostMetadata() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(PERMISSION_ID, e);
        Metadata md = Fixtures.createRandomDCMetadata();
        this.client.postMetadata(PERMISSION_ID, e.getId(), md);
    }

    @Test
    public void testPostBinaryMetadata() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(PERMISSION_ID, e);
        Binary bin = e.getBinaries().entrySet().iterator().next().getValue();
        Metadata binMd = Fixtures.createRandomDCMetadata();
        this.client.postBinaryMetadata(PERMISSION_ID, e.getId(), bin.getName(), binMd);
    }

    @Test
    public void testDeleteMetadata() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(PERMISSION_ID, e);
        Metadata md = Fixtures.createRandomDCMetadata();
        this.client.postMetadata(PERMISSION_ID, e.getId(), md);
        this.client.deleteMetadata(PERMISSION_ID, e.getId(), md.getName());
    }

    @Test
    public void testDeleteBinaryMetadata() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(PERMISSION_ID, e);
        Binary bin = e.getBinaries().entrySet().iterator().next().getValue();
        Metadata binMd = Fixtures.createRandomDCMetadata();
        this.client.postBinaryMetadata(PERMISSION_ID, e.getId(), bin.getName(), binMd);
        this.client.deleteBinaryMetadata(PERMISSION_ID, e.getId(), bin.getName(), binMd.getName());
    }

    @Test
    public void testRetrieveBinary() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(PERMISSION_ID, e);
        Binary orig = e.getBinaries().entrySet().iterator().next().getValue();
        Binary fetched = this.client.retrieveBinary(PERMISSION_ID, e.getId(), orig.getName());
    }

    @Test
    public void testPostBinary() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(PERMISSION_ID, e);
        Binary bin = Fixtures.createRandomImageBinary();
        this.client.postBinary(PERMISSION_ID, e.getId(), bin);
    }

    @Test
    public void testDeleteBinary() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(PERMISSION_ID, e);
        Binary bin = e.getBinaries().entrySet().iterator().next().getValue();
        this.client.deleteBinary(PERMISSION_ID, e.getId(), bin.getName());
    }

    @Test
    public void testRetrieveBinaryContent() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(PERMISSION_ID, e);
        Binary bin = e.getBinaries().entrySet().iterator().next().getValue();
        InputStream src = this.client.retrieveBinaryContent(PERMISSION_ID, e.getId(), bin.getName());
        File target = new File(tempFolder.getRoot(), RandomStringUtils.randomAlphabetic(16));
        try (FileOutputStream sink = new FileOutputStream(target)) {
            IOUtils.copy(src, sink);
        }
        assertTrue(target.length() > 100);
    }

    @Test
    public void testUpdateEntity() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(PERMISSION_ID, e);
        e.setLabel("new label");
        this.client.updateEntity(PERMISSION_ID, e);
        Entity updated = this.client.retrieveEntity(PERMISSION_ID, e.getId());
        assertEquals(e.getLabel(), updated.getLabel());
    }

    @Test
    public void testDeleteEntity() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(PERMISSION_ID, e);
        this.client.deleteEntity(PERMISSION_ID, e.getId());
    }
}
