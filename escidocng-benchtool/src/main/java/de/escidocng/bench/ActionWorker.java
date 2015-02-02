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
package de.escidocng.bench;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.escidocng.bench.BenchTool.MdSize;
import de.escidocng.client.EscidocngClient;
import de.escidocng.model.Binary;
import de.escidocng.model.Entity;
import de.escidocng.model.Metadata;

public class ActionWorker implements Callable<BenchToolResult> {

    private static final Logger log = LoggerFactory.getLogger(ActionWorker.class);

    private final BenchTool.Action action;

    private final long size;

    private final EscidocngClient escidocngClient;

    private final ObjectMapper mapper = new ObjectMapper();

    private final String level1Id;

    private final String level2Id;

    protected ActionWorker(BenchTool.Action action, long size, EscidocngClient escidocngClient, String level1Id, String level2Id) {
        this.action = action;
        this.size = size;
        this.escidocngClient = escidocngClient;
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
        final String entityId = this.escidocngClient.postEntity(e);

        /* add a binary */
        final Binary binary = BenchToolEntities.createRandomBinary(size);
        this.escidocngClient.postBinary(entityId, binary);

        /* measure the deletion duration */
        long time = System.currentTimeMillis();
        this.escidocngClient.deleteEntity(entityId);
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }

    private BenchToolResult updateEntity(boolean indexInline) throws IOException {
        /* create an entity */
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, indexInline);
        String entityJson = mapper.writeValueAsString(e);
        final String entityId = this.escidocngClient.postEntity(entityJson);

        /* measure the update duration */
        e.setLabel("updated label");
        e.setId(entityId);
        Binary bin = BenchToolEntities.createRandomBinary(size);
        bin.setMetadata(BenchToolEntities.createMetadataList(2, size, indexInline));
        e.getBinaries().add(bin);
        entityJson = mapper.writeValueAsString(e);
        
        long time = System.currentTimeMillis();
        this.escidocngClient.updateEntity(entityId, entityJson);
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }

    private BenchToolResult retrieveEntity(boolean indexInline) throws IOException {
        /* create an entity */
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, indexInline);
        String entityJson = mapper.writeValueAsString(e);
        final String entityId = this.escidocngClient.postEntity(entityJson);

        /* measure the retrieval duration */
        long time = System.currentTimeMillis();
        IOUtils.toString(this.escidocngClient.retrieveEntityAsStream(entityId));
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }

    private BenchToolResult authRetrieveEntity() throws IOException {
    	String password = "passwd";
    	List<String> level2Ids = new ArrayList<String>();
    	level2Ids.add(level2Id);
    	for (int i = 0; i < 10; i++) {
            level2Ids.add(escidocngClient.postEntity(BenchToolEntities.createLevel2Entity(level1Id)));
		}
    	String username = escidocngClient.createUser(null, password, level2Ids);
    	String entityId = escidocngClient.postEntity(BenchToolEntities.createRandomFullEntity(level2Id, size, true));

        Object[] results = this.escidocngClient.executeAsUser("GET", "/entity/" + entityId, null, username, password);
        return new BenchToolResult(size, (long)results[1]);
    }
    
    private BenchToolResult createEntity(boolean indexInline) throws IOException {
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, indexInline);
        String entityJson = mapper.writeValueAsString(e);

        long time = System.currentTimeMillis();
        this.escidocngClient.postEntity(entityJson);
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }
    
    private BenchToolResult createEntityParted(boolean indexInline) throws IOException {
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, indexInline);

        long time = System.currentTimeMillis();
        this.escidocngClient.postEntityMultipart(e);
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }
    
    private BenchToolResult authCreateEntity() throws IOException {
    	String password = "passwd";
    	List<String> level2Ids = new ArrayList<String>();
    	level2Ids.add(level2Id);
    	for (int i = 0; i < 10; i++) {
            level2Ids.add(escidocngClient.postEntity(BenchToolEntities.createLevel2Entity(level1Id)));
		}
    	String username = escidocngClient.createUser(null, password, level2Ids);
    	
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, true);
        String entityJson = mapper.writeValueAsString(e);

        Object[] results = this.escidocngClient.executeAsUser("POST", "/entity", entityJson, username, password);
        return new BenchToolResult(size, (long)results[1]);
    }
    
    private BenchToolResult createBinary() throws IOException {
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, false);
        String entityJson = mapper.writeValueAsString(e);
        String entityId = this.escidocngClient.postEntity(entityJson);
        final Binary binary = BenchToolEntities.createRandomBinary(size);

        long time = System.currentTimeMillis();
        this.escidocngClient.postBinary(entityId, binary);
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }
    
    private BenchToolResult createBinaryMultipart() throws IOException {
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, false);
        String entityJson = mapper.writeValueAsString(e);
        String entityId = this.escidocngClient.postEntity(entityJson);
        final Binary binary = BenchToolEntities.createRandomBinary(size);

        long time = System.currentTimeMillis();
        this.escidocngClient.postBinaryMultipart(entityId, binary);
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }
    
    private BenchToolResult createMetadata(boolean indexInline) throws IOException {
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, indexInline);
        String entityJson = mapper.writeValueAsString(e);
        String entityId = this.escidocngClient.postEntity(entityJson);
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
        this.escidocngClient.postMetadata(entityId, metadata);
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }
    
    private BenchToolResult retrieveBinary() throws IOException {
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, false);
        String binaryName = e.getBinaries().get(0).getName();
        String entityJson = mapper.writeValueAsString(e);
        String entityId = this.escidocngClient.postEntity(entityJson);

        long time = System.currentTimeMillis();
        IOUtils.toString(this.escidocngClient.retrieveBinaryContent(entityId, binaryName));
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }
    
    private BenchToolResult retrieveMetadata() throws IOException {
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, false);
        String mdName = e.getMetadata().get(0).getName();
        String entityJson = mapper.writeValueAsString(e);
        String entityId = this.escidocngClient.postEntity(entityJson);

        long time = System.currentTimeMillis();
        IOUtils.toString(this.escidocngClient.retrieveMetadataContent(entityId, mdName));
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
