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


package de.escidocng.integration.fs.authorize;

import de.escidocng.integration.fs.helpers.AuthConfigurer;
import de.escidocng.integration.fs.helpers.AuthConfigurer.MissingPermission;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import de.escidocng.model.Entity;
import de.escidocng.model.ContentModel.FixedContentModel;
import de.escidocng.model.Entity.EntityState;

public class AuthorizeArchiveControllerIT extends AbstractAuthorizeEscidocngIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeArchiveControllerIT.class);

    @Test
    public void testRetrieveArchive() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id, false);
        createArchive(entity.getId(), entity.getVersion());
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, archiveUrl + entity.getId() + "/" + entity.getVersion())
                .neededPermission(MissingPermission.READ_PENDING_METADATA)
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id, false);
        createArchive(entity.getId(), entity.getVersion());
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, archiveUrl + entity.getId() + "/" + entity.getVersion())
                .neededPermission(MissingPermission.READ_SUBMITTED_METADATA)
                .build());
        // create published entity
        entity = createEntity(EntityState.PUBLISHED, FixedContentModel.DATA.getName(), level2Id, false);
        createArchive(entity.getId(), entity.getVersion());
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, archiveUrl + entity.getId() + "/" + entity.getVersion())
                .neededPermission(MissingPermission.READ_PUBLISHED_METADATA)
                .build());
        // create withdrawn entity
        entity = createEntity(EntityState.WITHDRAWN, FixedContentModel.DATA.getName(), level2Id, false);
        createArchive(entity.getId(), entity.getVersion());
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, archiveUrl + entity.getId() + "/" + entity.getVersion())
                .neededPermission(MissingPermission.READ_WITHDRAWN_METADATA)
                .build());
    }

    @Test
    public void testCreateArchive() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, archiveUrl + entity.getId() + "/" + entity.getVersion())
                .neededPermission(MissingPermission.WRITE_PENDING_METADATA)
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, archiveUrl + entity.getId() + "/" + entity.getVersion())
                .neededPermission(MissingPermission.WRITE_SUBMITTED_METADATA)
                .build());
        // create published entity
        entity = createEntity(EntityState.PUBLISHED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, archiveUrl + entity.getId() + "/" + entity.getVersion())
                .neededPermission(MissingPermission.WRITE_PUBLISHED_METADATA)
                .build());
        // create withdrawn entity
        entity = createEntity(EntityState.WITHDRAWN, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, archiveUrl + entity.getId() + "/" + entity.getVersion())
                .neededPermission(MissingPermission.WRITE_WITHDRAWN_METADATA)
                .build());
    }

    @Test
    public void testRetrieveArchiveContent() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id, false);
        createArchive(entity.getId(), entity.getVersion());
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, archiveUrl + entity.getId() + "/" + entity.getVersion() + "/content")
                .neededPermission(MissingPermission.READ_PENDING_METADATA)
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id, false);
        createArchive(entity.getId(), entity.getVersion());
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, archiveUrl + entity.getId() + "/" + entity.getVersion() + "/content")
                .neededPermission(MissingPermission.READ_SUBMITTED_METADATA)
                .build());
        // create published entity
        entity = createEntity(EntityState.PUBLISHED, FixedContentModel.DATA.getName(), level2Id, false);
        createArchive(entity.getId(), entity.getVersion());
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, archiveUrl + entity.getId() + "/" + entity.getVersion() + "/content")
                .neededPermission(MissingPermission.READ_PUBLISHED_METADATA)
                .build());
        // create withdrawn entity
        entity = createEntity(EntityState.WITHDRAWN, FixedContentModel.DATA.getName(), level2Id, false);
        createArchive(entity.getId(), entity.getVersion());
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, archiveUrl + entity.getId() + "/" + entity.getVersion() + "/content")
                .neededPermission(MissingPermission.READ_WITHDRAWN_METADATA)
                .build());
    }

}
