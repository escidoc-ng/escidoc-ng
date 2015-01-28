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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import net.objecthunter.larch.bench.BenchTool.MdSize;
import net.objecthunter.larch.client.LarchClient;
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ActionWorker implements Callable<BenchToolResult> {

    private static final Logger log = LoggerFactory.getLogger(ActionWorker.class);

    private final BenchTool.Action action;

    private final long size;

    private final LarchClient larchClient;

    private final ObjectMapper mapper = new ObjectMapper();

    private final String level1Id;

    private final String level2Id;

    protected ActionWorker(BenchTool.Action action, long size, LarchClient larchClient, String level1Id, String level2Id) {
        this.action = action;
        this.size = size;
        this.larchClient = larchClient;
        this.level1Id = level1Id;
        this.level2Id = level2Id;
    }

    @Override
    public BenchToolResult call() throws Exception {
        switch (this.action) {
        case CREATE_ENTITY:
            return createEntity(false);
        case CREATE_INDEXED_ENTITY:
            return createEntity(true);
        case CREATE_ENTITY_PARTED:
            return createEntityParted(false);
        case CREATE_INDEXED_ENTITY_PARTED:
            return createEntityParted(true);
        case RETRIEVE_ENTITY:
            return retrieveEntity(false);
        case RETRIEVE_INDEXED_ENTITY:
            return retrieveEntity(true);
        case UPDATE_ENTITY:
            return updateEntity(false);
        case UPDATE_INDEXED_ENTITY:
            return updateEntity(true);
        case DELETE_ENTITY:
            return deleteEntity(false);
        case DELETE_INDEXED_ENTITY:
            return deleteEntity(true);
        case CREATE_BINARY:
            return createBinary();
        case CREATE_BINARY_MULTIPART:
            return createBinaryMultipart();
        case CREATE_METADATA:
            return createMetadata(false);
        case CREATE_INDEXED_METADATA:
            return createMetadata(true);
        case RETRIEVE_BINARY:
            return retrieveBinary();
        case RETRIEVE_METADATA:
            return retrieveMetadata();
        case AUTH_CREATE_ENTITY:
            return authCreateEntity();
        case AUTH_RETRIEVE_ENTITY:
            return authRetrieveEntity();
        default:
            throw new IllegalArgumentException("Unknown action '" + this.action + "'");
        }
    }

    private BenchToolResult deleteEntity(boolean indexInline) throws IOException {
        /* create an entity */
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, indexInline);
        final String entityId = this.larchClient.postEntity(e);

        /* add a binary */
        final Binary binary = BenchToolEntities.createRandomBinary(size);
        this.larchClient.postBinary(entityId, binary);

        /* measure the deletion duration */
        long time = System.currentTimeMillis();
        this.larchClient.deleteEntity(entityId);
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }

    private BenchToolResult updateEntity(boolean indexInline) throws IOException {
        /* create an entity */
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, indexInline);
        String entityJson = mapper.writeValueAsString(e);
        final String entityId = this.larchClient.postEntity(entityJson);

        /* measure the update duration */
        e.setLabel("updated label");
        e.setId(entityId);
        Binary bin = BenchToolEntities.createRandomBinary(size);
        bin.setMetadata(BenchToolEntities.createMetadataList(2, size, indexInline));
        e.getBinaries().add(bin);
        entityJson = mapper.writeValueAsString(e);
        
        long time = System.currentTimeMillis();
        this.larchClient.updateEntity(entityId, entityJson);
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }

    private BenchToolResult retrieveEntity(boolean indexInline) throws IOException {
        /* create an entity */
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, indexInline);
        String entityJson = mapper.writeValueAsString(e);
        final String entityId = this.larchClient.postEntity(entityJson);

        /* measure the retrieval duration */
        long time = System.currentTimeMillis();
        IOUtils.toString(this.larchClient.retrieveEntityAsStream(entityId));
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }

    private BenchToolResult authRetrieveEntity() throws IOException {
    	String password = "passwd";
    	List<String> level2Ids = new ArrayList<String>();
    	level2Ids.add(level2Id);
    	for (int i = 0; i < 10; i++) {
            level2Ids.add(larchClient.postEntity(BenchToolEntities.createLevel2Entity(level1Id)));
		}
    	String username = larchClient.createUser(null, password, level2Ids);
    	String entityId = larchClient.postEntity(BenchToolEntities.createRandomFullEntity(level2Id, size, true));

        Object[] results = this.larchClient.executeAsUser("GET", "/entity/" + entityId, null, username, password);
        return new BenchToolResult(size, (long)results[1]);
    }
    
    private BenchToolResult createEntity(boolean indexInline) throws IOException {
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, indexInline);
        String entityJson = mapper.writeValueAsString(e);

        long time = System.currentTimeMillis();
        this.larchClient.postEntity(entityJson);
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }
    
    private BenchToolResult createEntityParted(boolean indexInline) throws IOException {
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, indexInline);

        long time = System.currentTimeMillis();
        this.larchClient.postEntityMultipart(e);
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }
    
    private BenchToolResult authCreateEntity() throws IOException {
    	String password = "passwd";
    	List<String> level2Ids = new ArrayList<String>();
    	level2Ids.add(level2Id);
    	for (int i = 0; i < 10; i++) {
            level2Ids.add(larchClient.postEntity(BenchToolEntities.createLevel2Entity(level1Id)));
		}
    	String username = larchClient.createUser(null, password, level2Ids);
    	
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, true);
        String entityJson = mapper.writeValueAsString(e);

        Object[] results = this.larchClient.executeAsUser("POST", "/entity", entityJson, username, password);
        return new BenchToolResult(size, (long)results[1]);
    }
    
    private BenchToolResult createBinary() throws IOException {
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, false);
        String entityJson = mapper.writeValueAsString(e);
        String entityId = this.larchClient.postEntity(entityJson);
        final Binary binary = BenchToolEntities.createRandomBinary(size);

        long time = System.currentTimeMillis();
        this.larchClient.postBinary(entityId, binary);
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }
    
    private BenchToolResult createBinaryMultipart() throws IOException {
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, false);
        String entityJson = mapper.writeValueAsString(e);
        String entityId = this.larchClient.postEntity(entityJson);
        final Binary binary = BenchToolEntities.createRandomBinary(size);

        long time = System.currentTimeMillis();
        this.larchClient.postBinaryMultipart(entityId, binary);
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }
    
    private BenchToolResult createMetadata(boolean indexInline) throws IOException {
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, indexInline);
        String entityJson = mapper.writeValueAsString(e);
        String entityId = this.larchClient.postEntity(entityJson);
        MdSize mdSize;
        if (size < 100000) {
            mdSize = MdSize.SMALL;
        } else if (size < 20000000) {
            mdSize = MdSize.MEDIUM;
        } else {
            mdSize = MdSize.BIG;
        }
        
        final Metadata metadata = BenchToolEntities.createRandomMetadata(mdSize, indexInline);

        long time = System.currentTimeMillis();
        this.larchClient.postMetadata(entityId, metadata);
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }
    
    private BenchToolResult retrieveBinary() throws IOException {
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, false);
        String binaryName = e.getBinaries().get(0).getName();
        String entityJson = mapper.writeValueAsString(e);
        String entityId = this.larchClient.postEntity(entityJson);

        long time = System.currentTimeMillis();
        IOUtils.toString(this.larchClient.retrieveBinaryContent(entityId, binaryName));
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }
    
    private BenchToolResult retrieveMetadata() throws IOException {
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, false);
        String mdName = e.getMetadata().get(0).getName();
        String entityJson = mapper.writeValueAsString(e);
        String entityId = this.larchClient.postEntity(entityJson);

        long time = System.currentTimeMillis();
        IOUtils.toString(this.larchClient.retrieveMetadataContent(entityId, mdName));
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }
    
    private void writeFile(String filepath, String text) {
        OutputStreamWriter ostr = null;
        try {
            ostr = new OutputStreamWriter(new FileOutputStream(filepath, false), "UTF-8");
            ostr.write(text);
            ostr.flush();
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (ostr != null) {
                try {
                    ostr.close();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }


}
