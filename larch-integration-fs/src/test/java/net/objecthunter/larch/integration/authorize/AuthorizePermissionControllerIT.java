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
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.Entity.EntityType;
import net.objecthunter.larch.model.security.role.AreaAdminRole;
import net.objecthunter.larch.test.util.Fixtures;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizePermissionControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizePermissionControllerIT.class);

    @Test
    public void testPatchPermission() throws Exception {
        // retrieve workspace
        String permission = "{\"label\": \"patched\"}";
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PATCH, entityUrl + permissionId)
                .body(permission)
                .neededPermission(MissingPermission.WRITE_PERMISSION)
                .build());

        // area admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.PATCH, entityUrl + permissionId, permission,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PATCH, entityUrl + permissionId, permission,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrievePermission() throws Exception {
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + permissionId)
                .neededPermission(MissingPermission.READ_PERMISSION)
                .build());

        // area admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + permissionId, null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + permissionId, null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrievePermissionHtml() throws Exception {
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + permissionId)
                .neededPermission(MissingPermission.READ_PERMISSION)
                .html(true)
                .build());

        // area admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + permissionId, null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], true);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + permissionId, null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], true);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrieveVersion() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, EntityType.PERMISSION, areaId1);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/version/2")
                .roleRestriction(RoleRestriction.ADMIN)
                .build());

        // area admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + entity.getId() + "/version/1", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + entity.getId() + "/version/1", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrieveVersionHtml() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, EntityType.AREA, null);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/version/2")
                .roleRestriction(RoleRestriction.ADMIN)
                .html(true)
                .build());

        // area admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + permissionId + "/version/1", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], true);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + permissionId + "/version/1", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], true);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrieveVersions() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, EntityType.AREA, null);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/versions")
                .roleRestriction(RoleRestriction.ADMIN)
                .build());

        // area admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + permissionId + "/versions", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + permissionId + "/versions", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrieveVersionsHtml() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, EntityType.AREA, null);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/versions")
                .roleRestriction(RoleRestriction.ADMIN)
                .html(true)
                .build());

        // area admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + permissionId + "/versions", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], true);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.GET, entityUrl + permissionId + "/versions", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], true);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testCreatePermission() throws Exception {
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl)
                .roleRestriction(RoleRestriction.ADMIN)
                .body(mapper.writeValueAsString(getPermission(Fixtures.AREA_ID)))
                .build());

        // area admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.POST, entityUrl, mapper.writeValueAsString(getPermission(Fixtures.AREA_ID)),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.POST, entityUrl, mapper.writeValueAsString(getPermission(areaId1)),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testUpdatePermission() throws Exception {
        // retrieve workspace
        Entity permission = retrievePermission(permissionId);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + permissionId)
                .body(mapper.writeValueAsString(permission))
                .neededPermission(MissingPermission.WRITE_PERMISSION)
                .build());

        // area admin
        HttpResponse resp = this.executeAsAdmin(Request.Get(entityUrl + permissionId));
        Entity area = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        area.setLabel("changed");
        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + permissionId, mapper.writeValueAsString(area),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + permissionId, mapper.writeValueAsString(area),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testDeleteEntity() throws Exception {
        // create submitted entity
        Entity entity = createEntity(EntityState.SUBMITTED, EntityType.AREA, null);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId())
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        
        // area admin
        entity = createEntity(EntityState.PENDING, EntityType.PERMISSION, areaId1);
        HttpResponse resp =
                this.executeAsUser(HttpMethod.DELETE, entityUrl + entity.getId(), null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                        .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        resp =
                this.executeAsUser(HttpMethod.DELETE, entityUrl + entity.getId(), null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                        .get("ROLE_AREA_ADMIN" + areaId1)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);
        
    }

    @Test
    public void testPublishEntity() throws Exception {
        // create submitted entity
        Entity entity = createEntity(EntityState.SUBMITTED, EntityType.PERMISSION, areaId1);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/publish")
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());

        // area admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/publish", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/publish", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testPublishEntityHtml() throws Exception {
        // create submitted entity
        Entity entity = createEntity(EntityState.SUBMITTED, EntityType.PERMISSION, areaId1);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/publish")
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());

        // area admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/publish", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], true);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/publish", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], true);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testSubmitEntity() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, EntityType.PERMISSION, areaId1);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/submit")
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());

        // area admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/submit", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/submit", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testSubmitEntityHtml() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, EntityType.PERMISSION, areaId1);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/submit")
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());

        // area admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/submit", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], true);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/submit", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], true);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testWithdrawEntity() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, EntityType.PERMISSION, areaId1);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/withdraw")
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());

        // area admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/withdraw", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/withdraw", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testWithdrawEntityHtml() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, EntityType.PERMISSION, areaId1);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/withdraw")
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());

        // area admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/withdraw", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], true);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.PUT, entityUrl + entity.getId() + "/withdraw", null,
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], true);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());
    }

    private Entity getPermission(String areaId) {
        final Entity permission = new Entity();
        permission.setParentId(areaId);
        permission.setType(EntityType.PERMISSION);
        permission.setLabel("bar");
        return permission;
    }

    private Entity retrievePermission(String permissionId) throws IOException {
        HttpResponse resp = this.executeAsAdmin(
                Request.Get(entityUrl + permissionId));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        return mapper.readValue(resp.getEntity().getContent(), Entity.class);
    }

}
