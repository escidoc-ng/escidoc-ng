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

import static net.objecthunter.larch.test.util.Fixtures.WORKSPACE_ID;
import static net.objecthunter.larch.test.util.Fixtures.createFixtureEntity;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.objecthunter.larch.LarchServerConfiguration;
import net.objecthunter.larch.integration.helpers.NullOutputStream;
import net.objecthunter.larch.model.AlternativeIdentifier;
import net.objecthunter.larch.model.AlternativeIdentifier.IdentifierType;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.UserRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = LarchServerConfiguration.class)
@IntegrationTest
@WebAppConfiguration
@ActiveProfiles("fs")
public abstract class AbstractLarchIT {

    protected static final int port = 8080;

    protected static final String hostUrl = "http://localhost:" + port + "/";

    protected static final String workspaceUrl = hostUrl + "workspace/";

    protected static final String defaultWorkspaceUrl = workspaceUrl + WORKSPACE_ID + "/";

    protected static final String entityUrl = defaultWorkspaceUrl + "entity/";

    protected static final String userUrl = hostUrl + "user/";

    protected static final String confirmUrl = hostUrl + "confirm/";

    protected static final String adminUsername = "admin";

    protected static final String adminPassword = "admin";

    protected boolean wsCreated = false;

    final PrintStream sysOut = System.out;

    final PrintStream sysErr = System.err;

    @Autowired
    protected ObjectMapper mapper;

    private HttpHost localhost = new HttpHost("localhost", 8080, "http");

    private Executor adminExecutor = Executor.newInstance().auth(localhost, adminUsername, adminPassword)
            .authPreemptive(
                    localhost);

    @Before
    public void resetSystOutErr() throws Exception {
        this.showLog();
        if (!wsCreated) {
            // create default workspace
            Workspace ws = new Workspace();
            ws.setId(WORKSPACE_ID);
            ws.setName("Test Workspace");
            Request r = Request.Post(hostUrl + "workspace")
                    .bodyString(mapper.writeValueAsString(ws), ContentType.APPLICATION_JSON);
            this.executeAsAdmin(r);
            wsCreated = true;
        }
    }

    protected String createUser(String name, String password) throws IOException {
        if (StringUtils.isBlank(name)) {
            name = RandomStringUtils.randomAlphabetic(5);
        }
        HttpResponse resp =
                executeAsAdmin(
                Request.Post(userUrl)
                        .body(MultipartEntityBuilder.create()
                                .addTextBody("name", name)
                                .addTextBody("first_name", "test")
                                .addTextBody("last_name", "test")
                                .addTextBody("email", name + "@fiz.de")
                                .addTextBody("groups", "ROLE_USER")
                                .build()
                        ));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        final String token = EntityUtils.toString(resp.getEntity());
        resp =
                executeAsAdmin(
                Request.Post(confirmUrl + token)
                        .body(MultipartEntityBuilder.create()
                                .addTextBody("password", password)
                                .addTextBody("passwordRepeat", password)
                                .build()
                        ));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        return name;
    }

    protected UserRequest createUserRequest(String name, String password) throws IOException {
        if (StringUtils.isBlank(name)) {
            name = RandomStringUtils.randomAlphabetic(5);
        }
        HttpResponse resp =
                executeAsAdmin(
                Request.Post(userUrl)
                        .body(MultipartEntityBuilder.create()
                                .addTextBody("name", name)
                                .addTextBody("first_name", "test")
                                .addTextBody("last_name", "test")
                                .addTextBody("email", name + "@fiz.de")
                                .addTextBody("groups", "ROLE_USER")
                                .build()
                        ));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        final String token = EntityUtils.toString(resp.getEntity());
        UserRequest userRequest = new UserRequest();
        userRequest.setToken(token);
        User user = new User();
        user.setName(name);
        userRequest.setUser(user);
        return userRequest;
    }

    /**
     * @param status
     * @return String entityId
     */
    protected Entity createEntity(String status, String workspaceId) throws Exception {
        if (StringUtils.isBlank(status) ||
                (!status.equals(Entity.STATE_PENDING) && !status.equals(Entity.STATE_SUBMITTED) &&
                        !status.equals(Entity.STATE_PUBLISHED) && !status.equals(Entity.STATE_WITHDRAWN))) {
            throw new Exception("given status not valid");
        }
        String publishId = null;
        Entity e = createFixtureEntity();
        e.setWorkspaceId(workspaceId);
        AlternativeIdentifier identifier = new AlternativeIdentifier();
        identifier.setType(IdentifierType.DOI.name);
        identifier.setValue("testdoi");
        List<AlternativeIdentifier> identifiers = new ArrayList<AlternativeIdentifier>();
        identifiers.add(identifier);
        e.setAlternativeIdentifiers(identifiers);
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(workspaceUrl + workspaceId + "/entity").bodyString(
                                mapper.writeValueAsString(e),
                                ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        String entityId = EntityUtils.toString(resp.getEntity());
        e.setLabel("other");
        resp =
                this.executeAsAdmin(
                        Request.Put(workspaceUrl + workspaceId + "/entity/" + entityId).bodyString(
                                mapper.writeValueAsString(e),
                                ContentType.APPLICATION_JSON));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        if (!status.equals(Entity.STATE_PENDING)) {
            // submit
            resp =
                    this.executeAsAdmin(
                            Request.Put(workspaceUrl + workspaceId + "/entity/" + entityId + "/submit"));
            assertEquals(200, resp.getStatusLine().getStatusCode());
            if (!status.equals(Entity.STATE_SUBMITTED)) {
                // publish
                resp =
                        this.executeAsAdmin(
                                Request.Put(workspaceUrl + workspaceId + "/entity/" + entityId + "/publish"));
                assertEquals(200, resp.getStatusLine().getStatusCode());
                publishId = EntityUtils.toString(resp.getEntity());
                if (!status.equals(Entity.STATE_PUBLISHED)) {
                    // // withdraw
                    // resp =
                    // this.executeAsAdmin(
                    // Request.Post(workspaceUrl + workspaceId + "/entity" + entityId + "/withdraw"));
                    // assertEquals(200, resp.getStatusLine().getStatusCode());
                }
            }
        }
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(workspaceUrl + workspaceId + "/entity/" + entityId));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        fetched.setPublishId(publishId);
        return fetched;
    }

    protected HttpResponse executeAsAdmin(Request req) throws IOException {
        return this.adminExecutor.execute(req).returnResponse();
    }

    protected void checkResponseError(HttpResponse response, int statusCode,
            Class<? extends Exception> expectedException,
            String message)
            throws IOException {
        JsonNode error = mapper.readTree(response.getEntity().getContent());
        assertEquals(statusCode, response.getStatusLine().getStatusCode());
        assertEquals(statusCode, error.get("status").asInt());
        assertEquals(expectedException.getName(), error.get("exception").asText());
        assertEquals(message, error.get("message").asText());
    }

    protected void hideLog() {
        System.setOut(new PrintStream(NullOutputStream.getInstance()));
        System.setErr(new PrintStream(NullOutputStream.getInstance()));
    }

    protected void showLog() {
        System.setOut(sysOut);
        System.setErr(sysErr);
    }

}
