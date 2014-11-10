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

package net.objecthunter.larch.test.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.ContentModel;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.MetadataType;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.source.UrlSource;

import org.apache.commons.lang3.RandomStringUtils;

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

    public static Entity createEntity() {
        Entity e = new Entity();
        e.setId("testid");
        e.setState(EntityState.PENDING);
        e.setLabel("Test label");
        e.setContentModelId(FixedContentModel.DATA.getName());
        e.setTags(Arrays.asList("tag1", "tag2"));
        e.setMetadata(createMetadataMap());
        e.setBinaries(createBinaryMap());
        e.setRelations(createRelations());
        e.setParentId(LEVEL2_ID);
        return e;
    }

    public static Map<String, List<String>> createRelations() {
        Map<String, List<String>> relations = new HashMap<>();
        relations.put("testpredicate", Arrays.asList("object1", "object2"));
        return relations;
    }

    public static Map<String, Binary> createBinaryMap() {
        Map<String, Binary> bins = new HashMap<>(1);
        Binary bin = createBinary();
        bins.put(bin.getName(), bin);
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

    public static Map<String, Metadata> createMetadataMap() {
        Map<String, Metadata> metadataMap = new HashMap<>(1);
        Metadata md = createMetadata();
        metadataMap.put(md.getName(), md);
        return metadataMap;
    }

    public static Metadata createMetadata() {
        Metadata data = new Metadata();
        data.setName("DC");
        data.setType("Dublin Core");
        data.setData("<empty/>");
        return data;
    }

    public static MetadataType createMetadataType() {
        MetadataType type = new MetadataType();
        type.setName("Dublin Core");
        type.setSchemaUrl("http://example.com");
        return type;
    }

    public static Entity createFixtureEntityWithRandomId() throws Exception {
        Entity e = createFixtureEntity();
        e.setId(RandomStringUtils.randomAlphabetic(16));
        return e;
    }

    public static Entity createFixtureEntity() throws Exception {
        Binary bin1 = new Binary();
        bin1.setMimetype("image/png");
        bin1.setFilename("image_1.png");
        bin1.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").toURI()));
        bin1.setName("image-1");
        Map<String, Metadata> bin1Md = new HashMap<>();
        Metadata md = createRandomDCMetadata();
        bin1Md.put(md.getName(), md);
        bin1.setMetadata(bin1Md);
        Binary bin2 = new Binary();
        bin2.setMimetype("image/png");
        bin2.setFilename("image_2.png");
        bin2.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").toURI()));
        bin2.setName("image-2");
        Map<String, Metadata> bin2Md = new HashMap<>();
        md = createRandomDCMetadata();
        bin2Md.put(md.getName(), md);
        bin2.setMetadata(bin2Md);
        Map<String, Binary> binaries = new HashMap<>();
        binaries.put(bin1.getName(), bin1);
        binaries.put(bin2.getName(), bin2);
        Map<String, Metadata> metadata = new HashMap<>();
        md = createRandomDCMetadata();
        metadata.put(md.getName(), md);
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

    public static Metadata createRandomDCMetadata() {
        Metadata md = new Metadata();
        md.setType("DC");
        md.setName("Dublin-Core-" + RandomStringUtils.randomAlphabetic(16));
        StringBuilder dcBuilder = new StringBuilder();
        dcBuilder
                .append(
                        "<metadata xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:dc=\"http://purl"
                                + ".org/dc/elements/1.1/\">").append("\n\t<dc:title>Test Object</dc:title>")
                .append("\n\t<dc:creator>fasseg</dc:creator>").append("\n\t<dc:subject>Testing Groven</dc:subject>")
                .append("\n\t<dc:description>Test Object to implement integration Tests</dc:description>")
                .append("\n</metadata>");
        md.setData(dcBuilder.toString());
        return md;
    }
    
    public static Binary createRandomBinary() throws Exception {
        Binary binary = new Binary();
        binary.setMimetype("image/png");
        binary.setFilename("image_2.png");
        binary.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").toURI()));
        binary.setName("image-" + RandomStringUtils.randomAlphabetic(16));
        return binary;
    }

    public static Entity createSimpleFixtureEntity() throws Exception {
        Binary bin1 = new Binary();
        bin1.setMimetype("image/png");
        bin1.setFilename("image_1.png");
        bin1.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").toURI()));
        bin1.setName("image-1");
        Map<String, Binary> binaries = new HashMap<>();
        binaries.put(bin1.getName(), bin1);
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
        bin.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").toURI()));
        bin.setName(RandomStringUtils.randomAlphabetic(16));
        return bin;
    }

}
