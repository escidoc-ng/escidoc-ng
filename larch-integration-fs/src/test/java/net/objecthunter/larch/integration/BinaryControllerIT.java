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
 * WITHOUT WARRANTIES OR CONDITIONS OF ROLE_ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.objecthunter.larch.integration;

import static net.objecthunter.larch.test.util.Fixtures.createFixtureEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.objecthunter.larch.model.Entity;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BinaryControllerIT extends AbstractLarchIT {

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void testDelete() throws Exception {
        // create entity
        Entity child = createFixtureEntity();
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl)
                                .bodyString(mapper.writeValueAsString(child), ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        String id = EntityUtils.toString(resp.getEntity());

        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + id));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertNotNull(fetched.getBinaries());
        assertEquals(2, fetched.getBinaries().size());
        String name = fetched.getBinaries().keySet().iterator().next();

        // delete binary
        resp =
                this.executeAsAdmin(
                        Request.Delete(entityUrl + id + "/binary/" + name));
        assertEquals(200, resp.getStatusLine().getStatusCode());

        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + id));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertNotNull(fetched.getBinaries());
        assertEquals(1, fetched.getBinaries().size());
        name = fetched.getBinaries().keySet().iterator().next();

        // delete binary
        resp =
                this.executeAsAdmin(
                        Request.Delete(entityUrl + id + "/binary/" + name));
        assertEquals(200, resp.getStatusLine().getStatusCode());

        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + id));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertNotNull(fetched.getBinaries());
        assertEquals(0, fetched.getBinaries().size());
    }

}
