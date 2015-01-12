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
import java.util.concurrent.Callable;

import net.objecthunter.larch.client.LarchClient;
import net.objecthunter.larch.model.Entity;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ActionWorker implements Callable<BenchToolResult> {

    private static final Logger log = LoggerFactory.getLogger(ActionWorker.class);

    private final BenchTool.Action action;

    private final long size;

    private final LarchClient larchClient;

    private final String larchUri;

    private final ObjectMapper mapper = new ObjectMapper();

    private final String level2Id;

    protected ActionWorker(BenchTool.Action action, long size, LarchClient larchClient, String larchUri, String level2Id) {
        this.action = action;
        this.size = size;
        this.larchClient = larchClient;
        this.larchUri = larchUri;
        this.level2Id = level2Id;
    }

    @Override
    public BenchToolResult call() throws Exception {
        switch (this.action) {
        case CREATE_ENTITY:
            return createEntity(false);
        case CREATE_INDEXED_ENTITY:
            return createEntity(true);
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
        default:
            throw new IllegalArgumentException("Unknown action '" + this.action + "'");
        }
    }

    private BenchToolResult deleteEntity(boolean indexInline) throws IOException {
        /* create an entity */
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, indexInline);
        final String entityId = this.larchClient.postEntity(e);

        /* add a binary */
        final String binaryName = RandomStringUtils.randomAlphabetic(16);
        this.larchClient.postBinary(entityId,
                binaryName,
                "application/octet-stream",
                new RandomInputStream(size));

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

        /* measure the update duration 
         * NOTE: recreates all binaries + metadata*/
        e.setLabel("updated label");
        e.setId(entityId);
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
        this.larchClient.retrieveEntityAsStream(entityId);
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }

    private BenchToolResult createEntity(boolean indexInline) throws IOException {
        final Entity e = BenchToolEntities.createRandomFullEntity(level2Id, size, indexInline);
        String entityJson = mapper.writeValueAsString(e);

        long time = System.currentTimeMillis();
        /* create an entity */
        this.larchClient.postEntity(entityJson);
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
