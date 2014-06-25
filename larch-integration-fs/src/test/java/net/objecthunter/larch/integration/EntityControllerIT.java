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

import static net.objecthunter.larch.integration.helpers.Fixtures.createFixtureCollectionEntity;
import static net.objecthunter.larch.integration.helpers.Fixtures.createFixtureEntity;
import static net.objecthunter.larch.integration.helpers.Fixtures.createSimpleFixtureEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.ZonedDateTime;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import net.objecthunter.larch.integration.helpers.TestMessageListener;
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
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EntityControllerIT extends AbstractLarchIT {
    private static final Logger log = LoggerFactory.getLogger(EntityControllerIT.class);

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void testCreateAndUpdateEntity() throws Exception {
        HttpResponse resp =
            this.execute(
                Request.Post("http://localhost:8080/entity").bodyString(
                    mapper.writeValueAsString(createFixtureEntity()), ContentType.APPLICATION_JSON)).returnResponse();
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String id = EntityUtils.toString(resp.getEntity());

        Entity update = createFixtureEntity();
        update.setLabel("My updated Label");
        resp =
            this.execute(
                Request.Put("http://localhost:8080/entity/" + id).bodyString(mapper.writeValueAsString(update),
                    ContentType.APPLICATION_JSON)).returnResponse();
        assertEquals(200, resp.getStatusLine().getStatusCode());

        resp = this.execute(Request.Get("http://localhost:8080/entity/" + id)).returnResponse();
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertEquals(2, fetched.getVersion());

        resp = this.execute(Request.Get("http://localhost:8080/entity/" + id + "/version/1")).returnResponse();
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
                Request.Post("http://localhost:8080/entity").bodyString(
                    mapper.writeValueAsString(createSimpleFixtureEntity()), ContentType.APPLICATION_JSON))
                .returnResponse();
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String id = EntityUtils.toString(resp.getEntity());

        for (int i = 0; i < 2; i++) {
            Entity child = createSimpleFixtureEntity();
            child.setParentId(id);
            resp =
                this.execute(
                    Request.Post("http://localhost:8080/entity").bodyString(mapper.writeValueAsString(child),
                        ContentType.APPLICATION_JSON)).returnResponse();
            assertEquals(201, resp.getStatusLine().getStatusCode());
        }

        resp = this.execute(Request.Get("http://localhost:8080/entity/" + id)).returnResponse();
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        assertEquals(2, fetched.getChildren().size());
    }

    @Test
    public void testCreateAndRetrieveEntityWithOneHundredChildren() throws Exception {
        Entity e = createFixtureCollectionEntity();
        long time = System.currentTimeMillis();
        HttpResponse resp =
            this.execute(
                Request.Post("http://localhost:8080/entity").bodyString(mapper.writeValueAsString(e),
                    ContentType.APPLICATION_JSON)).returnResponse();
        final String id = EntityUtils.toString(resp.getEntity());

        for (int i = 0; i < 100; i++) {
            Entity child = createSimpleFixtureEntity();
            child.setParentId(id);
            resp =
                this.execute(
                    Request.Post("http://localhost:8080/entity").bodyString(mapper.writeValueAsString(child),
                        ContentType.APPLICATION_JSON)).returnResponse();
            assertEquals(201, resp.getStatusLine().getStatusCode());
        }
        log.debug("creating an entity with 100 children took {} ms", System.currentTimeMillis() - time);
        assertEquals(201, resp.getStatusLine().getStatusCode());

        time = System.currentTimeMillis();
        resp = this.execute(Request.Get("http://localhost:8080/entity/" + id)).returnResponse();
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
                Request.Post("http://localhost:8080/entity").bodyString(
                    mapper.writeValueAsString(createSimpleFixtureEntity()), ContentType.APPLICATION_JSON))
                .returnResponse();
        assertEquals(201, resp.getStatusLine().getStatusCode());
        assertTrue(listener.isMessageReceived());
        Message msg = listener.getLastMessage();
        assertNotNull(msg);
        assertTrue(msg instanceof TextMessage);
        assertTrue(((TextMessage) msg).getText().startsWith("Created entity"));
    }

    @Test
    public void testRetrieveVersions() throws Exception {
        HttpResponse resp =
            this.execute(
                Request.Post("http://localhost:8080/entity").bodyString(
                    mapper.writeValueAsString(createFixtureEntity()), ContentType.APPLICATION_JSON)).returnResponse();
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String id = EntityUtils.toString(resp.getEntity());

        for (int i = 0; i < 50; i++) {
            Entity update = createFixtureEntity();
            update.setLabel("My updated Label" + i);
            resp =
                this.execute(
                    Request.Put("http://localhost:8080/entity/" + id).bodyString(mapper.writeValueAsString(update),
                        ContentType.APPLICATION_JSON)).returnResponse();
            assertEquals(200, resp.getStatusLine().getStatusCode());
        }

        resp = this.execute(Request.Get("http://localhost:8080/entity/" + id + "/versions")).returnResponse();
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

}
