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

import static net.objecthunter.larch.test.util.Fixtures.PERMISSION_ID;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.LarchServerConfiguration;
import net.objecthunter.larch.bench.BenchTool;
import net.objecthunter.larch.bench.BenchToolResult;
import net.objecthunter.larch.bench.BenchToolRunner;
import net.objecthunter.larch.bench.ResultFormatter;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityType;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = LarchServerConfiguration.class)
@IntegrationTest
@WebAppConfiguration
@ActiveProfiles("weedfs")
public class PerformanceIT {

    private static final Logger log = LoggerFactory.getLogger(PerformanceIT.class);

    protected static final int port =8080;

    protected static final String hostUrl = "http://localhost:" + port + "/";

    private HttpHost localhost = new HttpHost("localhost", 8080, "http");

    private Executor executor = Executor.newInstance().auth(localhost, "admin", "admin").authPreemptive(localhost);

    @Autowired
    private Environment env;

    private boolean wsCreated = false;

    @PostConstruct
    public void waitForWeedFs() throws Exception {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        int count = 0;
        boolean weedfsReady = false;
        final ObjectMapper mapper = new ObjectMapper();
        final String weedUri =
                "http://" + env.getProperty("weedfs.master.public") + ":" + env.getProperty("weedfs.master.port");
        log.info("waiting for (datacenters != null) at " + weedUri + "/dir/status");
        do {
            HttpResponse resp = Request.Get(weedUri + "/dir/status")
                    .execute()
                    .returnResponse();
            final String data = EntityUtils.toString(resp.getEntity());
            if (!data.isEmpty()) {
                JsonNode node = mapper.readTree(data);
                if (node.get("Topology").get("DataCenters").get(0) != null) {
                    weedfsReady = true;
                } else {
                    Thread.sleep(150);
                }
            } else {
                Thread.sleep(150);
            }
        } while (!weedfsReady && count++ < 500);
        if (!weedfsReady) {
            throw new Exception("WeedFS not ready after " + count * 150 + " ms");
        }
        if (!wsCreated) {
            // create default area
            Entity area = new Entity();
            area.setType(EntityType.AREA);
            Request r = Request.Post(hostUrl + "entity")
                    .bodyString(mapper.writeValueAsString(area), ContentType.APPLICATION_JSON);
            HttpResponse resp = this.execute(r).returnResponse();
            assertEquals(201, resp.getStatusLine().getStatusCode());
            String areaId = EntityUtils.toString(resp.getEntity());
            // create default workspace
            Entity permission = new Entity();
            permission.setId(PERMISSION_ID);
            permission.setLabel("Test Workspace");
            permission.setParentId(areaId);
            r = Request.Post(hostUrl + "entity")
                    .bodyString(mapper.writeValueAsString(permission), ContentType.APPLICATION_JSON);
            resp = this.execute(r).returnResponse();
            wsCreated = true;
        }
    }

    protected Response execute(Request req) throws IOException {
        return this.executor.execute(req);
    }

    @Test
    public void ingestSingle100MBFile() throws Exception {
        long size = 1024 * 1024 * 100;
        int num = 1;
        int threads = 1;
        BenchToolRunner bench = new BenchToolRunner(BenchTool.Action.INGEST,
                URI.create("http://localhost:8080"),
                "admin",
                "admin",
                num,
                threads,
                size,
                PERMISSION_ID);
        long time = System.currentTimeMillis();
        List<BenchToolResult> results = bench.run();
        ResultFormatter.printResults(results, System.currentTimeMillis() - time, num, size, threads, System.out, 30f);
    }

    @Test
    public void ingestTen10MBFile() throws Exception {
        long size = 1024 * 1024 * 10;
        int num = 10;
        int threads = 3;
        BenchToolRunner bench = new BenchToolRunner(BenchTool.Action.INGEST,
                URI.create("http://localhost:8080"),
                "admin",
                "admin",
                num,
                threads,
                size,
                PERMISSION_ID);
        long time = System.currentTimeMillis();
        List<BenchToolResult> results = bench.run();
        ResultFormatter.printResults(results, System.currentTimeMillis() - time, num, size, threads, System.out, 30f);
    }
}
