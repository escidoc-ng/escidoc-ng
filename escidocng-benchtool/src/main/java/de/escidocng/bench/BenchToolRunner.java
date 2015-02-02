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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.escidocng.client.EscidocngClient;

public class BenchToolRunner {

    private static final Logger log = LoggerFactory.getLogger(BenchToolRunner.class);

    private final long size;

    private final int numActions;

    private final BenchTool.Action action;

    private final ExecutorService executor;

    private final EscidocngClient escidocngClient;

    private final URI escidocngUri;

    private final String level1Id;

    private final String level2Id;

    public BenchToolRunner(BenchTool.Action action, URI escidocngUri, String user, String password, int numActions,
            int numThreads, long size) throws IOException {
        this.size = size;
        this.numActions = numActions;
        this.action = action;
        this.escidocngUri = escidocngUri;
        this.escidocngClient = new EscidocngClient(escidocngUri, user, password);
        this.executor = Executors.newFixedThreadPool(numThreads);
        this.level1Id = escidocngClient.postEntity(BenchToolEntities.createLevel1Entity());
        this.level2Id = escidocngClient.postEntity(BenchToolEntities.createLevel2Entity(level1Id));
    }

    public List<BenchToolResult> run() throws IOException {
        final List<Future<BenchToolResult>> futures = new ArrayList<>();
        for (int i = 0; i < numActions; i++) {
            futures.add(executor.submit(new ActionWorker(this.action, this.size, this.escidocngClient, level1Id, level2Id)));
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
