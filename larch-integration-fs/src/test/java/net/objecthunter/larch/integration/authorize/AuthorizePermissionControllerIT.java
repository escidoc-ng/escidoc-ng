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

package net.objecthunter.larch.integration.authorize;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import net.objecthunter.larch.integration.helpers.AuthConfigurer;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.MissingPermission;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.RoleRestriction;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityType;
import net.objecthunter.larch.test.util.Fixtures;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizePermissionControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizePermissionControllerIT.class);

    @Test
    public void testCreatePermission() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl)
                .roleRestriction(RoleRestriction.ADMIN)
                .body(mapper.writeValueAsString(getPermission(Fixtures.AREA_ID)))
                .build());
    }

    @Test
    public void testRetrievePermission() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + permissionId)
                .neededPermission(MissingPermission.READ_PERMISSION)
                .build());
    }

    @Test
    public void testRetrievePermissionHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + permissionId)
                .neededPermission(MissingPermission.READ_PERMISSION)
                .html(true)
                .build());
    }

    @Test
    public void testUpdatePermission() throws Exception {
        // retrieve workspace
        Entity permission = retrievePermission(permissionId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + permissionId)
                .body(mapper.writeValueAsString(permission))
                .neededPermission(MissingPermission.WRITE_PERMISSION)
                .build());
    }

    @Test
    public void testPatchPermission() throws Exception {
        // retrieve workspace
        String permission = "{\"label\": \"patched\"}";
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PATCH, entityUrl + permissionId)
                .body(permission)
                .neededPermission(MissingPermission.WRITE_PERMISSION)
                .build());
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
