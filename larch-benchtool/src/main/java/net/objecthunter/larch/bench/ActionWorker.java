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

package net.objecthunter.larch.bench;

import java.io.IOException;
import java.util.concurrent.Callable;

import net.objecthunter.larch.client.LarchClient;
import net.objecthunter.larch.model.Entity;

import net.objecthunter.larch.model.Workspace;
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

    protected ActionWorker(BenchTool.Action action, long size, LarchClient larchClient, String larchUri) {
        this.action = action;
        this.size = size;
        this.larchClient = larchClient;
        this.larchUri = larchUri;
    }

    @Override
    public BenchToolResult call() throws Exception {
        switch (this.action) {
        case INGEST:
            return doIngest();
        case RETRIEVE:
            return doRetrieve();
        case UPDATE:
            return doUpdate();
        case DELETE:
            return doDelete();
        default:
            throw new IllegalArgumentException("Unknown action '" + this.action + "'");
        }
    }

    private BenchToolResult doDelete() throws IOException {
        /* create an entity */
        final Entity e = BenchToolEntities.createRandomEmptyEntity();
        final String entityId = this.larchClient.postEntity(Workspace.DEFAULT, e);

        /* add a binary */
        final String binaryName = RandomStringUtils.randomAlphabetic(16);
        this.larchClient.postBinary(Workspace.DEFAULT, entityId,
                binaryName,
                "application/octet-stream",
                new RandomInputStream(size));

        /* measure the deletion duration */
        long time = System.currentTimeMillis();
        this.larchClient.deleteEntity(Workspace.DEFAULT, entityId);
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }

    private BenchToolResult doUpdate() throws IOException {
        /* create an entity */
        final Entity e = BenchToolEntities.createRandomEmptyEntity();
        final String entityId = this.larchClient.postEntity(Workspace.DEFAULT, e);

        /* add a binary */
        final String binaryName = RandomStringUtils.randomAlphabetic(16);
        this.larchClient.postBinary(Workspace.DEFAULT, entityId,
                binaryName,
                "application/octet-stream",
                new RandomInputStream(size));

        /* measure the update duration */
        e.setLabel("updated label");
        e.setId(entityId);
        long time = System.currentTimeMillis();
        this.larchClient.postBinary(Workspace.DEFAULT, entityId,
                binaryName,
                "application/octet-stream",
                new RandomInputStream(size));
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }

    private BenchToolResult doRetrieve() throws IOException {
        /* create an entity */
        final Entity e = BenchToolEntities.createRandomEmptyEntity();
        final String entityId = this.larchClient.postEntity(Workspace.DEFAULT, e);

        /* add a binary */
        final String binaryName = RandomStringUtils.randomAlphabetic(16);
        this.larchClient.postBinary(Workspace.DEFAULT, entityId,
                binaryName,
                "application/octet-stream",
                new RandomInputStream(size));

        /* measure the retrieval duration */
        long time = System.currentTimeMillis();
        this.larchClient.retrieveBinaryContent(Workspace.DEFAULT, entityId, binaryName);
        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }

    private BenchToolResult doIngest() throws IOException {
        final Entity e = BenchToolEntities.createRandomEmptyEntity();

        long time = System.currentTimeMillis();
        /* create an entity */
        final String entityId = this.larchClient.postEntity(Workspace.DEFAULT, e);

        /* add a binary */
        this.larchClient.postBinary(Workspace.DEFAULT, entityId,
                RandomStringUtils.randomAlphabetic(16),
                "application/octet-stream",
                new RandomInputStream(size));

        return new BenchToolResult(size, System.currentTimeMillis() - time);
    }

}
