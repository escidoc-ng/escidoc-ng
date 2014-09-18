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

import net.objecthunter.larch.integration.helpers.AuthConfigurer;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.MissingPermission;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.Entity.EntityType;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizeIdentifierControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeIdentifierControllerIT.class);

    @Test
    public void testCreateIdentifier() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, EntityType.DATA, permissionId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl + entity.getId() + "/identifier")
                .body("type=DOI&value=123")
                .neededPermission(MissingPermission.WRITE_PENDING_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, EntityType.DATA, permissionId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl + entity.getId() + "/identifier")
                .body("type=DOI&value=123")
                .neededPermission(MissingPermission.WRITE_SUBMITTED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create published entity
        entity = createEntity(EntityState.PUBLISHED, EntityType.DATA, permissionId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl + entity.getId() + "/identifier")
                .body("type=DOI&value=123")
                .neededPermission(MissingPermission.WRITE_PUBLISHED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testCreateIdentifierHtml() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, EntityType.DATA, permissionId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl + entity.getId() + "/identifier")
                .body("type=DOI&value=123")
                .neededPermission(MissingPermission.WRITE_PENDING_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, EntityType.DATA, permissionId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl + entity.getId() + "/identifier")
                .body("type=DOI&value=123")
                .neededPermission(MissingPermission.WRITE_SUBMITTED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());
        // create published entity
        entity = createEntity(EntityState.PUBLISHED, EntityType.DATA, permissionId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl + entity.getId() + "/identifier")
                .body("type=DOI&value=123")
                .neededPermission(MissingPermission.WRITE_PUBLISHED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());
    }

    @Test
    public void testDeleteIdentifier() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, EntityType.DATA, permissionId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId() +
                        "/identifier/DOI/testdoi")
                .neededPermission(MissingPermission.WRITE_PENDING_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, EntityType.DATA, permissionId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId() +
                        "/identifier/DOI/testdoi")
                .neededPermission(MissingPermission.WRITE_SUBMITTED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create published entity
        entity = createEntity(EntityState.PUBLISHED, EntityType.DATA, permissionId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId() +
                        "/identifier/DOI/testdoi")
                .neededPermission(MissingPermission.WRITE_PUBLISHED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testDeleteIdentifierHtml() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, EntityType.DATA, permissionId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId() +
                        "/identifier/DOI/testdoi")
                .neededPermission(MissingPermission.WRITE_PENDING_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, EntityType.DATA, permissionId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId() +
                        "/identifier/DOI/testdoi")
                .neededPermission(MissingPermission.WRITE_SUBMITTED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());
        // create published entity
        entity = createEntity(EntityState.PUBLISHED, EntityType.DATA, permissionId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId() +
                        "/identifier/DOI/testdoi")
                .neededPermission(MissingPermission.WRITE_PUBLISHED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());
    }

}