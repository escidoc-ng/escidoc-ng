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

import static net.objecthunter.larch.test.util.Fixtures.createFixtureEntity;
import net.objecthunter.larch.integration.helpers.AuthConfigurer;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.RoleRestriction;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.Entity.EntityType;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizeAreaControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeAreaControllerIT.class);

    @Test
    public void testPatchEntity() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, EntityType.AREA, null);
        String patchData = "{\"label\":\"otherLabel\"}";
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PATCH, entityUrl + entity.getId())
                .body(patchData)
                .roleRestriction(RoleRestriction.ADMIN)
                .build());
    }

    @Test
    public void testRetrieveEntity() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, EntityType.AREA, null);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId())
                .roleRestriction(RoleRestriction.ADMIN)
                .build());
    }

    @Test
    public void testRetrieveEntityHtml() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, EntityType.AREA, null);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId())
                .roleRestriction(RoleRestriction.ADMIN)
                .html(true)
                .build());
    }

    @Test
    public void testRetrieveVersion() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, EntityType.AREA, null);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/version/2")
                .roleRestriction(RoleRestriction.ADMIN)
                .build());
    }

    @Test
    public void testRetrieveVersionHtml() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, EntityType.AREA, null);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/version/2")
                .roleRestriction(RoleRestriction.ADMIN)
                .html(true)
                .build());
    }

    @Test
    public void testRetrieveVersions() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, EntityType.AREA, null);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/versions")
                .roleRestriction(RoleRestriction.ADMIN)
                .build());
    }

    @Test
    public void testRetrieveVersionsHtml() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, EntityType.AREA, null);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/versions")
                .roleRestriction(RoleRestriction.ADMIN)
                .html(true)
                .build());
    }

    @Test
    public void testCreateEntity() throws Exception {
        Entity e = createFixtureEntity();
        e.setParentId(null);
        e.setType(EntityType.AREA);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl)
                .body(mapper.writeValueAsString(e))
                .roleRestriction(RoleRestriction.ADMIN)
                .build());
    }

    @Test
    public void testUpdateEntity() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, EntityType.AREA, null);
        entity.setLabel("otherLabel");
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId())
                .body(mapper.writeValueAsString(entity))
                .roleRestriction(RoleRestriction.ADMIN)
                .build());
    }

    @Test
    public void testDeleteEntity() throws Exception {
        // create submitted entity
        Entity entity = createEntity(EntityState.SUBMITTED, EntityType.AREA, null);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId())
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testPublishEntity() throws Exception {
        // create submitted entity
        Entity entity = createEntity(EntityState.SUBMITTED, EntityType.AREA, null);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/publish")
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testPublishEntityHtml() throws Exception {
        // create submitted entity
        Entity entity = createEntity(EntityState.SUBMITTED, EntityType.AREA, null);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/publish")
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());
    }

    @Test
    public void testSubmitEntity() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, EntityType.AREA, null);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/submit")
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testSubmitEntityHtml() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, EntityType.AREA, null);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/submit")
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());
    }
}