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

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.objecthunter.larch.LarchServerConfiguration;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.model.WorkspacePermissions;
import net.objecthunter.larch.model.WorkspacePermissions.Permission;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Class holds methods used by auth-tests.<br>
 * <br>
 * -create workspace where different users have different rights <br>
 * -execute http-request with different auth-headers <br>
 * -execute same request with different users and check response
 * 
 * @author mih
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = LarchServerConfiguration.class)
@IntegrationTest
@WebAppConfiguration
@ActiveProfiles("fs")
public abstract class AbstractAuthorizeLarchIT extends AbstractLarchIT {

    protected static String workspaceId = null;

    /**
     * Holds users with different rights. key: Permission the user does not have, value: String[2]: user password
     */
    protected static Map<MissingPermission, String[]> usernames = new HashMap<MissingPermission, String[]>();

    private static int methodCounter = 0;

    @Before
    public void initialize() throws Exception {
        if (methodCounter == 0) {
            prepareWorkspace();
            methodCounter++;
        }
    }

    /**
     * Create Workspace.<br>
     * Create users having different rights in the workspace.<br>
     * store workspaceId in variable.<br>
     * 
     * @throws Exception
     */
    private void prepareWorkspace() throws Exception {
        // create Workspace
        final Workspace ws = new Workspace();
        ws.setId(RandomStringUtils.randomAlphanumeric(16));
        ws.setOwner("foo");
        ws.setName("bar");
        HttpResponse resp = Request.Post(workspaceUrl)
                .bodyString(this.mapper.writeValueAsString(ws), ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();

        workspaceId = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());
        assertNotNull(workspaceId);
        assertEquals(ws.getId(), workspaceId);

        // create users
        for (MissingPermission missingPermission : MissingPermission.values()) {
            usernames.put(missingPermission, new String[] { createUser("ttestt"), "ttestt" });
        }

        // create permissions for users in workspace
        for (Entry<MissingPermission, String[]> e : usernames.entrySet()) {
            if (e.getKey().equals(MissingPermission.NONE)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0], null);
            } else if (e.getKey().equals(MissingPermission.READ_PENDING_BINARY)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0], Permission.READ_PENDING_BINARY);
            } else if (e.getKey().equals(MissingPermission.READ_PENDING_METADATA)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0], Permission.READ_PENDING_METADATA);
            } else if (e.getKey().equals(MissingPermission.READ_PUBLISHED_BINARY)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0], Permission.READ_PUBLISHED_BINARY);
            } else if (e.getKey().equals(MissingPermission.READ_PUBLISHED_METADATA)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0], Permission.READ_PUBLISHED_METADATA);
            } else if (e.getKey().equals(MissingPermission.READ_SUBMITTED_BINARY)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0], Permission.READ_SUBMITTED_BINARY);
            } else if (e.getKey().equals(MissingPermission.READ_SUBMITTED_METADATA)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0], Permission.READ_SUBMITTED_METADATA);
            } else if (e.getKey().equals(MissingPermission.READ_WITHDRAWN_BINARY)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0], Permission.READ_WITHDRAWN_BINARY);
            } else if (e.getKey().equals(MissingPermission.READ_WITHDRAWN_METADATA)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0], Permission.READ_WITHDRAWN_METADATA);
            } else if (e.getKey().equals(MissingPermission.READ_WORKSPACE)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0], Permission.READ_WORKSPACE);
            } else if (e.getKey().equals(MissingPermission.WRITE_PENDING_BINARY)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0], Permission.WRITE_PENDING_BINARY);
            } else if (e.getKey().equals(MissingPermission.WRITE_PENDING_METADATA)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0], Permission.WRITE_PENDING_METADATA);
            } else if (e.getKey().equals(MissingPermission.WRITE_PUBLISHED_BINARY)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0], Permission.WRITE_PUBLISHED_BINARY);
            } else if (e.getKey().equals(MissingPermission.WRITE_PUBLISHED_METADATA)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0],
                        Permission.WRITE_PUBLISHED_METADATA);
            } else if (e.getKey().equals(MissingPermission.WRITE_SUBMITTED_BINARY)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0], Permission.WRITE_SUBMITTED_BINARY);
            } else if (e.getKey().equals(MissingPermission.WRITE_SUBMITTED_METADATA)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0],
                        Permission.WRITE_SUBMITTED_METADATA);
            } else if (e.getKey().equals(MissingPermission.WRITE_WITHDRAWN_BINARY)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0], Permission.WRITE_WITHDRAWN_BINARY);
            } else if (e.getKey().equals(MissingPermission.WRITE_WITHDRAWN_METADATA)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0],
                        Permission.WRITE_WITHDRAWN_METADATA);
            } else if (e.getKey().equals(MissingPermission.WRITE_WORKSPACE)) {
                createMissingPermissionRightsForUser(workspaceId, e.getValue()[0], Permission.WRITE_WORKSPACE);
            }
        }
    }

    /**
     * Execute Http-Request as user with given username/password.
     * 
     * @param method HttpMethod
     * @param url
     * @param body String-body for PUT/POST requests
     * @param username
     * @param password
     * @return HttpResponse
     * @throws IOException
     */
    protected HttpResponse executeAsUser(HttpMethod method, String url, String body, String username,
            String password)
            throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpUriRequest request = getRequest(method, url, body);
        if (request != null) {
            byte[] encodedBytes = Base64.encodeBase64((username + ":" + password).getBytes());
            String authorization = "Basic " + new String(encodedBytes);
            request.setHeader("Authorization", authorization);
            return httpClient.execute(request);
        }
        return null;
    }

    /**
     * Execute Http-Request with no auth-header (anonymous).
     * 
     * @param method HttpMethod
     * @param url
     * @param body String-body for PUT/POST requests
     * @return
     * @throws IOException
     */
    protected HttpResponse executeAsAnonymous(HttpMethod method, String url, String body) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpUriRequest request = getRequest(method, url, body);
        if (request != null) {
            return httpClient.execute(request);
        }
        return null;
    }

    /**
     * Create Workspace-Rights where user with provided username has all rights except for provided permission.
     * 
     * @param username
     * @param permission permission
     * @return String workspaceId
     * @throws Exception
     */
    protected void createMissingPermissionRightsForUser(String workspaceId, String username, Permission permission)
            throws Exception {
        // try to retrieve workspace
        HttpResponse resp = Request.Get(workspaceUrl + workspaceId)
                .execute()
                .returnResponse();
        assertEquals(200, resp.getStatusLine().getStatusCode());
        Workspace fetched = this.mapper.readValue(resp.getEntity().getContent(), Workspace.class);

        // Set permissions for user
        WorkspacePermissions permissions = fetched.getPermissions();
        if (permissions == null) {
            permissions = new WorkspacePermissions();
        }
        permissions.setPermissions(username, EnumSet.copyOf(new ArrayList<Permission>() {

            {
                for (Permission perm : Permission.values()) {
                    if (permission == null || !permission.equals(perm)) {
                        add(perm);
                    }
                }
            }
        }));
        fetched.setPermissions(permissions);

        // update workspace
        resp = Request.Put(workspaceUrl + workspaceId)
                .bodyString(this.mapper.writeValueAsString(fetched), ContentType.APPLICATION_JSON)
                .execute()
                .returnResponse();
        assertEquals(200, resp.getStatusLine().getStatusCode());

    }

    /**
     * Get HttpUriRequest for given parameters.
     * 
     * @param method
     * @param url
     * @param body
     * @return
     * @throws IOException
     */
    private HttpUriRequest getRequest(HttpMethod method, String url, String body) throws IOException {
        if (method.equals(HttpMethod.POST)) {
            HttpPost httpPost =
                    new HttpPost(url);
            if (StringUtils.isNotBlank(body)) {
                httpPost.setEntity(new StringEntity(body));
                httpPost.setHeader("Content-type", "application/json; charset=UTF-8");
            }
            return httpPost;
        } else if (method.equals(HttpMethod.PUT)) {
            HttpPut httpPut =
                    new HttpPut(url);
            if (StringUtils.isNotBlank(body)) {
                httpPut.setEntity(new StringEntity(body));
                httpPut.setHeader("Content-type", "application/json; charset=UTF-8");
            }
            return httpPut;
        } else if (method.equals(HttpMethod.PATCH)) {
            HttpPatch httpPatch =
                    new HttpPatch(url);
            if (StringUtils.isNotBlank(body)) {
                httpPatch.setEntity(new StringEntity(body));
                httpPatch.setHeader("Content-type", "application/json; charset=UTF-8");
            }
            return httpPatch;
        } else if (method.equals(HttpMethod.GET)) {
            return new HttpGet(url);
        } else if (method.equals(HttpMethod.DELETE)) {
            return new HttpDelete(url);
        }
        return null;
    }

    /**
     * See testAuth(HttpMethod method, String url, String object, MissingPermission neededPermission, String
     * resetState, String resetStateEntityId, boolean adminOnly)
     * 
     * @param method
     * @param url
     * @param object
     * @param neededPermission
     * @throws Exception
     */
    protected void testAuth(HttpMethod method, String url, String object, MissingPermission neededPermission)
            throws Exception {
        testAuth(method, url, object, neededPermission, null, null, false);
    }

    /**
     * See testAuth(HttpMethod method, String url, String object, MissingPermission neededPermission, String
     * resetState, String resetStateEntityId, boolean adminOnly)
     * 
     * @param method
     * @param url
     * @param object
     * @param neededPermission
     * @throws Exception
     */
    protected void testAuth(HttpMethod method, String url, String object, boolean adminOnly)
            throws Exception {
        testAuth(method, url, object, null, null, null, adminOnly);
    }

    /**
     * See testAuth(HttpMethod method, String url, String object, MissingPermission neededPermission, String
     * resetState, String resetStateEntityId, boolean adminOnly)
     * 
     * @param method
     * @param url
     * @param object
     * @param neededPermission
     * @throws Exception
     */
    protected void testAuth(HttpMethod method, String url, String object, MissingPermission neededPermission,
            String resetState, String resetStateEntityId)
            throws Exception {
        testAuth(method, url, object, neededPermission, resetState, resetStateEntityId, false);
    }

    /**
     * Tests calling the given url with different user-permissions.<br>
     * neededPermission indicates the permission that is needed to be allowed to call the request.<br>
     * If neededPermission is null, everybody may call the url. If adminOnly is true, only admin may call the url,
     * independent of workspace-rights.
     * 
     * @param method
     * @param url
     * @param object
     * @param neededPermission
     * @param resetState to with state the entity should get reset after url was called
     * @param resetStateEntityId id of the entity to reset the state
     * @param adminOnly
     * @throws Exception
     */
    protected void testAuth(HttpMethod method, String url, String object, MissingPermission neededPermission,
            String resetState, String resetStateEntityId, boolean adminOnly)
            throws Exception {
        int okStatus = 200;
        if (HttpMethod.POST.equals(method)) {
            okStatus = 201;
        }
        // try as admin
        HttpResponse resp =
                this.executeAsUser(method, url, object, adminUsername, adminPassword);
        String response = EntityUtils.toString(resp.getEntity());
        assertEquals(okStatus, resp.getStatusLine().getStatusCode());
        resetState(resetState, resetStateEntityId);
        // try as user with all rights
        resp =
                this.executeAsUser(method, url, object, usernames.get(MissingPermission.NONE)[0], usernames
                        .get(MissingPermission.NONE)[1]);
        response = EntityUtils.toString(resp.getEntity());
        if (adminOnly) {
            assertEquals(403, resp.getStatusLine().getStatusCode());
        } else {
            assertEquals(okStatus, resp.getStatusLine().getStatusCode());
        }
        resetState(resetState, resetStateEntityId);
        // try as user with no rights
        resp =
                this.executeAsUser(method, url, object, usernames.get(MissingPermission.ALL)[0], usernames
                        .get(MissingPermission.ALL)[1]);
        response = EntityUtils.toString(resp.getEntity());
        if (adminOnly) {
            assertEquals(403, resp.getStatusLine().getStatusCode());
        } else if (neededPermission == null) {
            assertEquals(okStatus, resp.getStatusLine().getStatusCode());
        } else {
            assertEquals(403, resp.getStatusLine().getStatusCode());
        }
        resetState(resetState, resetStateEntityId);
        // try as anonymous user
        resp =
                this.executeAsAnonymous(method, url, object);
        response = EntityUtils.toString(resp.getEntity());
        if (neededPermission == null && !adminOnly) {
            assertEquals(okStatus, resp.getStatusLine().getStatusCode());
        } else {
            assertEquals(401, resp.getStatusLine().getStatusCode());
        }
        resetState(resetState, resetStateEntityId);
        // try as user with wrong workspace rights
        if (neededPermission != null) {
            resp =
                    this.executeAsUser(method, url, object, usernames.get(neededPermission)[0], usernames
                            .get(neededPermission)[1]);
            response = EntityUtils.toString(resp.getEntity());
            assertEquals(403, resp.getStatusLine().getStatusCode());
            resetState(resetState, resetStateEntityId);
        }
        // try as user with correct workspace rights
        for (Entry<MissingPermission, String[]> e : usernames.entrySet()) {
            if (neededPermission == null || (!e.getKey().equals(MissingPermission.ALL) &&
                    !e.getKey().equals(neededPermission))) {
                resp =
                        this.executeAsUser(method, url, object, e.getValue()[0], e.getValue()[1]);
                response = EntityUtils.toString(resp.getEntity());
                if (adminOnly) {
                    assertEquals(403, resp.getStatusLine().getStatusCode());
                } else {
                    assertEquals(okStatus, resp.getStatusLine().getStatusCode());
                }
                resetState(resetState, resetStateEntityId);
            }
        }
    }

    /**
     * Resets the state of an entity.
     * 
     * @param resetState
     * @param resetStateEntityId
     * @throws Exception
     */
    private void resetState(String resetState, String resetStateEntityId) throws Exception {
        if (StringUtils.isNotBlank(resetState) && StringUtils.isNotBlank(resetStateEntityId)) {
            // check if entity is there
            Entity e = null;
            HttpResponse resp =
                    this.executeAsAdmin(
                            Request.Get(workspaceUrl + workspaceId + "/entity/" + resetStateEntityId));
            if (resp.getStatusLine().getStatusCode() == HttpStatus.NOT_FOUND.value()) {
                // recreate entity
                e = createFixtureEntity();
                e.setWorkspaceId(workspaceId);
                e.setId(resetStateEntityId);
                resp =
                        this.executeAsAdmin(
                                Request.Post(workspaceUrl + workspaceId + "/entity").bodyString(
                                        mapper.writeValueAsString(e),
                                        ContentType.APPLICATION_JSON));
                assertEquals(201, resp.getStatusLine().getStatusCode());
            } else {
                assertEquals(200, resp.getStatusLine().getStatusCode());
                e = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            }
            String urlSuffix = null;
            if (resetState.equals(Entity.STATE_SUBMITTED)) {
                urlSuffix = "submit";
            } else if (resetState.equals(Entity.STATE_PUBLISHED)) {
                urlSuffix = "publish";
            } else if (resetState.equals(Entity.STATE_PENDING)) {
                if (!Entity.STATE_PENDING.equals(e.getState())) {
                    e.setLabel("changed");
                    resp =
                            this.executeAsAdmin(
                                    Request.Put(workspaceUrl + workspaceId + "/entity/" + e.getId()).bodyString(
                                            mapper.writeValueAsString(e),
                                            ContentType.APPLICATION_JSON));
                    assertEquals(200, resp.getStatusLine().getStatusCode());
                }
                return;
            } else {
                return;
            }
            resp =
                    this.executeAsAdmin(
                            Request.Put(workspaceUrl + workspaceId + "/entity/" + resetStateEntityId + "/" +
                                    urlSuffix));
            assertEquals(200, resp.getStatusLine().getStatusCode());
        }
    }

    protected enum MissingPermission {
        READ_PENDING_METADATA,
        READ_SUBMITTED_METADATA,
        READ_PUBLISHED_METADATA,
        READ_WITHDRAWN_METADATA,
        WRITE_PENDING_METADATA,
        WRITE_SUBMITTED_METADATA,
        WRITE_PUBLISHED_METADATA,
        WRITE_WITHDRAWN_METADATA,
        READ_PENDING_BINARY,
        READ_SUBMITTED_BINARY,
        READ_PUBLISHED_BINARY,
        READ_WITHDRAWN_BINARY,
        WRITE_PENDING_BINARY,
        WRITE_SUBMITTED_BINARY,
        WRITE_PUBLISHED_BINARY,
        WRITE_WITHDRAWN_BINARY,
        READ_WORKSPACE,
        WRITE_WORKSPACE,
        ALL,
        NONE;
    }

}
