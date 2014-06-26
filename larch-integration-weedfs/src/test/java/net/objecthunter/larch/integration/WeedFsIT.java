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
package net.objecthunter.larch.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.objecthunter.larch.LarchServerConfiguration;
import net.objecthunter.larch.service.backend.weedfs.WeedFsMaster;
import net.objecthunter.larch.service.backend.weedfs.WeedFsVolume;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

import static org.junit.Assert.assertTrue;

public class WeedFsIT extends AbstractWeedFsLarchIT {

    @Autowired
    private LarchServerConfiguration config;

    @Autowired
    private Environment env;

    @PostConstruct
    public void setup() {
        final WeedFsMaster master = config.weedFsMaster();
        final WeedFsVolume volume = config.weedfsVolume();
    }

    @Test
    public void testGetFid() throws Exception {
        // retrieve a fid from WeedFs
        int count = 0;
        boolean fidFetched = false;
        while (count++ < 50 && !fidFetched) {
            final HttpResponse resp = Request.Get("http://localhost:9333/dir/assign")
                    .execute()
                    .returnResponse();
            final JsonNode node = new ObjectMapper().readTree(resp.getEntity().getContent());
            fidFetched = node.get("fid") != null;
            if (!fidFetched) {
                Thread.sleep(100);
            }
        }
        assertTrue(fidFetched);
    }

    @Test
    public void testGetStatus() throws Exception {
        int count = 0;
        boolean statusFetched = false;
        while (count++ < 50 && !statusFetched) {
            final HttpResponse resp = Request.Get("http://localhost:9333/dir/status")
                    .execute()
                    .returnResponse();
            final JsonNode node = new ObjectMapper().readTree(resp.getEntity().getContent());
            statusFetched = node.get("Topology") != null;
            if (!statusFetched) {
                Thread.sleep(100);
            }
        }
        assertTrue(statusFetched);
    }
}
