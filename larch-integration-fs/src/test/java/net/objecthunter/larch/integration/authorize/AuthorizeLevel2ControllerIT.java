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
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import net.objecthunter.larch.integration.helpers.AuthConfigurer;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.MissingPermission;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.RoleRestriction;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.test.util.Fixtures;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizeLevel2ControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeLevel2ControllerIT.class);

    @Test
    public void testPatchLevel2() throws Exception {
        // retrieve level2
        String level2 = "{\"label\": \"patched\"}";
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PATCH, entityUrl + level2Id)
                .body(level2)
                .neededPermission(MissingPermission.WRITE_PERMISSION)
                .build());

        // level1 admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.PATCH, entityUrl + level2Id, level2,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PATCH, entityUrl + level2Id, level2,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + level1Id1)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrieveLevel2() throws Exception {
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + level2Id)
                .neededPermission(MissingPermission.READ_PERMISSION)
                .build());

        // level1 admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + level2Id, null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + level2Id, null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + level1Id1)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrieveVersion() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, FixedContentModel.LEVEL2.getName(), level1Id1);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/version/2")
                .roleRestriction(RoleRestriction.ADMIN)
                .build());

        // level1 admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + entity.getId() + "/version/1", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + level1Id1)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + entity.getId() + "/version/1", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrieveVersions() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, FixedContentModel.LEVEL1.getName(), null);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/versions")
                .roleRestriction(RoleRestriction.ADMIN)
                .build());

        // level1 admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + level2Id + "/versions", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + level2Id + "/versions", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + level1Id1)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testCreateLevel2() throws Exception {
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl)
                .roleRestriction(RoleRestriction.ADMIN)
                .body(mapper.writeValueAsString(getLevel2(Fixtures.LEVEL1_ID)))
                .build());

        // level1 admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.POST, entityUrl, mapper.writeValueAsString(getLevel2(Fixtures.LEVEL1_ID)),
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.POST, entityUrl, mapper.writeValueAsString(getLevel2(level1Id1)),
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testUpdateLevel2() throws Exception {
        // retrieve level2
        Entity level2 = retrieveLevel2(level2Id);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + level2Id)
                .body(mapper.writeValueAsString(level2))
                .neededPermission(MissingPermission.WRITE_PERMISSION)
                .build());

        // level1 admin
        HttpResponse resp = this.executeAsAdmin(Request.Get(entityUrl + level2Id));
        Entity level1 = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        level1.setLabel("changed");
        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + level2Id, mapper.writeValueAsString(level1),
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + level2Id, mapper.writeValueAsString(level1),
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + level1Id1)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testDeleteEntity() throws Exception {
        // create submitted entity
        Entity entity = createEntity(EntityState.SUBMITTED, FixedContentModel.LEVEL1.getName(), null);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId())
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        
        // level1 admin
        entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL2.getName(), level1Id1);
        HttpResponse resp =
                this.executeAsUser(HttpMethod.DELETE, entityUrl + entity.getId(), null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                        .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        resp =
                this.executeAsUser(HttpMethod.DELETE, entityUrl + entity.getId(), null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], level1AdminRoleUsernames
                        .get("ROLE_LEVEL1_ADMIN" + level1Id1)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);
        
    }

    @Test
    public void testPublishEntity() throws Exception {
        // create submitted entity
        Entity entity = createEntity(EntityState.SUBMITTED, FixedContentModel.LEVEL2.getName(), level1Id1);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/publish")
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());

        // level1 admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/publish", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + level1Id1)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/publish", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testSubmitEntity() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL2.getName(), level1Id1);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/submit")
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());

        // level1 admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/submit", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + level1Id1)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/submit", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testWithdrawEntity() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, FixedContentModel.LEVEL2.getName(), level1Id1);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/withdraw")
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());

        // level1 admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/withdraw", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + level1Id1)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/withdraw", null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    private Entity getLevel2(String level1Id) {
        final Entity level2 = new Entity();
        level2.setParentId(level1Id);
        level2.setContentModelId(FixedContentModel.LEVEL2.getName());
        level2.setLabel("bar");
        return level2;
    }

    private Entity retrieveLevel2(String level2Id) throws IOException {
        HttpResponse resp = this.executeAsAdmin(
                Request.Get(entityUrl + level2Id));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        return mapper.readValue(resp.getEntity().getContent(), Entity.class);
    }

}
