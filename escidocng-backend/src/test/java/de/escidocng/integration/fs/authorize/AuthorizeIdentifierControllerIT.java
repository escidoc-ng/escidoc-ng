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

public class AuthorizeIdentifierControllerIT extends AbstractAuthorizeEscidocngIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeIdentifierControllerIT.class);

    @Test
    public void testCreateIdentifier() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl + entity.getId() + "/identifier")
                .body("type=DOI&value=123")
                .neededPermission(MissingPermission.WRITE_PENDING_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl + entity.getId() + "/identifier")
                .body("type=DOI&value=123")
                .neededPermission(MissingPermission.WRITE_SUBMITTED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testDeleteIdentifier() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId() +
                        "/identifier/DOI/testdoi")
                .neededPermission(MissingPermission.WRITE_PENDING_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId() +
                        "/identifier/DOI/testdoi")
                .neededPermission(MissingPermission.WRITE_SUBMITTED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

}
