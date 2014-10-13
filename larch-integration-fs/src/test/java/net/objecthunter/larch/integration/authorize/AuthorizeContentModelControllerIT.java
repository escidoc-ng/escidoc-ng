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
 * WITHOUT WARRANTIES OR CONDITIONS OF ROLE_ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.objecthunter.larch.integration.authorize;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import net.objecthunter.larch.integration.helpers.AuthConfigurer;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.MissingPermission;
import net.objecthunter.larch.model.ContentModel;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.security.role.Level1AdminRole;
import net.objecthunter.larch.model.security.role.Role.RoleName;
import net.objecthunter.larch.model.security.role.UserAdminRole;
import net.objecthunter.larch.model.security.role.UserRole;
import net.objecthunter.larch.test.util.Fixtures;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizeContentModelControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeContentModelControllerIT.class);

    private static int methodCounter = 0;

    private static String password = "ttestt";

    private static Map<String, String[]> users = new HashMap<String, String[]>();

    @Before
    public void initialize() throws Exception {
        if (methodCounter == 0) {
            prepareRoles();
            methodCounter++;
        }
    }

    @Test
    public void testCreateContentModel() throws Exception {
        ContentModel contentModel = Fixtures.createContentModel();

        // level1 admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.POST, contentModelUrl, this.mapper
                        .writeValueAsString(contentModel),
                        users.get(RoleName.ROLE_LEVEL1_ADMIN.name())[0],
                        users.get(RoleName.ROLE_LEVEL1_ADMIN.name())[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());

        // admin
        resp =
                this.executeAsUser(HttpMethod.POST, contentModelUrl, this.mapper
                        .writeValueAsString(contentModel),
                        adminUsername, adminPassword, false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());

        // user-admin
        resp =
                this.executeAsUser(HttpMethod.POST, contentModelUrl, this.mapper
                        .writeValueAsString(contentModel),
                        users.get(RoleName.ROLE_USER_ADMIN.name())[0],
                        users.get(RoleName.ROLE_USER_ADMIN.name())[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        // user
        resp =
                this.executeAsUser(HttpMethod.POST, contentModelUrl, this.mapper
                        .writeValueAsString(contentModel),
                        users.get(RoleName.ROLE_USER.name())[0],
                        users.get(RoleName.ROLE_USER.name())[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrieveContentModel() throws Exception {
        // create content model
        String contentModelId = createContentModel();

        // level1 admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.GET, contentModelUrl + contentModelId, null,
                        users.get(RoleName.ROLE_LEVEL1_ADMIN.name())[0],
                        users.get(RoleName.ROLE_LEVEL1_ADMIN.name())[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());

        // admin
        resp =
                this.executeAsUser(HttpMethod.GET, contentModelUrl + contentModelId, null,
                        adminUsername, adminPassword, false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());

        // user-admin
        resp =
                this.executeAsUser(HttpMethod.GET, contentModelUrl + contentModelId, null,
                        users.get(RoleName.ROLE_USER_ADMIN.name())[0],
                        users.get(RoleName.ROLE_USER_ADMIN.name())[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        // user
        resp =
                this.executeAsUser(HttpMethod.GET, contentModelUrl + contentModelId, null,
                        users.get(RoleName.ROLE_USER.name())[0],
                        users.get(RoleName.ROLE_USER.name())[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testDeleteContentModel() throws Exception {
        // level1 admin
        String contentModelId = createContentModel();
        HttpResponse resp =
                this.executeAsUser(HttpMethod.DELETE, contentModelUrl + contentModelId, null,
                        users.get(RoleName.ROLE_LEVEL1_ADMIN.name())[0],
                        users.get(RoleName.ROLE_LEVEL1_ADMIN.name())[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());

        // admin
        contentModelId = createContentModel();
        resp =
                this.executeAsUser(HttpMethod.DELETE, contentModelUrl + contentModelId, null,
                        adminUsername, adminPassword, false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());

        // user-admin
        contentModelId = createContentModel();
        resp =
                this.executeAsUser(HttpMethod.DELETE, contentModelUrl + contentModelId, null,
                        users.get(RoleName.ROLE_USER_ADMIN.name())[0],
                        users.get(RoleName.ROLE_USER_ADMIN.name())[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        // user
        contentModelId = createContentModel();
        resp =
                this.executeAsUser(HttpMethod.DELETE, contentModelUrl + contentModelId, null,
                        users.get(RoleName.ROLE_USER.name())[0],
                        users.get(RoleName.ROLE_USER.name())[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    private void prepareRoles() throws Exception {
        String level1Id = createLevel1();
        String level2Id = createLevel2(level1Id);
        users.put(RoleName.ROLE_USER_ADMIN.name(), new String[] { createUser(null, password), password });
        createRoleForUser(users.get(RoleName.ROLE_USER_ADMIN.name())[0], new UserAdminRole(), "");

        users.put(RoleName.ROLE_USER.name(), new String[] { createUser(null, password), password });
        createRoleForUser(users.get(RoleName.ROLE_USER.name())[0], new UserRole(), level2Id);

        users.put(RoleName.ROLE_LEVEL1_ADMIN.name(), new String[] { createUser(null, password), password });
        createRoleForUser(users.get(RoleName.ROLE_LEVEL1_ADMIN.name())[0], new Level1AdminRole(), level1Id);

    }
}
