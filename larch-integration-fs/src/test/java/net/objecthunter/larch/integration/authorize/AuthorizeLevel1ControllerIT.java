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


package net.objecthunter.larch.integration.authorize;

import static net.objecthunter.larch.test.util.Fixtures.createFixtureEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.objecthunter.larch.integration.helpers.AuthConfigurer;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.RoleRestriction;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.security.role.Level1AdminRole;
import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.test.util.Fixtures;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizeLevel1ControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeLevel1ControllerIT.class);

    @Test
    public void testPatchEntity() throws Exception {
        // create submitted entity
        Entity entity = createEntity(EntityState.SUBMITTED, FixedContentModel.LEVEL1.getName(), null, false);
        String patchData = "{\"label\":\"otherLabel\"}";
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PATCH, entityUrl + entity.getId())
                .body(patchData)
                .roleRestriction(RoleRestriction.ADMIN)
                .build());

        // level1 admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.PATCH, entityUrl + Fixtures.LEVEL1_ID, patchData,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PATCH, entityUrl + level1Id1, patchData,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrieveEntity() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, FixedContentModel.LEVEL1.getName(), null, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId())
                .roleRestriction(RoleRestriction.ADMIN)
                .build());

        // level1 admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + Fixtures.LEVEL1_ID, null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + level1Id1, null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrieveVersion() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, FixedContentModel.LEVEL1.getName(), null, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/version/1")
                .roleRestriction(RoleRestriction.ADMIN)
                .build());

        // level1 admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + Fixtures.LEVEL1_ID + "/version/1", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + level1Id1 + "/version/1", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrieveVersions() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, FixedContentModel.LEVEL1.getName(), null, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/versions")
                .roleRestriction(RoleRestriction.ADMIN)
                .build());

        // level1 admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + Fixtures.LEVEL1_ID + "/versions", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + level1Id1 + "/versions", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testCreateEntity() throws Exception {
        Entity e = createFixtureEntity(false);
        e.setParentId(null);
        e.setContentModelId(FixedContentModel.LEVEL1.getName());
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl)
                .body(mapper.writeValueAsString(e))
                .roleRestriction(RoleRestriction.ADMIN)
                .build());

        // level1 admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.POST, entityUrl, mapper.writeValueAsString(e),
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        resp =
                this.executeAsUser(HttpMethod.POST, entityUrl, mapper.writeValueAsString(e),
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testUpdateEntity() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.SUBMITTED, FixedContentModel.LEVEL1.getName(), null, false);
        entity.setLabel("otherLabel");
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId())
                .body(mapper.writeValueAsString(entity))
                .roleRestriction(RoleRestriction.ADMIN)
                .build());

        // level1 admin
        HttpResponse resp = this.executeAsAdmin(Request.Get(entityUrl + Fixtures.LEVEL1_ID));
        Entity level1 = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        level1.setLabel("changed");
        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + Fixtures.LEVEL1_ID, mapper.writeValueAsString(level1),
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + level1Id1, mapper.writeValueAsString(level1),
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testDeleteEntity() throws Exception {
        // create submitted entity
        Entity entity = createEntity(EntityState.SUBMITTED, FixedContentModel.LEVEL1.getName(), null, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId())
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        
        // level1 admin
        entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, false);
        String username = createUser(null, userPassword);
        createRoleForUser(username, new Level1AdminRole(), entity.getId());
        HttpResponse resp =
                this.executeAsUser(HttpMethod.DELETE, entityUrl + entity.getId(), null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                        .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        resp =
                this.executeAsUser(HttpMethod.DELETE, entityUrl + entity.getId(), null,
                        username, userPassword, false);
        response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);
        
    }

    @Test
    public void testPublishEntity() throws Exception {
        // create submitted entity
        Entity entity = createEntity(EntityState.SUBMITTED, FixedContentModel.LEVEL1.getName(), null, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/publish")
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());

        // level1 admin
        String myLevel1Id = createLevel1();
        String username = createUser(null, userPassword);
        createRoleForUser(username, new Level1AdminRole(), myLevel1Id);
        HttpResponse resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + myLevel1Id + "/publish", null,
                        username, userPassword, false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + level1Id1 + "/publish", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testSubmitEntity() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/submit")
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());

        // level1 admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + Fixtures.LEVEL1_ID + "/submit", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + level1Id1 + "/submit", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testWithdrawEntity() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, FixedContentModel.LEVEL1.getName(), null, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/withdraw")
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());

        // level1 admin
        String myLevel1Id = createLevel1();
        String username = createUser(null, userPassword);
        createRoleForUser(username, new Level1AdminRole(), myLevel1Id);
        HttpResponse resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + myLevel1Id + "/withdraw", null,
                        username, userPassword, false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + level1Id1 + "/withdraw", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

}
