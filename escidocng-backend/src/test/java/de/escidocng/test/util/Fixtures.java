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

package de.escidocng.test.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;

import de.escidocng.model.Binary;
import de.escidocng.model.ContentModel;
import de.escidocng.model.Entity;
import de.escidocng.model.Metadata;
import de.escidocng.model.MetadataType;
import de.escidocng.model.Relation;
import de.escidocng.model.ContentModel.FixedContentModel;
import de.escidocng.model.Entity.EntityState;
import de.escidocng.model.security.User;
import de.escidocng.model.source.ByteArraySource;

public abstract class Fixtures {

    public static final String LEVEL2_ID = "level2-" + RandomStringUtils.randomAlphabetic(16);

    public static final String LEVEL1_ID = "level1-" + RandomStringUtils.randomAlphabetic(16);

    public static User createUser() {
        User u = new User();
        u.setName(RandomStringUtils.randomAlphabetic(12));
        u.setPwhash("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"); // sha256 hash for pw 'test'
        u.setFirstName("foo");
        u.setLastName("bar");
        u.setEmail("foo.bar@exmaple.com");
        return u;
    }

    public static Entity createLevel1() {
        Entity e = new Entity();
        e.setId(RandomStringUtils.randomAlphanumeric(16));
        e.setLabel("Test level1");
        e.setContentModelId(FixedContentModel.LEVEL1.getName());
        return e;
    }

    public static Entity createLevel2(String level1Id) {
        Entity e = new Entity();
        e.setId(RandomStringUtils.randomAlphanumeric(16));
        e.setLabel("Test level2");
        e.setContentModelId(FixedContentModel.LEVEL2.getName());
        e.setParentId(level1Id);
        return e;
    }

    public static ContentModel createContentModel() {
        ContentModel contentModel = new ContentModel();
        contentModel.setName("test");
        contentModel.setAllowedParentContentModels(new ArrayList<String>() {

            {
                add(FixedContentModel.LEVEL2.getName());
                add(FixedContentModel.DATA.getName());
            }
        });
        return contentModel;
    }

    public static Entity createEntity() throws Exception {
        Entity e = new Entity();
        e.setId("testid");
        e.setState(EntityState.PENDING);
        e.setLabel("Test label");
        e.setContentModelId(FixedContentModel.DATA.getName());
        e.setTags(Arrays.asList("tag1", "tag2"));
        e.setMetadata(createMetadataList());
        e.setBinaries(createBinaryList());
        e.setRelations(createRelations());
        e.setParentId(LEVEL2_ID);
        return e;
    }

    public static List<Relation> createRelations() {
        List<Relation> relations = new ArrayList<>();
        relations.add(new Relation("testpredicate", Arrays.asList("object1", "object2")));
        return relations;
    }

    public static List<Binary> createBinaryList() {
        List<Binary> bins = new ArrayList<>(1);
        Binary bin = createBinary();
        bins.add(bin);
        return bins;
    }

    public static Binary createBinary() {
        Binary bin = new Binary();
        bin.setSize(1);
        bin.setMimetype("text/plain");
        bin.setName("BINARY-1");
        bin.setPath("/path/to/testbinary");
        return bin;
    }

    public static List<Metadata> createMetadataList() throws Exception {
        List<Metadata> metadataList = new ArrayList<>(1);
        Metadata md = createMetadata();
        metadataList.add(md);
        return metadataList;
    }

    public static Metadata createMetadata() throws Exception {
        Metadata data = new Metadata();
        data.setMimetype("text/xml");
        data.setFilename("dc.xml");
        data.setSource(new ByteArraySource(IOUtils.toByteArray(Fixtures.class.getClassLoader().getResource("fixtures/dc.xml").openStream())));
        data.setName("DC");
        data.setType("DC");
        return data;
    }

    public static MetadataType createMetadataType() {
        MetadataType type = new MetadataType();
        type.setName("Dublin Core");
        type.setSchemaUrl("http://example.com");
        return type;
    }

    public static Entity createFixtureEntityWithRandomId() throws Exception {
        Entity e = createFixtureEntity(false);
        e.setId(RandomStringUtils.randomAlphabetic(16));
        return e;
    }

