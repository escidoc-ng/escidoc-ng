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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.model.WorkspacePermissions.Permission;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizeEntityControllerIT extends AbstractLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeEntityControllerIT.class);

    private static Map<MissingPermission, String[]> usernames = new HashMap<MissingPermission, String[]>();

    private static String workspaceId = null;

    @Before
    public void prepareWorkspace() throws Exception {
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

    @Test
    public void testPatchEntity() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        String patchData = "{\"label\":\"otherLabel\"}";
        testAuth(HttpMethod.PATCH, workspaceUrl + workspaceId + "/entity/" + entity.getId(), patchData,
                MissingPermission.WRITE_PENDING_METADATA);
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(HttpMethod.PATCH, workspaceUrl + workspaceId + "/entity/" + entity.getId(), patchData,
                MissingPermission.WRITE_SUBMITTED_METADATA, Entity.STATE_SUBMITTED, entity.getId());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(HttpMethod.PATCH, workspaceUrl + workspaceId + "/entity/" + entity.getId(), patchData,
                MissingPermission.WRITE_PUBLISHED_METADATA, Entity.STATE_PUBLISHED, entity.getId());
    }

    @Test
    public void testCreateEntity() throws Exception {
        Entity e = createFixtureEntity();
        e.setWorkspaceId(workspaceId);
        testAuth(HttpMethod.POST, workspaceUrl + workspaceId + "/entity", mapper
                .writeValueAsString(e), MissingPermission.WRITE_WORKSPACE);
    }

    @Test
    public void testRetrieveEntity() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId(), null,
                MissingPermission.READ_PENDING_METADATA);
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId(), null,
                MissingPermission.READ_SUBMITTED_METADATA);
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId(), null,
                null);
    }

    @Test
    public void testRetrieveVersion() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/version/2", null,
                MissingPermission.READ_PENDING_METADATA);
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/version/2", null,
                MissingPermission.READ_SUBMITTED_METADATA);
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/version/2", null,
                null);
    }

    @Test
    public void testRetrieveVersions() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/versions", null,
                true);
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/versions", null,
                true);
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/versions", null,
                true);
    }

    private void testAuth(HttpMethod method, String url, String object, MissingPermission neededPermission)
            throws IOException {
        testAuth(method, url, object, neededPermission, null, null, false);
    }

    private void testAuth(HttpMethod method, String url, String object, boolean adminOnly)
            throws IOException {
        testAuth(method, url, object, null, null, null, adminOnly);
    }

    private void testAuth(HttpMethod method, String url, String object, MissingPermission neededPermission,
            String resetState, String resetStateEntityId)
            throws IOException {
        testAuth(method, url, object, neededPermission, resetState, resetStateEntityId, false);
    }

    private void testAuth(HttpMethod method, String url, String object, MissingPermission neededPermission,
            String resetState, String resetStateEntityId, boolean adminOnly)
            throws IOException {
        int okStatus = 200;
        if (HttpMethod.PUT.equals(method) || HttpMethod.POST.equals(method)) {
            okStatus = 201;
        }
        // try as admin
        HttpResponse resp =
                this.executeAsUser(method, url, object, adminUsername, adminPassword);
        assertEquals(okStatus, resp.getStatusLine().getStatusCode());
        EntityUtils.toString(resp.getEntity());
        resetState(resetState, resetStateEntityId);
        // try as user with all rights
        resp =
                this.executeAsUser(method, url, object, usernames.get(MissingPermission.NONE)[0], usernames
                        .get(MissingPermission.NONE)[1]);
        if (adminOnly) {
            assertEquals(403, resp.getStatusLine().getStatusCode());
        } else {
            assertEquals(okStatus, resp.getStatusLine().getStatusCode());
        }
        EntityUtils.toString(resp.getEntity());
        // try as user with no rights
        resp =
                this.executeAsUser(method, url, object, usernames.get(MissingPermission.ALL)[0], usernames
                        .get(MissingPermission.ALL)[1]);
        if (adminOnly) {
            assertEquals(403, resp.getStatusLine().getStatusCode());
        } else if (neededPermission == null) {
            assertEquals(okStatus, resp.getStatusLine().getStatusCode());
        } else {
            assertEquals(403, resp.getStatusLine().getStatusCode());
        }
        EntityUtils.toString(resp.getEntity());
        resetState(resetState, resetStateEntityId);
        // try as anonymous user
        resp =
                this.executeAsAnonymous(method, url, object);
        if (neededPermission == null && !adminOnly) {
            assertEquals(okStatus, resp.getStatusLine().getStatusCode());
        } else {
            assertEquals(401, resp.getStatusLine().getStatusCode());
        }
        EntityUtils.toString(resp.getEntity());
        resetState(resetState, resetStateEntityId);
        // try as user with wrong workspace rights
        if (neededPermission != null) {
            resp =
                    this.executeAsUser(method, url, object, usernames.get(neededPermission)[0], usernames
                            .get(neededPermission)[1]);
            assertEquals(403, resp.getStatusLine().getStatusCode());
            EntityUtils.toString(resp.getEntity());
            resetState(resetState, resetStateEntityId);
        }
        // try as user with correct workspace rights
        for (Entry<MissingPermission, String[]> e : usernames.entrySet()) {
            if (neededPermission == null || (!e.getKey().equals(MissingPermission.ALL) &&
                    !e.getKey().equals(neededPermission))) {
                resp =
                        this.executeAsUser(method, url, object, e.getValue()[0], e.getValue()[1]);
                if (adminOnly) {
                    assertEquals(403, resp.getStatusLine().getStatusCode());
                } else {
                    assertEquals(okStatus, resp.getStatusLine().getStatusCode());
                }
                EntityUtils.toString(resp.getEntity());
                resetState(resetState, resetStateEntityId);
            }
        }
    }

    private void resetState(String resetState, String resetStateEntityId) throws IOException {
        if (StringUtils.isNotBlank(resetState) && StringUtils.isNotBlank(resetStateEntityId)) {
            String urlSuffix = null;
            if (resetState.equals(Entity.STATE_SUBMITTED)) {
                urlSuffix = "submit";
            } else if (resetState.equals(Entity.STATE_PUBLISHED)) {
                urlSuffix = "publish";
            } else {
                throw new IOException("unknown state to reset");
            }
            HttpResponse resp =
                    this.executeAsAdmin(
                            Request.Post(workspaceUrl + workspaceId + "/entity/" + resetStateEntityId + "/" +
                                    urlSuffix));
            assertEquals(200, resp.getStatusLine().getStatusCode());
        }
    }

    /**
     * @param status
     * @return String entityId
     */
    private Entity createEntity(String status, String workspaceId) throws Exception {
        if (StringUtils.isBlank(status) ||
                (!status.equals(Entity.STATE_PENDING) && !status.equals(Entity.STATE_SUBMITTED) &&
                        !status.equals(Entity.STATE_PUBLISHED) && !status.equals(Entity.STATE_WITHDRAWN))) {
            throw new Exception("given status not valid");
        }
        Entity e = createFixtureEntity();
        e.setWorkspaceId(workspaceId);
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
                            Request.Post(workspaceUrl + workspaceId + "/entity/" + entityId + "/submit"));
            assertEquals(200, resp.getStatusLine().getStatusCode());
            if (!status.equals(Entity.STATE_SUBMITTED)) {
                // publish
                resp =
                        this.executeAsAdmin(
                                Request.Post(workspaceUrl + workspaceId + "/entity/" + entityId + "/publish"));
                assertEquals(200, resp.getStatusLine().getStatusCode());
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
        return fetched;
    }

    public enum MissingPermission {
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
