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

import java.io.File;

import net.objecthunter.larch.integration.helpers.AuthConfigurer;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.MissingPermission;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.RoleRestriction;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.MetadataType;
import net.objecthunter.larch.test.util.Fixtures;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
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
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/metadata")
                .body(mapper.writeValueAsString(getMetadata()))
                .neededPermission(MissingPermission.WRITE_PENDING_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/metadata")
                .body(mapper.writeValueAsString(getMetadata()))
                .neededPermission(MissingPermission.WRITE_SUBMITTED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/metadata")
                .body(mapper.writeValueAsString(getMetadata()))
                .neededPermission(MissingPermission.WRITE_PUBLISHED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testCreateMetadataHtml() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/metadata")
                .body(MultipartEntityBuilder.create()
                        .addTextBody("name", "test")
                        .addTextBody("type", "DC")
                        .addPart(
                                "metadata",
                                new FileBody(new File(Fixtures.class.getClassLoader().getResource(
                                        "fixtures/dc.xml").getFile())))
                        .build())
                .neededPermission(MissingPermission.WRITE_PENDING_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/metadata")
                .body(MultipartEntityBuilder.create()
                        .addTextBody("name", "test")
                        .addTextBody("type", "DC")
                        .addPart(
                                "metadata",
                                new FileBody(new File(Fixtures.class.getClassLoader().getResource(
                                        "fixtures/dc.xml").getFile())))
                        .build())
                .neededPermission(MissingPermission.WRITE_SUBMITTED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/metadata")
                .body(MultipartEntityBuilder.create()
                        .addTextBody("name", "test")
                        .addTextBody("type", "DC")
                        .addPart(
                                "metadata",
                                new FileBody(new File(Fixtures.class.getClassLoader().getResource(
                                        "fixtures/dc.xml").getFile())))
                        .build())
                .neededPermission(MissingPermission.WRITE_PUBLISHED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());
    }

    @Test
    public void testCreateBinaryMetadata() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/image-1/metadata")
                .body(mapper.writeValueAsString(getMetadata()))
                .neededPermission(MissingPermission.WRITE_PENDING_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/image-1/metadata")
                .body(mapper.writeValueAsString(getMetadata()))
                .neededPermission(MissingPermission.WRITE_SUBMITTED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/image-1/metadata")
                .body(mapper.writeValueAsString(getMetadata()))
                .neededPermission(MissingPermission.WRITE_PUBLISHED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testCreateBinaryMetadataHtml() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/image-1/metadata")
                .body(MultipartEntityBuilder.create()
                        .addTextBody("name", "test")
                        .addTextBody("type", "DC")
                        .addPart(
                                "metadata",
                                new FileBody(new File(Fixtures.class.getClassLoader().getResource(
                                        "fixtures/dc.xml").getFile())))
                        .build())
                .neededPermission(MissingPermission.WRITE_PENDING_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/image-1/metadata")
                .body(MultipartEntityBuilder.create()
                        .addTextBody("name", "test")
                        .addTextBody("type", "DC")
                        .addPart(
                                "metadata",
                                new FileBody(new File(Fixtures.class.getClassLoader().getResource(
                                        "fixtures/dc.xml").getFile())))
                        .build())
                .neededPermission(MissingPermission.WRITE_SUBMITTED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/image-1/metadata")
                .body(MultipartEntityBuilder.create()
                        .addTextBody("name", "test")
                        .addTextBody("type", "DC")
                        .addPart(
                                "metadata",
                                new FileBody(new File(Fixtures.class.getClassLoader().getResource(
                                        "fixtures/dc.xml").getFile())))
                        .build())
                .neededPermission(MissingPermission.WRITE_PUBLISHED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());
    }

    @Test
    public void testRetrieveMetadataXml() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/metadata/" + entity.getMetadata().keySet().iterator().next() + "/content")
                .neededPermission(MissingPermission.READ_PENDING_METADATA)
                .build());
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/metadata/" + entity.getMetadata().keySet().iterator().next() + "/content")
                .neededPermission(MissingPermission.READ_SUBMITTED_METADATA)
                .build());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/metadata/" + entity.getMetadata().keySet().iterator().next() + "/content")
                .build());
    }

    @Test
    public void testRetrieveBinaryMetadataXml() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        String binaryName = entity.getBinaries().keySet().iterator().next();
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinaries().get(binaryName).getMetadata().keySet().iterator().next() + "/content")
                .neededPermission(MissingPermission.READ_PENDING_BINARY)
                .build());
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinaries().get(binaryName).getMetadata().keySet().iterator().next() + "/content")
                .neededPermission(MissingPermission.READ_SUBMITTED_BINARY)
                .build());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinaries().get(binaryName).getMetadata().keySet().iterator().next() + "/content")
                .build());
    }

    @Test
    public void testValidateMetadata() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/metadata/" + entity.getMetadata().keySet().iterator().next() + "/validate")
                .neededPermission(MissingPermission.READ_PENDING_METADATA)
                .build());
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/metadata/" + entity.getMetadata().keySet().iterator().next() + "/validate")
                .neededPermission(MissingPermission.READ_SUBMITTED_METADATA)
                .build());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/metadata/" + entity.getMetadata().keySet().iterator().next() + "/validate")
                .build());
    }

    @Test
    public void testValidateBinaryMetadata() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        String binaryName = entity.getBinaries().keySet().iterator().next();
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinaries().get(binaryName).getMetadata().keySet().iterator().next() + "/validate")
                .neededPermission(MissingPermission.READ_PENDING_BINARY)
                .build());
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinaries().get(binaryName).getMetadata().keySet().iterator().next() + "/validate")
                .neededPermission(MissingPermission.READ_SUBMITTED_BINARY)
                .build());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinaries().get(binaryName).getMetadata().keySet().iterator().next() + "/validate")
                .build());
    }

    @Test
    public void testRetrieveTypes() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "metadatatype")
                .roleRestriction(RoleRestriction.LOGGED_IN)
                .build());
    }

    @Test
    public void testRetrieveTypesHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "metadatatype")
                .roleRestriction(RoleRestriction.LOGGED_IN)
                .html(true)
                .build());
    }

    @Test
    public void testAddType() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, hostUrl + "metadatatype")
                .body(mapper.writeValueAsString(getMetadataType()))
                .roleRestriction(RoleRestriction.ADMIN)
                .build());
    }

    @Test
    public void testAddTypeHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, hostUrl + "metadatatype")
                .body(MultipartEntityBuilder.create()
                        .addTextBody("name", RandomStringUtils.randomAlphabetic(16))
                        .addTextBody("schemaUrl", "http://www.somewhat.org")
                        .build())
                .roleRestriction(RoleRestriction.ADMIN)
                .build());
    }

    @Test
    public void testRetrieveMetadata() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/metadata/" + entity.getMetadata().keySet().iterator().next())
                .neededPermission(MissingPermission.READ_PENDING_METADATA)
                .build());
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/metadata/" + entity.getMetadata().keySet().iterator().next())
                .neededPermission(MissingPermission.READ_SUBMITTED_METADATA)
                .build());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/metadata/" + entity.getMetadata().keySet().iterator().next())
                .build());
    }

    @Test
    public void testRetrieveMetadataHtml() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/metadata/" + entity.getMetadata().keySet().iterator().next())
                .neededPermission(MissingPermission.READ_PENDING_METADATA)
                .html(true)
                .build());
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/metadata/" + entity.getMetadata().keySet().iterator().next())
                .neededPermission(MissingPermission.READ_SUBMITTED_METADATA)
                .html(true)
                .build());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/metadata/" + entity.getMetadata().keySet().iterator().next())
                .html(true)
                .build());
    }

    @Test
    public void testRetrieveBinaryMetadata() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        String binaryName = entity.getBinaries().keySet().iterator().next();
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinaries().get(binaryName).getMetadata().keySet().iterator().next())
                .neededPermission(MissingPermission.READ_PENDING_BINARY)
                .build());
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinaries().get(binaryName).getMetadata().keySet().iterator().next())
                .neededPermission(MissingPermission.READ_SUBMITTED_BINARY)
                .build());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinaries().get(binaryName).getMetadata().keySet().iterator().next())
                .build());
    }

    @Test
    public void testRetrieveBinaryMetadataHtml() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        String binaryName = entity.getBinaries().keySet().iterator().next();
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinaries().get(binaryName).getMetadata().keySet().iterator().next())
                .neededPermission(MissingPermission.READ_PENDING_BINARY)
                .html(true)
                .build());
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinaries().get(binaryName).getMetadata().keySet().iterator().next())
                .neededPermission(MissingPermission.READ_SUBMITTED_BINARY)
                .html(true)
                .build());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinaries().get(binaryName).getMetadata().keySet().iterator().next())
                .html(true)
                .build());
    }

    @Test
    public void testDeleteMetadata() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/metadata/" + entity.getMetadata().keySet().iterator().next())
                .neededPermission(MissingPermission.WRITE_PENDING_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/metadata/" + entity.getMetadata().keySet().iterator().next())
                .neededPermission(MissingPermission.WRITE_SUBMITTED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/metadata/" + entity.getMetadata().keySet().iterator().next())
                .neededPermission(MissingPermission.WRITE_PUBLISHED_METADATA)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testDeleteBinaryMetadata() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        String binaryName = entity.getBinaries().keySet().iterator().next();
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinaries().get(binaryName).getMetadata().keySet().iterator().next())
                .neededPermission(MissingPermission.WRITE_PENDING_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinaries().get(binaryName).getMetadata().keySet().iterator().next())
                .neededPermission(MissingPermission.WRITE_SUBMITTED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/" + binaryName + "/metadata/" +
                        entity.getBinaries().get(binaryName).getMetadata().keySet().iterator().next())
                .neededPermission(MissingPermission.WRITE_PUBLISHED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    private Metadata getMetadata() {
        Metadata md = Fixtures.createRandomDCMetadata();
        return md;
    }

    private MetadataType getMetadataType() {
        MetadataType md = new MetadataType();
        md.setName(RandomStringUtils.randomAlphabetic(16));
        md.setSchemaUrl("http://www.somewhat.org");
        return md;
    }

}
