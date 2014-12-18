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


package net.objecthunter.larch.integration.fs;

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

import net.objecthunter.larch.integration.fs.helpers.TestMessageListener;
import net.objecthunter.larch.model.AuditRecords;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entities;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityControllerIT extends AbstractFSLarchIT {

    private static final Logger log = LoggerFactory.getLogger(EntityControllerIT.class);

    @Test
    public void testCreateAndUpdateEntity() throws Exception {
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl).bodyString(mapper.writeValueAsString(createFixtureEntity(false)),
                                ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String id = EntityUtils.toString(resp.getEntity());

        Entity update = createFixtureEntity(false);
        update.setLabel("My updated Label");
        resp =
                this.executeAsAdmin(
                        Request.Put(entityUrl + id)
                                .bodyString(mapper.writeValueAsString(update), ContentType.APPLICATION_JSON));
        assertEquals(200, resp.getStatusLine().getStatusCode());

        resp = this.executeAsAdmin(Request.Get(entityUrl + id));
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertEquals(2, fetched.getVersion());

        resp = this.executeAsAdmin(Request.Get(entityUrl + id + "/version/1"));
        Entity oldVersion = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertEquals(1, oldVersion.getVersion());
        assertNotNull(oldVersion.getUtcCreated());
        assertNotNull(oldVersion.getUtcLastModified());
        assertTrue(Duration.between(ZonedDateTime.parse(oldVersion.getUtcLastModified()),
                ZonedDateTime.parse(fetched.getUtcLastModified())).getNano() > 0);
        assertEquals(ZonedDateTime.parse(oldVersion.getUtcCreated()), ZonedDateTime.parse(fetched.getUtcCreated()));
        oldVersion.getBinaries().forEach(b -> {
            assertNotNull(b.getUtcCreated());
            assertNotNull(b.getUtcLastModified());
        });
        fetched.getBinaries().forEach(b -> {
            assertNotNull(b.getUtcCreated());
            assertNotNull(b.getUtcLastModified());
        });
    }

    @Test
    public void testCreateAndRetrieveEntityWithChildren() throws Exception {
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl)
                                .bodyString(mapper.writeValueAsString(createSimpleFixtureEntity()),
                                        ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String id = EntityUtils.toString(resp.getEntity());

        for (int i = 0; i < 2; i++) {
            Entity child = createSimpleFixtureEntity();
            child.setParentId(id);
            resp =
                    this.executeAsAdmin(
                            Request.Post(entityUrl)
                                    .bodyString(mapper.writeValueAsString(child),
                                            ContentType.APPLICATION_JSON));
            assertEquals(201, resp.getStatusLine().getStatusCode());
        }

        resp = this.executeAsAdmin(Request.Get(entityUrl + id));
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertEquals(2, fetched.getChildren().size());
    }

    @Test
    public void testCreateAndRetrieveEntityWithOneHundredChildren() throws Exception {
        Entity e = createFixtureCollectionEntity();
        long time = System.currentTimeMillis();
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl)
                                .bodyString(mapper.writeValueAsString(e),
                                        ContentType.APPLICATION_JSON));
        final String id = EntityUtils.toString(resp.getEntity());

        for (int i = 0; i < 100; i++) {
            Entity child = createSimpleFixtureEntity();
            child.setParentId(id);
            resp =
                    this.executeAsAdmin(
                            Request.Post(entityUrl)
                                    .bodyString(mapper.writeValueAsString(child), ContentType.APPLICATION_JSON));
            assertEquals(201, resp.getStatusLine().getStatusCode());
        }
        log.debug("creating an entity with 100 children took {} ms", System.currentTimeMillis() - time);
        assertEquals(201, resp.getStatusLine().getStatusCode());

        time = System.currentTimeMillis();
        resp = this.executeAsAdmin(Request.Get(entityUrl + id));
        log.debug("fetching an entity with 100 children took {} ms", System.currentTimeMillis() - time);
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertEquals(100, fetched.getChildren().size());
        assertEquals(FixedContentModel.DATA.getName(), fetched.getContentModelId());
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
                this.executeAsAdmin(
                        Request.Post(entityUrl)
                                .bodyString(mapper.writeValueAsString(createSimpleFixtureEntity()),
                                        ContentType.APPLICATION_JSON));
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
            if (msg == null) {
                fail("Null message object detected");
            }
            if (msg instanceof TextMessage) {
                if (((TextMessage) msg).getText() == null) {
                    log.warn("!!!!!! HIT: NULL message", ((TextMessage) msg).getText());
                    fail("Null message detected");
                }
            }
        }
    }

    @Test
    public void testRetrieveVersions() throws Exception {
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl)
                                .bodyString(mapper.writeValueAsString(createFixtureEntity(false)),
                                        ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String id = EntityUtils.toString(resp.getEntity());

        for (int i = 0; i < 50; i++) {
            Entity update = createFixtureEntity(false);
            update.setLabel("My updated Label" + i);
            resp =
                    this.executeAsAdmin(
                            Request.Put(entityUrl + id)
                                    .bodyString(mapper.writeValueAsString(update),
                                            ContentType.APPLICATION_JSON));
            assertEquals(200, resp.getStatusLine().getStatusCode());
        }

        resp = this.executeAsAdmin(Request.Get(entityUrl + id + "/versions"));
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
                this.executeAsAdmin(
                        Request.Post(entityUrl)
                                .bodyString(mapper.writeValueAsString(createFixtureEntity(false)),
                                        ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String id = EntityUtils.toString(resp.getEntity());

        // publish
        resp = this.executeAsAdmin(Request.Put(entityUrl + id + "/publish"));

        // retrieve
        resp = this.executeAsAdmin(Request.Get(entityUrl + id));
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertEquals(EntityState.PUBLISHED, fetched.getState());
        assertEquals(1, fetched.getVersion());

        // update
        Entity update = createFixtureEntity(false);
        update.setLabel("My updated Label1");
        resp =
                this.executeAsAdmin(
                        Request.Put(entityUrl + id)
                                .bodyString(mapper.writeValueAsString(update),
                                        ContentType.APPLICATION_JSON));
        String response = EntityUtils.toString(resp.getEntity());
        assertEquals(400, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testDelete() throws Exception {
        // create hierarchy
        String id = null;
        List<String> ids = new ArrayList<String>();
        String parentId = null;
        for (int i = 0; i < 5; i++) {
            Entity child = createFixtureEntity(false);
            if (id != null) {
                child.setParentId(id);
            }
            HttpResponse resp =
                    this.executeAsAdmin(
                            Request.Post(entityUrl)
                                    .bodyString(mapper.writeValueAsString(child), ContentType.APPLICATION_JSON));
            String test = EntityUtils.toString(resp.getEntity());
            assertEquals(201, resp.getStatusLine().getStatusCode());
            if (id == null) {
                parentId = EntityUtils.toString(resp.getEntity());
            }
            id = EntityUtils.toString(resp.getEntity());
            ids.add(EntityUtils.toString(resp.getEntity()));
        }

        // delete parent
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Delete(entityUrl + parentId));
        assertEquals(200, resp.getStatusLine().getStatusCode());

        // Check Audit-Records
        for (String checkId : ids) {
            resp =
                    this.executeAsAdmin(
                            Request.Get(entityUrl + checkId + "/audit"));
            AuditRecords fetched = mapper.readValue(resp.getEntity().getContent(), AuditRecords.class);
            assertEquals(200, resp.getStatusLine().getStatusCode());
            assertNotNull(fetched);
            assertEquals(0, fetched.getAuditRecords().size());
        }
        // Check Entities
        for (String checkId : ids) {
            resp =
                    this.executeAsAdmin(
                            Request.Get(entityUrl + checkId));
            assertEquals(404, resp.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void testDeleteMetadata() throws Exception {
        // create entity
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl)
                                .bodyString(mapper.writeValueAsString(createFixtureEntity(false)),
                                        ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String newId = EntityUtils.toString(resp.getEntity());

        // retrieve entity
        resp = this.executeAsAdmin(Request.Get(entityUrl + newId));
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertNotNull(fetched.getMetadata());
        assertEquals(1, fetched.getMetadata().size());
        String name = fetched.getMetadata().iterator().next().getName();

        // delete metadata
        resp =
                this.executeAsAdmin(Request.Delete(entityUrl + newId + "/metadata/" + name));
        assertEquals(200, resp.getStatusLine().getStatusCode());

        // retrieve entity
        resp = this.executeAsAdmin(Request.Get(entityUrl + newId));
        fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertNotNull(fetched.getMetadata());
        assertEquals(0, fetched.getMetadata().size());
    }

    @Test
    public void testDeleteBinaryMetadata() throws Exception {
        // create entity
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl)
                                .bodyString(mapper.writeValueAsString(createFixtureEntity(false)),
                                        ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String newId = EntityUtils.toString(resp.getEntity());

        // retrieve entity
        resp = this.executeAsAdmin(Request.Get(entityUrl + newId));
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertNotNull(fetched.getBinaries());
        assertEquals(2, fetched.getBinaries().size());
        String name = fetched.getBinaries().iterator().next().getName();
        assertNotNull(fetched.getBinary(name).getMetadata());
        assertEquals(1, fetched.getBinary(name).getMetadata().size());
        String mdName = fetched.getBinary(name).getMetadata().iterator().next().getName();

        // delete binary metadata
        resp =
                this.executeAsAdmin(
                        Request.Delete(entityUrl + newId + "/binary/" + name + "/metadata/" +
                                mdName));
        assertEquals(200, resp.getStatusLine().getStatusCode());

        // retrieve entity
        resp = this.executeAsAdmin(Request.Get(entityUrl + newId));
        fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertNotNull(fetched.getBinary(name).getMetadata());
        assertEquals(0, fetched.getBinary(name).getMetadata().size());
    }

}
