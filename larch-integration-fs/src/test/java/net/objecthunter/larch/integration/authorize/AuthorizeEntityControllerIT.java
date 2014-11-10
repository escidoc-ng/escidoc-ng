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
import net.objecthunter.larch.integration.helpers.AuthConfigurer;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.MissingPermission;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.RoleRestriction;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizeEntityControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeEntityControllerIT.class);

    @Test
    public void testPatchEntity() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id);
        String patchData = "{\"label\":\"otherLabel\"}";
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PATCH, entityUrl + entity.getId())
                .body(patchData)
                .neededPermission(MissingPermission.WRITE_PENDING_METADATA)
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PATCH, entityUrl + entity.getId())
                .body(patchData)
                .neededPermission(MissingPermission.WRITE_SUBMITTED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testRetrieveEntity() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId())
                .neededPermission(MissingPermission.READ_PENDING_METADATA)
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId())
                .neededPermission(MissingPermission.READ_SUBMITTED_METADATA)
                .build());
        // create published entity
        entity = createEntity(EntityState.PUBLISHED, FixedContentModel.DATA.getName(), level2Id);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId())
                .neededPermission(MissingPermission.READ_PUBLISHED_METADATA)
                .build());
    }

    @Test
    public void testRetrieveVersion() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/version/1")
                .neededPermission(MissingPermission.READ_PENDING_METADATA)
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/version/1")
                .neededPermission(MissingPermission.READ_SUBMITTED_METADATA)
                .build());
        // create published entity
        entity = createEntity(EntityState.PUBLISHED, FixedContentModel.DATA.getName(), level2Id);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/version/1")
                .neededPermission(MissingPermission.READ_PUBLISHED_METADATA)
                .build());
    }

    @Test
    public void testRetrieveVersions() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/versions")
                .roleRestriction(RoleRestriction.ADMIN)
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/versions")
                .roleRestriction(RoleRestriction.ADMIN)
                .build());
        // create published entity
        entity = createEntity(EntityState.PUBLISHED, FixedContentModel.DATA.getName(), level2Id);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() + "/versions")
                .roleRestriction(RoleRestriction.ADMIN)
                .build());
    }

    @Test
    public void testCreateEntity() throws Exception {
        Entity e = createFixtureEntity();
        e.setParentId(level2Id);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl)
                .body(mapper.writeValueAsString(e))
                .neededPermission(MissingPermission.WRITE_PENDING_METADATA)
                .build());
    }

    @Test
    public void testUpdateEntity() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id);
        entity.setLabel("otherLabel");
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId())
                .body(mapper.writeValueAsString(entity))
                .neededPermission(MissingPermission.WRITE_PENDING_METADATA)
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id);
        entity.setLabel("otherLabel");
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId())
                .body(mapper.writeValueAsString(entity))
                .neededPermission(MissingPermission.WRITE_SUBMITTED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testDeleteEntity() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId())
                .neededPermission(MissingPermission.WRITE_PENDING_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId())
                .neededPermission(MissingPermission.WRITE_SUBMITTED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testPublishEntity() throws Exception {
        // create submitted entity
        Entity entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/publish")
                .neededPermission(MissingPermission.WRITE_SUBMITTED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testSubmitEntity() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/submit")
                .neededPermission(MissingPermission.WRITE_PENDING_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testWithdrawEntity() throws Exception {
        // create published entity
        Entity entity = createEntity(EntityState.PUBLISHED, FixedContentModel.DATA.getName(), level2Id);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, entityUrl + entity.getId() + "/withdraw")
                .neededPermission(MissingPermission.WRITE_PUBLISHED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

}
