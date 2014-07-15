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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import net.objecthunter.larch.model.Workspace;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.junit.Ignore;
import org.junit.Test;

public class WorkspaceControllerIT extends AbstractLarchIT {

    @Test
    public void testCreateAndRetrieve() throws Exception {
        final Workspace ws = new Workspace();
        ws.setId(RandomStringUtils.randomAlphanumeric(16));
        ws.setOwner("foo");
        ws.setName("bar");
        HttpResponse resp = Request.Post(workspaceUrl)
                .bodyString(this.mapper.writeValueAsString(ws), ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();

        final String id = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());
        assertNotNull(id);
        assertEquals(ws.getId(), id);

        resp = Request.Get(workspaceUrl + id)
                .execute()
                .returnResponse();
        assertEquals(200, resp.getStatusLine().getStatusCode());
        final Workspace fetched = this.mapper.readValue(resp.getEntity().getContent(), Workspace.class);
        assertEquals(ws, fetched);
    }

    @Test
    public void testCreateAndUpdate() throws Exception {
        final Workspace ws = new Workspace();
        ws.setId(RandomStringUtils.randomAlphanumeric(16));
        ws.setOwner("foo");
        ws.setName("bar");
        HttpResponse resp = Request.Post(workspaceUrl)
                .bodyString(this.mapper.writeValueAsString(ws), ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();

        final String id = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());
        assertNotNull(id);
        assertEquals(ws.getId(), id);

        ws.setName("bar2");
        resp = Request.Put(workspaceUrl + id)
                .bodyString(this.mapper.writeValueAsString(ws), ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();

        assertEquals(200, resp.getStatusLine().getStatusCode());

        resp = Request.Get(workspaceUrl + id)
                .execute()
                .returnResponse();
        assertEquals(200, resp.getStatusLine().getStatusCode());
        final Workspace fetched = this.mapper.readValue(resp.getEntity().getContent(), Workspace.class);
        assertEquals(ws, fetched);
    }

    @Test
    @Ignore
    /**
     * This test is ignored until there is patch support in fluent-hc
     */
    public void testCreateAndPatch() throws Exception {
        final Workspace ws = new Workspace();
        ws.setId(RandomStringUtils.randomAlphanumeric(16));
        ws.setOwner("foo");
        ws.setName("bar");
        HttpResponse resp = Request.Post(workspaceUrl)
                .bodyString(this.mapper.writeValueAsString(ws), ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();

        final String id = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());
        assertNotNull(id);
        assertEquals(ws.getId(), id);

        final Workspace patch = new Workspace();
        patch.setName("bar3");

        resp = Request.Post(workspaceUrl + id)
                .bodyString(this.mapper.writeValueAsString(patch), ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();

        assertEquals(200, resp.getStatusLine().getStatusCode());

        resp = Request.Get(workspaceUrl + id)
                .execute()
                .returnResponse();
        assertEquals(200, resp.getStatusLine().getStatusCode());
        final Workspace fetched = this.mapper.readValue(resp.getEntity().getContent(), Workspace.class);
        assertEquals(ws.getId(), fetched.getId());
        assertEquals(ws.getOwner(), fetched.getOwner());
        assertEquals(patch.getName(), fetched.getName());
    }
}
