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

import static net.objecthunter.larch.test.util.Fixtures.createFixtureCollectionEntity;
import static net.objecthunter.larch.test.util.Fixtures.createFixtureEntity;
import static net.objecthunter.larch.test.util.Fixtures.createSimpleFixtureEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import net.objecthunter.larch.integration.helpers.TestMessageListener;
import net.objecthunter.larch.model.AuditRecords;
import net.objecthunter.larch.model.Entities;
import net.objecthunter.larch.model.Entity;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityControllerIT extends AbstractLarchIT {

    private static final Logger log = LoggerFactory.getLogger(EntityControllerIT.class);

    @Test
    public void testCreateAndUpdateEntity() throws Exception {
        HttpResponse resp =
                this.execute(
                        Request.Post(entityUrl).bodyString(mapper.writeValueAsString(createFixtureEntity()),
                                ContentType.APPLICATION_JSON)).returnResponse();
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String id = EntityUtils.toString(resp.getEntity());

        Entity update = createFixtureEntity();
        update.setLabel("My updated Label");
        resp =
                this.execute(
                        Request.Put(entityUrl + id)
                                .bodyString(mapper.writeValueAsString(update), ContentType.APPLICATION_JSON))
                        .returnResponse();
        assertEquals(200, resp.getStatusLine().getStatusCode());

        resp = this.execute(Request.Get(entityUrl + id)).returnResponse();
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertEquals(2, fetched.getVersion());

        resp = this.execute(Request.Get(entityUrl + id + "/version/1")).returnResponse();
        Entity oldVersion = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertEquals(1, oldVersion.getVersion());
        assertNotNull(oldVersion.getUtcCreated());
        assertNotNull(oldVersion.getUtcLastModified());
        assertTrue(Duration.between(ZonedDateTime.parse(oldVersion.getUtcLastModified()),
                ZonedDateTime.parse(fetched.getUtcLastModified())).getNano() > 0);
        assertEquals(ZonedDateTime.parse(oldVersion.getUtcCreated()), ZonedDateTime.parse(fetched.getUtcCreated()));
        oldVersion.getBinaries().values().forEach(b -> {
            assertNotNull(b.getUtcCreated());
            assertNotNull(b.getUtcLastModified());
        });
        fetched.getBinaries().values().forEach(b -> {
            assertNotNull(b.getUtcCreated());
            assertNotNull(b.getUtcLastModified());
        });
    }

    @Test
    public void testCreateAndRetrieveEntityWithChildren() throws Exception {
        HttpResponse resp =
                this.execute(
                        Request.Post(entityUrl)
                                .bodyString(mapper.writeValueAsString(createSimpleFixtureEntity()),
                                        ContentType.APPLICATION_JSON))
                        .returnResponse();
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String id = EntityUtils.toString(resp.getEntity());

        for (int i = 0; i < 2; i++) {
            Entity child = createSimpleFixtureEntity();
            child.setParentId(id);
            resp =
                    this.execute(
                            Request.Post(entityUrl)
                                    .bodyString(mapper.writeValueAsString(child),
                                            ContentType.APPLICATION_JSON))
                            .returnResponse();
            assertEquals(201, resp.getStatusLine().getStatusCode());
        }

        resp = this.execute(Request.Get(entityUrl + id)).returnResponse();
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertEquals(2, fetched.getChildren().size());
    }

    @Test
    public void testCreateAndRetrieveEntityWithOneHundredChildren() throws Exception {
        Entity e = createFixtureCollectionEntity();
        long time = System.currentTimeMillis();
        HttpResponse resp =
                this.execute(
                        Request.Post(entityUrl)
                                .bodyString(mapper.writeValueAsString(e),
                                        ContentType.APPLICATION_JSON))
                        .returnResponse();
        final String id = EntityUtils.toString(resp.getEntity());

        for (int i = 0; i < 100; i++) {
            Entity child = createSimpleFixtureEntity();
            child.setParentId(id);
            resp =
                    this.execute(
                            Request.Post(entityUrl)
                                    .bodyString(mapper.writeValueAsString(child), ContentType.APPLICATION_JSON))
                            .returnResponse();
            assertEquals(201, resp.getStatusLine().getStatusCode());
        }
        log.debug("creating an entity with 100 children took {} ms", System.currentTimeMillis() - time);
        assertEquals(201, resp.getStatusLine().getStatusCode());

        time = System.currentTimeMillis();
        resp = this.execute(Request.Get(entityUrl + id)).returnResponse();
        log.debug("fetching an entity with 100 children took {} ms", System.currentTimeMillis() - time);
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertEquals(100, fetched.getChildren().size());
        assertEquals("Collection", fetched.getType());
    }

    @Test
    public void testCreateAndReceiveMessage() throws Exception {
        String brokerUrl = "vm://localhost";
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        Connection conn = connectionFactory.createConnection();
        conn.start();
        Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = sess.createQueue("larch");
        TestMessageListener listener = new TestMessageListener();
        MessageConsumer consumer = sess.createConsumer(queue);
        consumer.setMessageListener(listener);

        // create a new entity
        HttpResponse resp =
                this.execute(
                        Request.Post(entityUrl)
                                .bodyString(mapper.writeValueAsString(createSimpleFixtureEntity()),
                                        ContentType.APPLICATION_JSON))
                        .returnResponse();
        assertEquals(201, resp.getStatusLine().getStatusCode());
        // assertTrue(listener.isMessageReceived());
        // assertNotNull(msg);
        // assertTrue(msg instanceof TextMessage);
        // TextMessage txt = (TextMessage) msg;
        // this one fails on Travis sporadically
        // see e.g. https://travis-ci.org/fasseg/larch/builds/28821493#L3346
        // assertNotNull(txt.getText());
        // assertTrue(txt.getText().startsWith("Created entity"));
        while (listener.isMessageReceived()) {
            Message msg = listener.getMessage();
            log.warn("NULL ERROR ---------------------------");
            log.warn("Checking message {}", msg.toString());
            log.warn("Is text message? {}", (msg instanceof TextMessage));
            if (msg instanceof TextMessage) {
                if (((TextMessage) msg).getText() == null) {
                    log.warn("!!!!!! HIT: NULL message", ((TextMessage) msg).getText());
                    fail("null message detected");
                }
            }
        }
    }

    @Test
    public void testRetrieveVersions() throws Exception {
        HttpResponse resp =
                this.execute(
                        Request.Post(entityUrl)
                                .bodyString(mapper.writeValueAsString(createFixtureEntity()),
                                        ContentType.APPLICATION_JSON))
                        .returnResponse();
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String id = EntityUtils.toString(resp.getEntity());

        for (int i = 0; i < 50; i++) {
            Entity update = createFixtureEntity();
            update.setLabel("My updated Label" + i);
            resp =
                    this.execute(
                            Request.Put(entityUrl + id)
                                    .bodyString(mapper.writeValueAsString(update),
                                            ContentType.APPLICATION_JSON))
                            .returnResponse();
            assertEquals(200, resp.getStatusLine().getStatusCode());
        }

        resp = this.execute(Request.Get(entityUrl + id + "/versions")).returnResponse();
        Entities fetched = mapper.readValue(resp.getEntity().getContent(), Entities.class);
        assertEquals(51, fetched.getEntities().size());
        int i = 51;
        for (Entity entity : fetched.getEntities()) {
            assertEquals(i, entity.getVersion());
            i--;
            if (i > 0) {
                assertEquals("My updated Label" + (i - 1), entity.getLabel());
            }
        }
        assertEquals(i, 0);
    }

    @Test
    public void testPublish() throws Exception {
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
        resp = this.execute(Request.Post(entityUrl + id + "/publish")).returnResponse();

        // retrieve
        resp = this.execute(Request.Get(entityUrl + id)).returnResponse();
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertEquals("published", fetched.getState());
        assertEquals(1, fetched.getVersion());

        // update
        Entity update = createFixtureEntity();
        update.setLabel("My updated Label1");
        resp =
                this.execute(
                        Request.Put(entityUrl + id)
                                .bodyString(mapper.writeValueAsString(update),
                                        ContentType.APPLICATION_JSON))
                        .returnResponse();
        assertEquals(200, resp.getStatusLine().getStatusCode());

        // retrieve
        resp = this.execute(Request.Get(entityUrl + id)).returnResponse();
        fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertEquals("ingested", fetched.getState());
        assertEquals(2, fetched.getVersion());

        // retrieve published
        resp = this.execute(Request.Get(defaultWorkspaceUrl + "/published/" + id + ":1")).returnResponse();
        fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertEquals("published", fetched.getState());
        assertEquals(1, fetched.getVersion());
    }

    @Test
    public void testDelete() throws Exception {
        // create hierarchy
        String id = null;
        List<String> ids = new ArrayList<String>();
        String parentId = null;
        for (int i = 0; i < 5; i++) {
            Entity child = createFixtureEntity();
            child.setParentId(id);
            HttpResponse resp =
                    this.execute(
                            Request.Post("http://localhost:8080/entity")
                                    .bodyString(mapper.writeValueAsString(child), ContentType.APPLICATION_JSON))
                            .returnResponse();
            assertEquals(201, resp.getStatusLine().getStatusCode());
            if (id == null) {
                parentId = EntityUtils.toString(resp.getEntity());
            }
            id = EntityUtils.toString(resp.getEntity());
            ids.add(EntityUtils.toString(resp.getEntity()));
        }

        // delete parent
        HttpResponse resp =
                this.execute(
                        Request.Delete("http://localhost:8080/entity/" + parentId))
                        .returnResponse();
        assertEquals(200, resp.getStatusLine().getStatusCode());

        // Check Audit-Records
        for (String checkId : ids) {
            resp =
                    this.execute(
                            Request.Get("http://localhost:8080/entity/" + checkId + "/audit"))
                            .returnResponse();
            AuditRecords fetched = mapper.readValue(resp.getEntity().getContent(), AuditRecords.class);
            assertEquals(200, resp.getStatusLine().getStatusCode());
            assertNotNull(fetched);
            assertEquals(0, fetched.getAuditRecords().size());
        }
        // Check Entities
        for (String checkId : ids) {
            resp =
                    this.execute(
                            Request.Get("http://localhost:8080/entity/" + checkId))
                            .returnResponse();
            assertEquals(404, resp.getStatusLine().getStatusCode());
        }
    }

}
