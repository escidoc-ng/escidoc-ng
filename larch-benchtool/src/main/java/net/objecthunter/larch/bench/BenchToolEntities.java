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

package net.objecthunter.larch.bench;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.objecthunter.larch.bench.BenchTool.MdSize;
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.source.ByteArraySource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;

public abstract class BenchToolEntities {

    public static Entity createRandomEmptyEntity(String level2Id) {
        final Entity e = new Entity();
        e.setParentId(level2Id);
        e.setLabel("benchtool-" + RandomStringUtils.randomAlphabetic(16));
        e.setContentModelId(FixedContentModel.DATA.getName());
        return e;
    }

    public static Entity createRandomFullEntity(String level2Id, long dataSize, boolean indexInline) throws IOException {
        final Entity e = new Entity();
        e.setParentId(level2Id);
        e.setLabel("benchtool-" + RandomStringUtils.randomAlphabetic(16));
        e.setContentModelId(FixedContentModel.DATA.getName());
        //add metadata
        e.setMetadata(BenchToolEntities.createMetadataList(3, dataSize, indexInline));
        //add binary
        e.setBinaries(BenchToolEntities.createBinaryList(2, dataSize, indexInline));
        return e;
    }

    public static Metadata createRandomMetadata(MdSize size, boolean indexInline) throws IOException {
        String filename;
        switch (size) {
        case SMALL:
            filename = "dc.xml";
            break;
        case MEDIUM:
            filename = "md_medium.xml";
            break;
        case BIG:
            filename = "md_big.xml";
            break;
        default:
            throw new IllegalArgumentException("Unknown size '" + size + "'");
        }
        Metadata data = new Metadata();
        data.setIndexInline(indexInline);
        data.setMimetype("text/xml");
        data.setFilename(filename);
        data.setSource(new ByteArraySource(IOUtils.toByteArray(BenchToolEntities.class.getClassLoader().getResource("fixtures/" + filename).openStream())));
        data.setName("DC" + RandomStringUtils.randomAlphabetic(5));
        data.setType("DC");
        return data;
    }

    public static List<Metadata> createMetadataList(int size, long dataSize, boolean indexInline) throws IOException {
        //metadata-size is proportional to binary-size
        MdSize mdSize;
        if (dataSize < 100000) {
            mdSize = MdSize.SMALL;
        } else if (dataSize < 20000000) {
            mdSize = MdSize.MEDIUM;
        } else {
            mdSize = MdSize.BIG;
        }
        List<Metadata> metadataList = new ArrayList<Metadata>();
        for (int i = 0; i < size; i++) {
            Metadata md = createRandomMetadata(mdSize, indexInline);
            metadataList.add(md);
        }
        return metadataList;
    }

    public static Binary createRandomBinary(long size) throws IOException {
        Binary binary = new Binary();
        binary.setMimetype("application/octet-stream");
        binary.setName("binary-" + RandomStringUtils.randomAlphabetic(16));
        binary.setFilename(binary.getName() + ".bin");
        binary.setSource(new ByteArraySource(IOUtils.toByteArray(new RandomInputStream(size))));
        return binary;
    }

    public static List<Binary> createBinaryList(int size, long dataSize, boolean indexInline) throws IOException {
        List<Binary> binaryList = new ArrayList<Binary>();
        for (int i = 0; i < size; i++) {
            Binary bin = createRandomBinary(dataSize);
            bin.setMetadata(createMetadataList(2, dataSize, indexInline));
            binaryList.add(bin);
        }
        return binaryList;
    }

    public static Entity createLevel1Entity() {
        final Entity e = new Entity();
        e.setLabel("benchtool-" + RandomStringUtils.randomAlphabetic(16));
        e.setContentModelId(FixedContentModel.LEVEL1.getName());
        return e;
    }

    public static Entity createLevel2Entity(String level1Id) {
        final Entity e = new Entity();
        e.setParentId(level1Id);
        e.setLabel("benchtool-" + RandomStringUtils.randomAlphabetic(16));
        e.setContentModelId(FixedContentModel.LEVEL2.getName());
        return e;
    }

}
