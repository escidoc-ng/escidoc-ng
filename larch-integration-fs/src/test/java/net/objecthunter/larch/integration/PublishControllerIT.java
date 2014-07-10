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

import static net.objecthunter.larch.test.util.Fixtures.createFixtureEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import net.objecthunter.larch.model.Entities;
import net.objecthunter.larch.model.Entity;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PublishControllerIT extends AbstractLarchIT {

    private static final Logger log = LoggerFactory.getLogger(PublishControllerIT.class);

    private static final String hostUrl = "http://localhost:8080/";

    private static final String entityUrl = hostUrl + "entity/";

    private static final String publishedUrl = entityUrl + "published/";

    private static final String publishedForEntityUrl = entityUrl + "{id}/published/";

    private static final String identifierUrl = entityUrl + "{id}/identifier/";

    @Test
    public void testRetrievePublishedEntity() throws Exception {
        // create
        HttpResponse resp =
                this.execute(
                        Request.Post(entityUrl)
                                .bodyString(mapper.writeValueAsString(createFixtureEntity()),
                                            ContentType.APPLICATION_JSON))
                        .returnResponse();
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String id = EntityUtils.toString(resp.getEntity());

        // publish
        System.out.println(entityUrl + id + "/publish");
        resp = this.execute(Request.Post(entityUrl + id + "/publish")).returnResponse();
        final String publishId = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());

        // retrieve published
        resp = this.execute(Request.Get(publishedUrl + publishId)).returnResponse();
        assertEquals(200, resp.getStatusLine().getStatusCode());
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertEquals("published", fetched.getState());
        assertEquals(1, fetched.getVersion());
        assertEquals(publishId, fetched.getPublishId());
    }

    @Test
    public void testRetrievePublishedEntities() throws Exception {
        // create
        HttpResponse resp =
                this.execute(
                        Request.Post(entityUrl).bodyString(mapper.writeValueAsString(createFixtureEntity()),
                                ContentType.APPLICATION_JSON)).returnResponse();
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String id = EntityUtils.toString(resp.getEntity());

        // publish
        resp = this.execute(Request.Post("http://localhost:8080/entity/" + id + "/publish")).returnResponse();
        // resp = this.execute(Request.Post(entityUrl + id + "/publish")).returnResponse();
        String publishId = EntityUtils.toString(resp.getEntity());

        // create new identifier
        resp =
                this.execute(
                        Request.Post(identifierUrl.replaceFirst("\\{id\\}", id))
                                .bodyString("type=DOI&value=123",
                                            ContentType.APPLICATION_FORM_URLENCODED))
                        .returnResponse();
        assertEquals(201, resp.getStatusLine().getStatusCode());

        // publish
        resp = this.execute(Request.Post(entityUrl + id + "/publish")).returnResponse();
        String publishId1 = EntityUtils.toString(resp.getEntity());

        // retrieve published
        resp = this.execute(Request.Get(publishedForEntityUrl.replaceFirst("\\{id\\}", id))).returnResponse();
        Entities fetched = mapper.readValue(resp.getEntity().getContent(), Entities.class);
        assertEquals(2, fetched.getEntities().size());
        Entity e1 = fetched.getEntities().get(0);
        Entity e2 = fetched.getEntities().get(1);
        assertEquals(publishId, e1.getPublishId());
        assertEquals(publishId1, e2.getPublishId());
        assertEquals(1, e1.getVersion());
        assertEquals(2, e2.getVersion());
        assertNotNull(e1.getAlternativeIdentifiers());
        assertEquals(0, e1.getAlternativeIdentifiers().size());
        assertNotNull(e2.getAlternativeIdentifiers());
        assertEquals(1, e2.getAlternativeIdentifiers().size());
    }

}