    public static Entity createFixtureEntity(boolean indexInline) throws Exception {
        Binary bin1 = new Binary();
        bin1.setMimetype("image/png");
        bin1.setFilename("image_1.png");
        bin1.setSource(new ByteArraySource(IOUtils.toByteArray(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").openStream())));
        bin1.setName("image-1");
        List<Metadata> bin1Md = new ArrayList<>();
        Metadata md = createRandomDCMetadata(indexInline);
        bin1Md.add(md);
        bin1.setMetadata(bin1Md);
        Binary bin2 = new Binary();
        bin2.setMimetype("image/png");
        bin2.setFilename("image_2.png");
        bin2.setSource(new ByteArraySource(IOUtils.toByteArray(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").openStream())));
        bin2.setName("image-2");
        List<Metadata> bin2Md = new ArrayList<>();
        md = createRandomDCMetadata(indexInline);
        bin2Md.add(md);
        bin2.setMetadata(bin2Md);
        List<Binary> binaries = new ArrayList<>();
        binaries.add(bin1);
        binaries.add(bin2);
        List<Metadata> metadata = new ArrayList<>();
        md = createRandomDCMetadata(indexInline);
        metadata.add(md);
        Entity e = new Entity();
        e.setState(EntityState.PENDING);
        e.setParentId(LEVEL2_ID);
        e.setLabel("My Label");
        e.setTags(Arrays.asList("test", "integration-test"));
        e.setContentModelId(FixedContentModel.DATA.getName());
        e.setBinaries(binaries);
        e.setMetadata(metadata);
        return e;
    }

    public static Metadata createRandomDCMetadata(boolean indexInline) throws Exception {
        Metadata data = new Metadata();
        data.setMimetype("text/xml");
        data.setFilename("dc.xml");
        data.setSource(new ByteArraySource(IOUtils.toByteArray(Fixtures.class.getClassLoader().getResource("fixtures/dc.xml").openStream())));
        data.setName("Dublin-Core-" + RandomStringUtils.randomAlphabetic(16));
        data.setType("DC");
        data.setIndexInline(indexInline);
        return data;
    }
    
    public static Binary createRandomBinary() throws Exception {
        Binary binary = new Binary();
        binary.setMimetype("image/png");
        binary.setFilename("image_2.png");
        binary.setSource(new ByteArraySource(IOUtils.toByteArray(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").openStream())));
        binary.setName("image-" + RandomStringUtils.randomAlphabetic(16));
        return binary;
    }

    public static Entity createSimpleFixtureEntity() throws Exception {
        Binary bin1 = new Binary();
        bin1.setMimetype("image/png");
        bin1.setFilename("image_1.png");
        bin1.setSource(new ByteArraySource(IOUtils.toByteArray(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").openStream())));
        bin1.setName("image-1");
        List<Binary> binaries = new ArrayList<>();
        binaries.add(bin1);
        Entity e = new Entity();
        e.setState(EntityState.PENDING);
        e.setLabel("My Label");
        e.setTags(Arrays.asList("test", "integration-test"));
        e.setContentModelId(FixedContentModel.DATA.getName());
        e.setBinaries(binaries);
        e.setParentId(LEVEL2_ID);
        return e;
    }

    public static Entity createFixtureEntityWithoutBinary() throws Exception {
        Entity e = new Entity();
        e.setLabel("My Label");
        e.setTags(Arrays.asList("test", "integration-test"));
        e.setContentModelId(FixedContentModel.DATA.getName());
        e.setParentId(LEVEL2_ID);
        return e;
    }

    public static Entity createFixtureCollectionEntity() throws Exception {
        Entity e = createSimpleFixtureEntity();
        e.setContentModelId(FixedContentModel.DATA.getName());
        e.setParentId(LEVEL2_ID);
        return e;
    }

    public static Binary createRandomImageBinary() throws Exception {
        Binary bin = new Binary();
        bin.setMimetype("image/png");
        bin.setFilename("image_1.png");
        bin.setSource(new ByteArraySource(IOUtils.toByteArray(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").openStream())));
        bin.setName(RandomStringUtils.randomAlphabetic(16));
        return bin;
    }

}
