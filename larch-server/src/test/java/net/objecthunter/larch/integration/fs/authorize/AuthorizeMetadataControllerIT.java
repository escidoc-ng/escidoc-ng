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


package net.objecthunter.larch.integration.fs.authorize;

import net.objecthunter.larch.integration.fs.helpers.AuthConfigurer;
import net.objecthunter.larch.integration.fs.helpers.AuthConfigurer.MissingPermission;
import net.objecthunter.larch.integration.fs.helpers.AuthConfigurer.RoleRestriction;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.MetadataType;
import net.objecthunter.larch.test.util.Fixtures;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizeMetadataControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeMetadataControllerIT.class);

    @Test
    public void testCreateMetadata() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl + entity.getId() + "/metadata")
                .body(mapper.writeValueAsString(getMetadata()))
                .neededPermission(MissingPermission.WRITE_PENDING_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl + entity.getId() + "/metadata")
                .body(mapper.writeValueAsString(getMetadata()))
                .neededPermission(MissingPermission.WRITE_SUBMITTED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testCreateBinaryMetadata() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl + entity.getId() +
                        "/binary/image-1/metadata")
                .body(mapper.writeValueAsString(getMetadata()))
                .neededPermission(MissingPermission.WRITE_PENDING_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, entityUrl + entity.getId() +
                        "/binary/image-1/metadata")
                .body(mapper.writeValueAsString(getMetadata()))
                .neededPermission(MissingPermission.WRITE_SUBMITTED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testRetrieveMetadataXml() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/metadata/" + entity.getMetadata().iterator().next().getName() + "/content")
                .neededPermission(MissingPermission.READ_PENDING_METADATA)
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/metadata/" + entity.getMetadata().iterator().next().getName() + "/content")
                .neededPermission(MissingPermission.READ_SUBMITTED_METADATA)
                .build());
        // create published entity
        entity = createEntity(EntityState.PUBLISHED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/metadata/" + entity.getMetadata().iterator().next().getName() + "/content")
                .neededPermission(MissingPermission.READ_PUBLISHED_METADATA)
                .build());
    }

    @Test
    public void testRetrieveBinaryMetadataXml() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id, false);
        String binaryName = entity.getBinaries().iterator().next().getName();
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinary(binaryName).getMetadata().iterator().next().getName() + "/content")
                .neededPermission(MissingPermission.READ_PENDING_BINARY)
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinary(binaryName).getMetadata().iterator().next().getName() + "/content")
                .neededPermission(MissingPermission.READ_SUBMITTED_BINARY)
                .build());
        // create published entity
        entity = createEntity(EntityState.PUBLISHED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinary(binaryName).getMetadata().iterator().next().getName() + "/content")
                .neededPermission(MissingPermission.READ_PUBLISHED_BINARY)
                .build());
    }

    @Test
    public void testValidateMetadata() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/metadata/" + entity.getMetadata().iterator().next().getName() + "/validate")
                .neededPermission(MissingPermission.READ_PENDING_METADATA)
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/metadata/" + entity.getMetadata().iterator().next().getName() + "/validate")
                .neededPermission(MissingPermission.READ_SUBMITTED_METADATA)
                .build());
        // create published entity
        entity = createEntity(EntityState.PUBLISHED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/metadata/" + entity.getMetadata().iterator().next().getName() + "/validate")
                .neededPermission(MissingPermission.READ_PUBLISHED_METADATA)
                .build());
    }

    @Test
    public void testValidateBinaryMetadata() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id, false);
        String binaryName = entity.getBinaries().iterator().next().getName();
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinary(binaryName).getMetadata().iterator().next().getName() + "/validate")
                .neededPermission(MissingPermission.READ_PENDING_BINARY)
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinary(binaryName).getMetadata().iterator().next().getName() + "/validate")
                .neededPermission(MissingPermission.READ_SUBMITTED_BINARY)
                .build());
        // create published entity
        entity = createEntity(EntityState.PUBLISHED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinary(binaryName).getMetadata().iterator().next().getName() + "/validate")
                .neededPermission(MissingPermission.READ_PUBLISHED_BINARY)
                .build());
    }

    @Test
    public void testRetrieveTypes() throws Exception {
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "metadatatype")
                .roleRestriction(RoleRestriction.LOGGED_IN)
                .build());
    }

    @Test
    public void testAddType() throws Exception {
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, hostUrl + "metadatatype")
                .body(mapper.writeValueAsString(getMetadataType()))
                .roleRestriction(RoleRestriction.ADMIN)
                .build());
    }

    @Test
    public void testRetrieveMetadata() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/metadata/" + entity.getMetadata().iterator().next().getName())
                .neededPermission(MissingPermission.READ_PENDING_METADATA)
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/metadata/" + entity.getMetadata().iterator().next().getName())
                .neededPermission(MissingPermission.READ_SUBMITTED_METADATA)
                .build());
        // create published entity
        entity = createEntity(EntityState.PUBLISHED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/metadata/" + entity.getMetadata().iterator().next().getName())
                .neededPermission(MissingPermission.READ_PUBLISHED_METADATA)
                .build());
    }

    @Test
    public void testRetrieveBinaryMetadata() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id, false);
        String binaryName = entity.getBinaries().iterator().next().getName();
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinary(binaryName).getMetadata().iterator().next().getName())
                .neededPermission(MissingPermission.READ_PENDING_BINARY)
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinary(binaryName).getMetadata().iterator().next().getName())
                .neededPermission(MissingPermission.READ_SUBMITTED_BINARY)
                .build());
        // create published entity
        entity = createEntity(EntityState.PUBLISHED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, entityUrl + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinary(binaryName).getMetadata().iterator().next().getName())
                .neededPermission(MissingPermission.READ_PUBLISHED_BINARY)
                .build());
    }

    @Test
    public void testDeleteMetadata() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId() +
                        "/metadata/" + entity.getMetadata().iterator().next().getName())
                .neededPermission(MissingPermission.WRITE_PENDING_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId() +
                        "/metadata/" + entity.getMetadata().iterator().next().getName())
                .neededPermission(MissingPermission.WRITE_SUBMITTED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testDeleteBinaryMetadata() throws Exception {
        // create pending entity
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id, false);
        String binaryName = entity.getBinaries().iterator().next().getName();
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinary(binaryName).getMetadata().iterator().next().getName())
                .neededPermission(MissingPermission.WRITE_PENDING_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create submitted entity
        entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id, false);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, entityUrl + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinary(binaryName).getMetadata().iterator().next().getName())
                .neededPermission(MissingPermission.WRITE_SUBMITTED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    private Metadata getMetadata() throws Exception {
        Metadata md = Fixtures.createRandomDCMetadata(false);
        return md;
    }

    private MetadataType getMetadataType() {
        MetadataType md = new MetadataType();
        md.setName(RandomStringUtils.randomAlphabetic(16));
        md.setSchemaUrl("http://www.somewhat.org");
        return md;
    }

}
