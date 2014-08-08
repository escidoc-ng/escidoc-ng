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
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.objecthunter.larch.LarchServerConfiguration;
import net.objecthunter.larch.integration.helpers.NullOutputStream;
import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.model.WorkspacePermissions;
import net.objecthunter.larch.model.WorkspacePermissions.Permission;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpMethod;
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

    protected boolean wsCreated = false;

    final PrintStream sysOut = System.out;

    final PrintStream sysErr = System.err;

    @Autowired
    protected ObjectMapper mapper;

    private HttpHost localhost = new HttpHost("localhost", 8080, "http");

    private Executor adminExecutor = Executor.newInstance().auth(localhost, "admin", "admin").authPreemptive(
            localhost);

    private HttpClient httpClient = HttpClients.createDefault();

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

    protected String createUser(String password) throws IOException {
        String name = RandomStringUtils.randomAlphabetic(5);
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

    /**
     * Create Workspace where users with provided usernames have all rights.
     * 
     * @param usernames
     * @return String workspaceId
     * @throws Exception
     */
    private String createWorkspaceFor(List<String> usernames, EnumSet<Permission> permissionSet) throws Exception {
        final Workspace ws = new Workspace();
        if (usernames != null && !usernames.isEmpty() && permissionSet != null && !permissionSet.isEmpty()) {
            final WorkspacePermissions permissions = new WorkspacePermissions();
            for (String username : usernames) {
                permissions.setPermissions(username, permissionSet);
            }
            ws.setPermissions(permissions);
        }
        ws.setId(RandomStringUtils.randomAlphanumeric(16));
        ws.setOwner("foo");
        ws.setName("bar");
        HttpResponse resp = executeAsAdmin(Request.Post(hostUrl + "/workspace")
                .bodyString(this.mapper.writeValueAsString(ws), ContentType.APPLICATION_JSON));

        return EntityUtils.toString(resp.getEntity());
    }

    /**
     * Create Workspace where user with provided username has all rights.
     * 
     * @param username
     * @return String workspaceId
     * @throws Exception
     */
    protected String createAllPermissionWorkspaceForUser(String username) throws Exception {
        if (username == null) {
            return createWorkspaceFor(null, null);
        } else {
            return createWorkspaceFor(new ArrayList<String>() {

                {
                    add(username);
                }
            }, EnumSet.allOf(WorkspacePermissions.Permission.class));
        }
    }

    /**
     * Create Workspace where user with provided username has provided rights.
     * 
     * @param username
     * @param permission permission
     * @return String workspaceId
     * @throws Exception
     */
    protected String createPermissionWorkspaceForUser(String username, Permission... permission) throws Exception {
        if (username == null) {
            return createWorkspaceFor(null, null);
        } else {
            return createWorkspaceFor(new ArrayList<String>() {

                {
                    add(username);
                }
            }, EnumSet.copyOf(new ArrayList<Permission>() {

                {
                    for (int i = 0; i < permission.length; i++) {
                        add(permission[i]);
                    }
                }
            }));
        }
    }

    protected HttpResponse executeAsAdmin(Request req) throws IOException {
        return this.adminExecutor.execute(req).returnResponse();
    }

    protected HttpResponse executeAsUser(HttpMethod method, String url, String body, String username,
            String password)
            throws IOException {
        HttpUriRequest request = getRequest(method, url, body);
        if (request != null) {
            byte[] encodedBytes = Base64.encodeBase64((username + ":" + password).getBytes());
            String authorization = "Basic " + new String(encodedBytes);
            request.setHeader("Authorization", authorization);
            return httpClient.execute(request);
        }
        return null;
    }

    protected HttpResponse executeAsAnonymous(HttpMethod method, String url, String body) throws IOException {
        HttpUriRequest request = getRequest(method, url, body);
        if (request != null) {
            return httpClient.execute(request);
        }
        return null;
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

    private HttpUriRequest getRequest(HttpMethod method, String url, String body) throws IOException {
        if (method.equals(HttpMethod.POST)) {
            HttpPost httpPost =
                    new HttpPost(url);
            if (StringUtils.isNotBlank(body)) {
                httpPost.setEntity(new StringEntity(body));
                httpPost.setHeader("Content-type", "application/json; charset=UTF-8");
            }
            return httpPost;
        }
        return null;
    }

}
