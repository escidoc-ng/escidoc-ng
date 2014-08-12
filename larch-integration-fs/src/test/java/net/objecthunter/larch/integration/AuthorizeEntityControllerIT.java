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
import net.objecthunter.larch.model.Entity;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizeEntityControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeEntityControllerIT.class);

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

    @Test
    public void testUpdateEntity() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        entity.setLabel("otherLabel");
        testAuth(HttpMethod.PUT, workspaceUrl + workspaceId + "/entity/" + entity.getId(), mapper
                .writeValueAsString(entity),
                MissingPermission.WRITE_PENDING_METADATA);
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        entity.setLabel("otherLabel");
        testAuth(HttpMethod.PUT, workspaceUrl + workspaceId + "/entity/" + entity.getId(), mapper
                .writeValueAsString(entity),
                MissingPermission.WRITE_SUBMITTED_METADATA, Entity.STATE_SUBMITTED, entity.getId());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        entity.setLabel("otherLabel");
        testAuth(HttpMethod.PUT, workspaceUrl + workspaceId + "/entity/" + entity.getId(), mapper
                .writeValueAsString(entity),
                MissingPermission.WRITE_PUBLISHED_METADATA, Entity.STATE_PUBLISHED, entity.getId());
    }

    @Test
    public void testDeleteEntity() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(HttpMethod.DELETE, workspaceUrl + workspaceId + "/entity/" + entity.getId(), null,
                MissingPermission.WRITE_PENDING_METADATA, Entity.STATE_PENDING, entity.getId());
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(HttpMethod.DELETE, workspaceUrl + workspaceId + "/entity/" + entity.getId(), mapper
                .writeValueAsString(entity),
                MissingPermission.WRITE_SUBMITTED_METADATA, Entity.STATE_SUBMITTED, entity.getId());
    }

    @Test
    public void testSubmitEntity() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(HttpMethod.PUT, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/submit", null,
                MissingPermission.WRITE_PENDING_METADATA, Entity.STATE_PENDING, entity.getId());
    }

    @Test
    public void testPublishEntity() throws Exception {
        // create submitted entity
        Entity entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(HttpMethod.PUT, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/publish", null,
                MissingPermission.WRITE_SUBMITTED_METADATA, Entity.STATE_SUBMITTED, entity.getId());
    }

}
