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

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.test.util.Fixtures;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizeMetadataControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeMetadataControllerIT.class);

    @Test
    public void testCreateMetadata() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/metadata", mapper
                .writeValueAsString(getMetadata()), MissingPermission.WRITE_PENDING_METADATA, true, entity.getId());
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/metadata", mapper
                .writeValueAsString(getMetadata()), MissingPermission.WRITE_SUBMITTED_METADATA, true, entity.getId());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/metadata", mapper
                .writeValueAsString(getMetadata()), MissingPermission.WRITE_PUBLISHED_METADATA, true, entity.getId());
    }

    @Test
    public void testCreateBinaryMetadata() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                "/binary/image-1/metadata", mapper
                .writeValueAsString(getMetadata()), MissingPermission.WRITE_PENDING_BINARY, true, entity.getId());
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                "/binary/image-1/metadata", mapper
                .writeValueAsString(getMetadata()), MissingPermission.WRITE_SUBMITTED_BINARY, true, entity.getId());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                "/binary/image-1/metadata", mapper
                .writeValueAsString(getMetadata()), MissingPermission.WRITE_PUBLISHED_BINARY, true, entity.getId());
    }

    private Metadata getMetadata() {
        Metadata md = Fixtures.createRandomDCMetadata();
        return md;
    }

}
