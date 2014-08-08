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
import net.objecthunter.larch.model.WorkspacePermissions.Permission;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizeEntityControllerIT extends AbstractLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeEntityControllerIT.class);

    private static int usercount = 5;

    private static String[][] usernames = new String[usercount][2];

    @Before
    public void createUsers() throws Exception {
        for (int i = 0; i < usercount; i++) {
            usernames[i] = new String[] { createUser("ttestt"), "ttestt" };
        }
    }

    @Test
    public void testCreateEntityAsAdmin() throws Exception {
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl).bodyString(mapper.writeValueAsString(createFixtureEntity()),
                                ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testCreateEntityAsUserWithAllWorkspaceRights() throws Exception {
        String workspaceId = createAllPermissionWorkspaceForUser(usernames[0][0]);
        HttpResponse resp =
                this.executeAsUser(HttpMethod.POST, workspaceUrl + workspaceId + "/entity", mapper
                        .writeValueAsString(createFixtureEntity()), usernames[0][0], usernames[0][1]);
        assertEquals(201, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testCreateEntityAsUserWithoutWorkspaceRights() throws Exception {
        String workspaceId = createAllPermissionWorkspaceForUser(usernames[0][0]);
        HttpResponse resp =
                this.executeAsUser(HttpMethod.POST, workspaceUrl + workspaceId + "/entity", mapper
                        .writeValueAsString(createFixtureEntity()), usernames[1][0], usernames[1][1]);
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testCreateEntityAsAnonymousUser() throws Exception {
        String workspaceId = createAllPermissionWorkspaceForUser(null);
        HttpResponse resp =
                this.executeAsAnonymous(HttpMethod.POST, workspaceUrl + workspaceId + "/entity", mapper
                        .writeValueAsString(createFixtureEntity()));
        assertEquals(401, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testCreateEntityAsUserWithWrongWorkspaceRights() throws Exception {
        String workspaceId = createPermissionWorkspaceForUser(usernames[0][0], Permission.WRITE_PUBLISHED_METADATA);
        HttpResponse resp =
                this.executeAsUser(HttpMethod.POST, workspaceUrl + workspaceId + "/entity", mapper
                        .writeValueAsString(createFixtureEntity()), usernames[0][0], usernames[0][1]);
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testCreateEntityAsUserWithCorrectWorkspaceRights() throws Exception {
        String workspaceId =
                createPermissionWorkspaceForUser(usernames[0][0], Permission.WRITE_PUBLISHED_METADATA,
                        Permission.WRITE_WORKSPACE);
        HttpResponse resp =
                this.executeAsUser(HttpMethod.POST, workspaceUrl + workspaceId + "/entity", mapper
                        .writeValueAsString(createFixtureEntity()), usernames[0][0], usernames[0][1]);
        assertEquals(201, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrievePendingEntityAsAdmin() throws Exception {
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl).bodyString(mapper.writeValueAsString(createFixtureEntity()),
                                ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
    }

}
