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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.objecthunter.larch.client.LarchClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchToolRunner {

    private static final Logger log = LoggerFactory.getLogger(BenchToolRunner.class);

    private final long size;

    private final int numActions;

    private final BenchTool.Action action;

    private final ExecutorService executor;

    private final LarchClient larchClient;

    private final URI larchUri;

    private final String level1Id;

    private final String level2Id;

    public BenchToolRunner(BenchTool.Action action, URI larchUri, String user, String password, int numActions,
            int numThreads, long size) throws IOException {
        this.size = size;
        this.numActions = numActions;
        this.action = action;
        this.larchUri = larchUri;
        this.larchClient = new LarchClient(larchUri, user, password);
        this.executor = Executors.newFixedThreadPool(numThreads);
        this.level1Id = larchClient.postEntity(BenchToolEntities.createLevel1Entity());
        this.level2Id = larchClient.postEntity(BenchToolEntities.createLevel2Entity(level1Id));
    }

    public List<BenchToolResult> run() throws IOException {
        final List<Future<BenchToolResult>> futures = new ArrayList<>();
        for (int i = 0; i < numActions; i++) {
            futures.add(executor.submit(new ActionWorker(this.action, this.size, this.larchClient, level1Id, level2Id)));
        }

        try {
            final List<BenchToolResult> results = new ArrayList<>();
            int count = 0;
            for (Future<BenchToolResult> f : futures) {
                results.add(f.get());
                log.debug("Finished {} of {} {} actions", ++count, numActions, action.name());
            }
            return results;
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        } finally {
            this.executor.shutdown();
        }
    }
}
