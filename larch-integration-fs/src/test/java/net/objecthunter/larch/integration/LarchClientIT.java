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

import net.objecthunter.larch.client.LarchClient;
import net.objecthunter.larch.integration.helpers.Fixtures;
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Describe;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.state.LarchState;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

public class LarchClientIT extends AbstractLarchIT {

    private LarchClient client = new LarchClient();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testPostEntity() throws Exception {
        Entity e = Fixtures.createFixtureEntity();
        e.setId(RandomStringUtils.randomAlphabetic(16));
        client.postEntity(e);
    }

    @Test
    public void testRetrieveEntity() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        client.postEntity(e);
        Entity fetched = client.retrieveEntity(e.getId());
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
        this.client.postEntity(e);
        Metadata orig = e.getMetadata().entrySet().iterator().next().getValue();
        Metadata md = this.client.retrieveMetadata(e.getId(), orig.getName());
        assertNotNull(md);
        assertEquals(orig.getType(), md.getType());
        assertEquals(orig.getName(), md.getName());
        assertNotNull(md.getUtcCreated());
        assertNotNull(md.getUtcLastModified());
    }

    @Test
    public void testRetrieveBinaryMetadata() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(e);
        Binary bin = e.getBinaries().entrySet().iterator().next().getValue();
        Metadata orig = bin.getMetadata().entrySet().iterator().next().getValue();
        Metadata md = this.client.retrieveBinaryMetadata(e.getId(), bin.getName(), orig.getName());
        assertNotNull(md);
        assertEquals(orig.getType(), md.getType());
        assertEquals(orig.getName(), md.getName());
        assertNotNull(md.getUtcCreated());
        assertNotNull(md.getUtcLastModified());
    }

    @Test
    public void testPostMetadata() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(e);
        Metadata md = Fixtures.createRandomDCMetadata();
        this.client.postMetadata(e.getId(), md);
    }

    @Test
    public void testPostBinaryMetadata() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(e);
        Binary bin = e.getBinaries().entrySet().iterator().next().getValue();
        Metadata binMd = Fixtures.createRandomDCMetadata();
        this.client.postBinaryMetadata(e.getId(), bin.getName(), binMd);
    }

    @Test
    public void testDeleteMetadata() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(e);
        Metadata md = Fixtures.createRandomDCMetadata();
        this.client.postMetadata(e.getId(), md);
        this.client.deleteMetadata(e.getId(), md.getName());
    }

    @Test
    public void testDeleteBinaryMetadata() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(e);
        Binary bin = e.getBinaries().entrySet().iterator().next().getValue();
        Metadata binMd = Fixtures.createRandomDCMetadata();
        this.client.postBinaryMetadata(e.getId(), bin.getName(), binMd);
        this.client.deleteBinaryMetadata(e.getId(), bin.getName(), binMd.getName());
    }

    @Test
    public void testRetrieveBinary() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(e);
        Binary orig = e.getBinaries().entrySet().iterator().next().getValue();
        Binary fetched = this.client.retrieveBinary(e.getId(), orig.getName());
    }

    @Test
    public void testPostBinary() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(e);
        Binary bin = Fixtures.createRandomImageBinary();
        this.client.postBinary(e.getId(), bin);
    }

    @Test
    public void testDeleteBinary() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(e);
        Binary bin = e.getBinaries().entrySet().iterator().next().getValue();
        this.client.deleteBinary(e.getId(), bin.getName());
    }

    @Test
    public void testRetrieveBinaryContent() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(e);
        Binary bin = e.getBinaries().entrySet().iterator().next().getValue();
        InputStream src = this.client.retrieveBinaryContent(e.getId(), bin.getName());
        File target = new File(tempFolder.getRoot(), RandomStringUtils.randomAlphabetic(16));
        try (FileOutputStream sink = new FileOutputStream(target)) {
            IOUtils.copy(src, sink);
        }
        assertTrue(target.length() > 100);
    }

    @Test
    public void testUpdateEntity() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(e);
        e.setLabel("new label");
        this.client.updateEntity(e);
        Entity updated = this.client.retrieveEntity(e.getId());
        assertEquals(e.getLabel(), updated.getLabel());
    }

    @Test
    public void testDeleteEntity() throws Exception {
        Entity e = Fixtures.createFixtureEntityWithRandomId();
        this.client.postEntity(e);
        this.client.deleteEntity(e.getId());
    }
}
