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

import net.objecthunter.larch.integration.helpers.AuthConfigurer;
import net.objecthunter.larch.model.Entity;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizePublishControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizePublishControllerIT.class);

    @Test
    public void testRetrievePublishedByPublishId() throws Exception {
        // create published entity
        Entity entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/published/" + entity.getPublishId())
                .build());
    }

    @Test
    public void testRetrievePublishedByPublishIdHtml() throws Exception {
        // create published entity
        Entity entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/published/" + entity.getPublishId())
                .build());
    }

    @Test
    public void testRetrievePublishedByEntityId() throws Exception {
        // create published entity
        Entity entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/published/" + entity.getId() + "/list")
                .build());
    }

}
