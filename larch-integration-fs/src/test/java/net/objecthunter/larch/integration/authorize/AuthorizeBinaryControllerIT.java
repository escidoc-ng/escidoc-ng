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
import java.util.HashMap;
import java.util.Map;

import net.objecthunter.larch.integration.helpers.AuthConfigurer;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.MissingPermission;
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.source.UrlSource;
import net.objecthunter.larch.test.util.Fixtures;

import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizeBinaryControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeBinaryControllerIT.class);

    @Test
    public void testCreateBinaryStream() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary")
                .body(MultipartEntityBuilder.create()
                        .addTextBody("name", "test")
                        .addTextBody("mimetype", "image/png")
                        .addPart(
                                "binary",
                                new FileBody(new File(Fixtures.class.getClassLoader().getResource(
                                        "fixtures/image_1.png").getFile())))
                        .build())
                .neededPermission(MissingPermission.WRITE_PENDING_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());

        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary")
                .body(MultipartEntityBuilder.create()
                        .addTextBody("name", "test")
                        .addTextBody("mimetype", "image/png")
                        .addPart(
                                "binary",
                                new FileBody(new File(Fixtures.class.getClassLoader().getResource(
                                        "fixtures/image_1.png").getFile())))
                        .build())
                .neededPermission(MissingPermission.WRITE_SUBMITTED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());

        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary")
                .body(MultipartEntityBuilder.create()
                        .addTextBody("name", "test")
                        .addTextBody("mimetype", "image/png")
                        .addPart(
                                "binary",
                                new FileBody(new File(Fixtures.class.getClassLoader().getResource(
                                        "fixtures/image_1.png").getFile())))
                        .build())
                .neededPermission(MissingPermission.WRITE_PUBLISHED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());

    }

    @Test
    public void testCreateBinaryStreamHtml() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary")
                .body(MultipartEntityBuilder.create()
                        .addTextBody("name", "test")
                        .addTextBody("mimetype", "image/png")
                        .addPart(
                                "binary",
                                new FileBody(new File(Fixtures.class.getClassLoader().getResource(
                                        "fixtures/image_1.png").getFile())))
                        .build())
                .neededPermission(MissingPermission.WRITE_PENDING_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());

        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary")
                .body(MultipartEntityBuilder.create()
                        .addTextBody("name", "test")
                        .addTextBody("mimetype", "image/png")
                        .addPart(
                                "binary",
                                new FileBody(new File(Fixtures.class.getClassLoader().getResource(
                                        "fixtures/image_1.png").getFile())))
                        .build())
                .neededPermission(MissingPermission.WRITE_SUBMITTED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());

        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary")
                .body(MultipartEntityBuilder.create()
                        .addTextBody("name", "test")
                        .addTextBody("mimetype", "image/png")
                        .addPart(
                                "binary",
                                new FileBody(new File(Fixtures.class.getClassLoader().getResource(
                                        "fixtures/image_1.png").getFile())))
                        .build())
                .neededPermission(MissingPermission.WRITE_PUBLISHED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());

    }

    @Test
    public void testCreateBinaryJson() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary")
                .body(mapper.writeValueAsString(getBinary()))
                .neededPermission(MissingPermission.WRITE_PENDING_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());

        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary")
                .body(mapper.writeValueAsString(getBinary()))
                .neededPermission(MissingPermission.WRITE_SUBMITTED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());

        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary")
                .body(mapper.writeValueAsString(getBinary()))
                .neededPermission(MissingPermission.WRITE_PUBLISHED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    @Test
    public void testCreateBinaryJsonHtml() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary")
                .body(mapper.writeValueAsString(getBinary()))
                .neededPermission(MissingPermission.WRITE_PENDING_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());

        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary")
                .body(mapper.writeValueAsString(getBinary()))
                .neededPermission(MissingPermission.WRITE_SUBMITTED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());

        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary")
                .body(mapper.writeValueAsString(getBinary()))
                .neededPermission(MissingPermission.WRITE_PUBLISHED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .html(true)
                .build());
    }

    @Test
    public void testRetrieveBinary() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary/image-1")
                .neededPermission(MissingPermission.READ_PENDING_BINARY)
                .build());

        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary/image-1")
                .neededPermission(MissingPermission.READ_SUBMITTED_BINARY)
                .build());

        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary/image-1")
                .build());
    }

    @Test
    public void testRetrieveBinaryHtml() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary/image-1")
                .neededPermission(MissingPermission.READ_PENDING_BINARY)
                .html(true)
                .build());

        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary/image-1")
                .neededPermission(MissingPermission.READ_SUBMITTED_BINARY)
                .html(true)
                .build());

        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary/image-1")
                .html(true)
                .build());
    }

    @Test
    public void testDownloadBinary() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary/image-1/content")
                .neededPermission(MissingPermission.READ_PENDING_BINARY)
                .build());

        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary/image-1/content")
                .neededPermission(MissingPermission.READ_SUBMITTED_BINARY)
                .build());

        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/entity/" + entity.getId() + "/binary/image-1/content")
                .build());
    }

    @Test
    public void testDeleteBinary() throws Exception {
        // create pending entity
        Entity entity = createEntity(Entity.STATE_PENDING, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/image-1")
                .neededPermission(MissingPermission.WRITE_PENDING_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());

        // create submitted entity
        entity = createEntity(Entity.STATE_SUBMITTED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/image-1")
                .neededPermission(MissingPermission.WRITE_SUBMITTED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());

        // create published entity
        entity = createEntity(Entity.STATE_PUBLISHED, workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, workspaceUrl + workspaceId + "/entity/" + entity.getId() +
                        "/binary/image-1")
                .neededPermission(MissingPermission.WRITE_PUBLISHED_BINARY)
                .resetState(true)
                .resetStateId(entity.getId())
                .build());
    }

    private Binary getBinary() throws Exception {
        Binary bin1 = new Binary();
        bin1.setMimetype("image/png");
        bin1.setFilename("test.png");
        bin1.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png").toURI()));
        bin1.setName("test");
        Map<String, Metadata> bin1Md = new HashMap<>();
        bin1.setMetadata(bin1Md);
        return bin1;
    }

}
