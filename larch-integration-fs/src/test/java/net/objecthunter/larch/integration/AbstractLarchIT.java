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
 * WITHOUT WARRANTIES OR CONDITIONS OF ROLE_ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.objecthunter.larch.integration;

import static net.objecthunter.larch.test.util.Fixtures.LEVEL1_ID;
import static net.objecthunter.larch.test.util.Fixtures.LEVEL2_ID;
import static net.objecthunter.larch.test.util.Fixtures.createContentModel;
import static net.objecthunter.larch.test.util.Fixtures.createFixtureEntity;
import static net.objecthunter.larch.test.util.Fixtures.createRandomDCMetadata;
import static net.objecthunter.larch.test.util.Fixtures.createRandomBinary;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import net.objecthunter.larch.LarchServerConfiguration;
import net.objecthunter.larch.integration.helpers.NullOutputStream;
import net.objecthunter.larch.model.AlternativeIdentifier;
import net.objecthunter.larch.model.AlternativeIdentifier.IdentifierType;
import net.objecthunter.larch.model.Archive;
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.ContentModel;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.UserRequest;
import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.model.security.role.Role.RoleRight;
import net.objecthunter.larch.model.source.UrlSource;
import net.objecthunter.larch.test.util.Fixtures;

import net.objecthunter.larch.test.util.SftpServerConfiguration;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {LarchServerConfiguration.class, SftpServerConfiguration.class})
@IntegrationTest
@WebAppConfiguration
@ActiveProfiles("fs")
public abstract class AbstractLarchIT {

    protected static final int port = 8080;

    protected static final String hostUrl = "http://localhost:" + port + "/";

    protected static final String entityUrl = hostUrl + "entity/";

    protected static final String userUrl = hostUrl + "user/";

    protected static final String contentModelUrl = hostUrl + "content-model/";

    protected static final String confirmUrl = hostUrl + "confirm/";

    protected static final String adminUsername = "admin";

    protected static final String adminPassword = "admin";

    protected static final String IGNORE = "ignore";

    protected boolean wsCreated = false;

    final PrintStream sysOut = System.out;

    final PrintStream sysErr = System.err;

    @Autowired
    protected ObjectMapper mapper;

    private HttpHost localhost = new HttpHost("localhost", 8080, "http");

    private Executor adminExecutor = Executor.newInstance().auth(localhost, adminUsername, adminPassword)
            .authPreemptive(
                    localhost);

    @Before
    public void resetSystOutErr() throws Exception {
        this.showLog();
        if (!wsCreated) {
            // create default level1
            Entity level1 = new Entity();
            level1.setId(LEVEL1_ID);
            level1.setContentModelId(FixedContentModel.LEVEL1.getName());
            Request r = Request.Post(entityUrl)
                    .bodyString(mapper.writeValueAsString(level1), ContentType.APPLICATION_JSON);
            HttpResponse response = this.executeAsAdmin(r);
            // create default level2
            Entity level2 = new Entity();
            level2.setId(LEVEL2_ID);
            level2.setContentModelId(FixedContentModel.LEVEL2.getName());
            level2.setLabel("Test Level2");
            level2.setParentId(LEVEL1_ID);
            r = Request.Post(entityUrl)
                    .bodyString(mapper.writeValueAsString(level2), ContentType.APPLICATION_JSON);
            response = this.executeAsAdmin(r);
            wsCreated = true;
        }
    }

    protected String createLevel1() throws IOException {
        Entity level1 = Fixtures.createLevel1();
        HttpResponse resp = this.executeAsAdmin(Request.Post(entityUrl)
                .bodyString(this.mapper.writeValueAsString(level1), ContentType.APPLICATION_JSON));
        String test = EntityUtils.toString(resp.getEntity());
        String level1Id = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());
        assertNotNull(level1Id);
        assertEquals(level1.getId(), level1Id);
        return level1Id;
    }

    protected String createLevel2(String level1Id) throws IOException {
        Entity level2 = Fixtures.createLevel2(level1Id);
        HttpResponse resp = this.executeAsAdmin(Request.Post(entityUrl)
                .bodyString(this.mapper.writeValueAsString(level2), ContentType.APPLICATION_JSON));

        String test = EntityUtils.toString(resp.getEntity());
        String level2Id = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());
        assertNotNull(level2Id);
        assertEquals(level2.getId(), level2Id);
        return level2Id;
    }

    protected String createContentModel(String id, int expectedStatus) throws IOException {
        ContentModel contentModel = Fixtures.createContentModel();
        if (id == null || !id.equals(IGNORE)) {
            contentModel.setId(id);
        }
        HttpResponse resp = this.executeAsAdmin(Request.Post(contentModelUrl)
                .bodyString(this.mapper.writeValueAsString(contentModel), ContentType.APPLICATION_JSON));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        String contentModelId = null;
        if (expectedStatus == 201) {
            contentModelId = EntityUtils.toString(resp.getEntity());
            assertNotNull(contentModelId);
        }
        return contentModelId;
    }

    protected ContentModel retrieveContentModel(String id, int expectedStatus) throws IOException {
        HttpResponse resp = this.executeAsAdmin(Request.Get(contentModelUrl + id));
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        ContentModel fetched = null;
        if (expectedStatus == 201) {
            fetched = mapper.readValue(resp.getEntity().getContent(), ContentModel.class);
            assertEquals(id, fetched.getId());
        }
        return fetched;
    }

    protected void deleteContentModel(String id, int expectedStatus) throws IOException {
        HttpResponse resp = this.executeAsAdmin(Request.Delete(contentModelUrl + id));
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
    }

    protected String createUser(String name, String password) throws IOException {
        if (StringUtils.isBlank(name)) {
            name = RandomStringUtils.randomAlphabetic(5);
        }
        HttpResponse resp =
                executeAsAdmin(
                Request.Post(userUrl)
                        .body(MultipartEntityBuilder.create()
                                .addTextBody("name", name)
                                .addTextBody("first_name", "test")
                                .addTextBody("last_name", "test")
                                .addTextBody("email", name + "@fiz.de")
                                .build()
                        ));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        final String token = EntityUtils.toString(resp.getEntity());
        resp =
                executeAsAdmin(
                Request.Post(confirmUrl + token)
                        .body(MultipartEntityBuilder.create()
                                .addTextBody("password", password)
                                .addTextBody("passwordRepeat", password)
                                .build()
                        ));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        return name;
    }

    protected UserRequest createUserRequest(String name, String password) throws IOException {
        if (StringUtils.isBlank(name)) {
            name = RandomStringUtils.randomAlphabetic(5);
        }
        HttpResponse resp =
                executeAsAdmin(
                Request.Post(userUrl)
                        .body(MultipartEntityBuilder.create()
                                .addTextBody("name", name)
                                .addTextBody("first_name", "test")
                                .addTextBody("last_name", "test")
                                .addTextBody("email", name + "@fiz.de")
                                .build()
                        ));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        final String token = EntityUtils.toString(resp.getEntity());
        UserRequest userRequest = new UserRequest();
        userRequest.setToken(token);
        User user = new User();
        user.setName(name);
        userRequest.setUser(user);
        return userRequest;
    }

    /**
     * Add Role for anchorId where user with provided username has all rights.
     * 
     * @param username
     * @param role role
     * @param anchorId anchorId
     * @throws Exception
     */
    protected void createRoleForUser(String username, Role role, String anchorId)
            throws Exception {
        // try to retrieve user
        HttpResponse resp = this.executeAsAdmin(Request.Get(userUrl + username));
        String result = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());
        User fetched = this.mapper.readValue(resp.getEntity().getContent(), User.class);

        List<Role> userRoles = (ArrayList) fetched.getRoles();
        if (userRoles == null) {
            userRoles = new ArrayList<Role>();
        }
        Map<String, List<RoleRight>> newRights = new HashMap<String, List<RoleRight>>();
        for (Role userRole : userRoles) {
            if (userRole.getRoleName().equals(role.getRoleName()) && userRole.getRights() != null) {
                newRights = userRole.getRights();
                userRoles.remove(userRole);
                break;
            }
        }

        if (anchorId != null) {
            List<RoleRight> roleRights = new ArrayList<RoleRight>();
            for (RoleRight roleRight : role.allowedRights()) {
                roleRights.add(roleRight);
            }
            if (!roleRights.isEmpty()) {
                newRights.put(anchorId, roleRights);
                role.setRights(newRights);
                userRoles.add(role);
            }
        }

        // set roles
        resp = this.executeAsAdmin(Request.Post(userUrl + username + "/roles")
                .bodyString(this.mapper.writeValueAsString(userRoles), ContentType.APPLICATION_JSON));
        result = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());

    }

    /**
     * @param status
     * @return String entityId
     */
    protected Entity createEntity(EntityState status, String contentModelId, String parentId) throws Exception {
        if (status == null ||
                (!status.equals(EntityState.PENDING) && !status.equals(EntityState.SUBMITTED) &&
                        !status.equals(EntityState.PUBLISHED) && !status.equals(EntityState.WITHDRAWN))) {
            throw new Exception("given status not valid");
        }
        Entity e = createFixtureEntity();
        e.setState(status);
        e.setParentId(parentId);
        e.setContentModelId(contentModelId);
        AlternativeIdentifier identifier = new AlternativeIdentifier();
        identifier.setType(IdentifierType.DOI.name);
        identifier.setValue("testdoi");
        List<AlternativeIdentifier> identifiers = new ArrayList<AlternativeIdentifier>();
        identifiers.add(identifier);
        e.setAlternativeIdentifiers(identifiers);
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl).bodyString(
                                mapper.writeValueAsString(e),
                                ContentType.APPLICATION_JSON));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());
        String entityId = EntityUtils.toString(resp.getEntity());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entityId));
        String response = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        return fetched;
    }

    /**
     * Add Metadata to an Entity.
     * 
     * @param entity entity
     * @param mdName name of mdRecord to add
     * @return Entity updated entity
     */
    protected Entity addMetadataStream(Entity entity, String mdName, String mdType, String data, int expectedStatus)
            throws Exception {
        Metadata metadata = createRandomDCMetadata();
        if (mdName == null || !mdName.equals(IGNORE)) {
            metadata.setName(mdName);
        }
        if (mdType == null || !mdType.equals(IGNORE)) {
            metadata.setType(mdType);
        }
        if (data == null || !data.equals(IGNORE)) {
            metadata.setData(data);
        }
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl + entity.getId() + "/metadata").bodyString(
                                mapper.writeValueAsString(metadata),
                                ContentType.APPLICATION_JSON));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        Entity fetched = null;
        if (resp.getStatusLine().getStatusCode() == 200) {
            fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            if (expectedStatus == 201) {
                // check if metadata exists
                assertTrue(fetched.getMetadata().containsKey(metadata.getName()));
                assertEquals(metadata.getType(), fetched.getMetadata().get(metadata.getName()).getType());
                assertEquals(metadata.getData(), fetched.getMetadata().get(metadata.getName()).getData());
            } else {
                assertFalse(fetched.getMetadata().containsKey(metadata.getName()));
            }
        }
        return fetched;
    }

    /**
     * Add Metadata to an Entity.
     * 
     * @param entity entity
     * @param mdName name of mdRecord to add
     * @return Entity updated entity
     */
    protected Entity
            addMetadataMultipart(Entity entity, String mdName, String mdType, String data, int expectedStatus)
                    throws Exception {
        Metadata metadata = createRandomDCMetadata();
        if (mdName == null || !mdName.equals(IGNORE)) {
            metadata.setName(mdName);
        }
        if (mdType == null || !mdType.equals(IGNORE)) {
            metadata.setType(mdType);
        }
        if (data == null || !data.equals(IGNORE)) {
            metadata.setData(data);
        }
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl + entity.getId() + "/metadata").body(MultipartEntityBuilder.create()
                                .addTextBody("name", metadata.getName())
                                .addTextBody("type", metadata.getType())
                                .addPart(
                                        "data",
                                        new StringBody(metadata.getData(), ContentType.APPLICATION_XML))
                                .build()));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        Entity fetched = null;
        if (resp.getStatusLine().getStatusCode() == 200) {
            fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            if (expectedStatus == 201) {
                // check if metadata exists
                assertTrue(fetched.getMetadata().containsKey(metadata.getName()));
                assertEquals(metadata.getType(), fetched.getMetadata().get(metadata.getName()).getType());
                assertEquals(metadata.getData(), fetched.getMetadata().get(metadata.getName()).getData());
            } else {
                assertFalse(fetched.getMetadata().containsKey(metadata.getName()));
            }
        }
        return fetched;
    }

    /**
     * Remove Metadata from an Entity.
     * 
     * @param entity entity
     * @param mdName name of mdRecord to remove
     * @return Entity updated entity
     */
    protected Entity removeMetadata(Entity entity, String mdName, int expectedStatus) throws Exception {
        String tmdName = mdName;
        if (tmdName != null && tmdName.equals(IGNORE)) {
            tmdName = entity.getMetadata().keySet().iterator().next();
        }
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Delete(entityUrl + entity.getId() + "/metadata/" + tmdName));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        Entity fetched = null;
        if (resp.getStatusLine().getStatusCode() == 200) {
            fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            if (expectedStatus == 200) {
                // check if metadata doesnt exist
                assertFalse(fetched.getMetadata().containsKey(tmdName));
            }
        }
        return fetched;
    }

    /**
     * Add Binary Metadata to an Entity.
     * 
     * @param entity entity
     * @param binaryName name of binary to add metadata to
     * @param mdName name of mdRecord to add
     * @return Entity updated entity
     */
    protected Entity addBinaryMetadataStream(Entity entity, String binaryName, String mdName, String mdType,
            String data, int expectedStatus) throws Exception {
        String bName = binaryName;
        if (bName != null && bName.equals(IGNORE)) {
            bName = entity.getBinaries().keySet().iterator().next();
        }
        Metadata metadata = createRandomDCMetadata();
        if (mdName == null || !mdName.equals(IGNORE)) {
            metadata.setName(mdName);
        }
        if (mdType == null || !mdType.equals(IGNORE)) {
            metadata.setType(mdType);
        }
        if (data == null || !data.equals(IGNORE)) {
            metadata.setData(data);
        }
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(
                                entityUrl + entity.getId() + "/binary/" +
                                        bName + "/metadata").bodyString(
                                mapper.writeValueAsString(metadata),
                                ContentType.APPLICATION_JSON));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        Entity fetched = null;
        if (resp.getStatusLine().getStatusCode() == 200) {
            fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            if (expectedStatus == 201) {
                // check if metadata exists
                assertTrue(fetched.getBinaries().get(bName).getMetadata().containsKey(metadata.getName()));
                assertEquals(metadata.getType(), fetched.getBinaries().get(bName).getMetadata().get(
                        metadata.getName()).getType());
                assertEquals(metadata.getData(), fetched.getBinaries().get(bName).getMetadata().get(
                        metadata.getName()).getData());
            } else {
                if (fetched.getBinaries().containsKey(bName)) {
                    assertFalse(fetched.getBinaries().get(bName).getMetadata().containsKey(metadata.getName()));
                }
            }
        }
        return fetched;
    }

    /**
     * Add Binary Metadata to an Entity.
     * 
     * @param entity entity
     * @param binaryName name of binary to add metadata to
     * @param mdName name of mdRecord to add
     * @return Entity updated entity
     */
    protected Entity addBinaryMetadataMultipart(Entity entity, String binaryName, String mdName, String mdType,
            String data, int expectedStatus) throws Exception {
        String bName = binaryName;
        if (bName != null && bName.equals(IGNORE)) {
            bName = entity.getBinaries().keySet().iterator().next();
        }
        Metadata metadata = createRandomDCMetadata();
        if (mdName == null || !mdName.equals(IGNORE)) {
            metadata.setName(mdName);
        }
        if (mdType == null || !mdType.equals(IGNORE)) {
            metadata.setType(mdType);
        }
        if (data == null || !data.equals(IGNORE)) {
            metadata.setData(data);
        }
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(
                                entityUrl + entity.getId() + "/binary/" +
                                        bName + "/metadata").body(MultipartEntityBuilder.create()
                                .addTextBody("name", metadata.getName())
                                .addTextBody("type", metadata.getType())
                                .addPart(
                                        "data",
                                        new StringBody(metadata.getData(), ContentType.APPLICATION_XML))
                                .build()));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        Entity fetched = null;
        if (resp.getStatusLine().getStatusCode() == 200) {
            fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            if (expectedStatus == 201) {
                // check if metadata exists
                assertTrue(fetched.getBinaries().get(bName).getMetadata().containsKey(metadata.getName()));
                assertEquals(metadata.getType(), fetched.getBinaries().get(bName).getMetadata().get(
                        metadata.getName()).getType());
                assertEquals(metadata.getData(), fetched.getBinaries().get(bName).getMetadata().get(
                        metadata.getName()).getData());
            } else {
                if (fetched.getBinaries().containsKey(bName)) {
                    assertFalse(fetched.getBinaries().get(bName).getMetadata().containsKey(metadata.getName()));
                }
            }
        }
        return fetched;
    }

    /**
     * Remove Binary Metadata from an Entity.
     * 
     * @param entity entity
     * @param binaryName name of binary to remove metadata from
     * @param mdName name of mdRecord to remove
     * @return Entity updated entity
     */
    protected Entity removeBinaryMetadata(Entity entity, String binaryName, String mdName, int expectedStatus)
            throws Exception {
        String bName = binaryName;
        String mName = mdName;
        Binary binary = null;
        if (bName != null && bName.equals(IGNORE)) {
            binary = entity.getBinaries().values().iterator().next();
        } else {
            binary = entity.getBinaries().get(bName);
        }
        bName = binary.getName();
        if (mName != null && mName.equals(IGNORE)) {
            mName = binary.getMetadata().keySet().iterator().next();
        }
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Delete(
                                entityUrl + entity.getId() + "/binary/" +
                                        bName + "/metadata/" + mName));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        Entity fetched = null;
        if (resp.getStatusLine().getStatusCode() == 200) {
            fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            if (expectedStatus == 200) {
                // check if metadata doesnt exist
                assertFalse(fetched.getBinaries().get(bName).getMetadata().containsKey(mName));
            }
        }
        return fetched;
    }

    /**
     * Add Binary to an Entity.
     * 
     * @param entity entity
     * @param binaryName name of the binary to add
     * @return Entity updated entity
     */
    protected Entity addBinaryStream(Entity entity, String binaryName, String mimetype, String resource,
            int expectedStatus) throws Exception {
        Binary binary = createRandomBinary();
        if (binaryName == null || !binaryName.equals(IGNORE)) {
            binary.setName(binaryName);
        }
        if (mimetype == null || !mimetype.equals(IGNORE)) {
            binary.setMimetype(mimetype);
        }
        if (resource == null || !resource.equals(IGNORE)) {
            if (resource == null) {
                binary.setSource(null);
            } else {
                binary.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource(resource).toURI()));
            }
        }
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(
                                entityUrl + entity.getId() + "/binary").bodyString(
                                mapper.writeValueAsString(binary),
                                ContentType.APPLICATION_JSON));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        Entity fetched = null;
        if (resp.getStatusLine().getStatusCode() == 200) {
            fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            if (expectedStatus == 201) {
                // check if binary exists
                assertTrue(fetched.getBinaries().containsKey(binary.getName()));
                assertEquals(binary.getMimetype(), fetched.getBinaries().get(binary.getName()).getMimetype());
            }
        }
        return fetched;
    }

    /**
     * Add Binary to an Entity.
     * 
     * @param entity entity
     * @param binaryName name of the binary to add
     * @return Entity updated entity
     */
    protected Entity addBinaryMultipart(Entity entity, String binaryName, String mimetype, String resource,
            int expectedStatus) throws Exception {
        Binary binary = createRandomBinary();
        if (binaryName == null || !binaryName.equals(IGNORE)) {
            binary.setName(binaryName);
        }
        if (mimetype == null || !mimetype.equals(IGNORE)) {
            binary.setMimetype(mimetype);
        }

        if (resource != null && resource.equals(IGNORE)) {
            resource = "fixtures/image_1.png";
        }
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        if (binary.getName() != null) {
            entityBuilder.addTextBody("name", binary.getName());
        }
        if (resource != null) {
            if (binary.getMimetype() != null) {
                entityBuilder.addBinaryBody("binary", new File(Fixtures.class.getClassLoader().getResource(
                        resource).getFile()), ContentType.create(binary.getMimetype()), resource);
            } else {
                entityBuilder.addBinaryBody("binary", new File(Fixtures.class.getClassLoader().getResource(
                        resource).getFile()), ContentType.create(null), resource);
            }
        }
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(
                                entityUrl + entity.getId() + "/binary").body(entityBuilder.build()));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        Entity fetched = null;
        if (resp.getStatusLine().getStatusCode() == 200) {
            fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            if (expectedStatus == 201) {
                // check if binary exists
                assertTrue(fetched.getBinaries().containsKey(binary.getName()));
                assertEquals(binary.getMimetype(), fetched.getBinaries().get(binary.getName()).getMimetype());
            }
        }
        return fetched;
    }

    /**
     * Remove Binary from an Entity.
     * 
     * @param entity entity
     * @param binaryName name of the binary to remove
     * @return Entity updated entity
     */
    protected Entity removeBinary(Entity entity, String binaryName, int expectedStatus) throws Exception {
        String bName = binaryName;
        if (bName != null && bName.equals(IGNORE)) {
            bName = entity.getBinaries().keySet().iterator().next();
        }
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Delete(
                                entityUrl + entity.getId() + "/binary/" + bName));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        Entity fetched = null;
        if (resp.getStatusLine().getStatusCode() == 200) {
            fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            if (expectedStatus == 200) {
                // check if binary doesnt exist
                assertFalse(fetched.getBinaries().containsKey(bName));
            }
        }
        return fetched;
    }

    /**
     * Retrieve Binary for an Entity.
     * 
     * @param entity entity
     * @param binaryName name of the binary to remove
     * @param expectedStatus expected status
     * @return Entity updated entity
     */
    protected Binary retrieveBinary(Entity entity, String binaryName, int expectedStatus) throws Exception {
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Get(
                                entityUrl + entity.getId() + "/binary/" + binaryName));
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        Binary fetched = null;
        if (resp.getStatusLine().getStatusCode() == 200) {
            fetched = mapper.readValue(resp.getEntity().getContent(), Binary.class);
        }
        return fetched;
    }

    /**
     * Download Binary Content for an Entity.
     * 
     * @param entity entity
     * @param binaryName name of the binary to remove
     * @param expectedStatus expected status
     */
    protected void downloadBinaryContent(Entity entity, String binaryName, int expectedStatus) throws Exception {
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Get(
                                entityUrl + entity.getId() + "/binary/" + binaryName + "/content"));
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
    }

    /**
     * Add an Identifier to an Entity.
     * 
     * @param entity entity
     * @param type type
     * @param value value
     * @return Entity updated entity
     */
    protected Entity addIdentifier(Entity entity, String type, String value, int expectedStatus) throws Exception {
        String ttype = AlternativeIdentifier.IdentifierType.DOI.name();
        String tvalue = "identifier-" + RandomStringUtils.randomAlphabetic(16);
        if (type == null || !type.equals(IGNORE)) {
            ttype = type;
        }
        if (value == null || !value.equals(IGNORE)) {
            tvalue = value;
        }
        NameValuePair typePair = new BasicNameValuePair("type", ttype);
        NameValuePair valuePair = new BasicNameValuePair("value", tvalue);
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(
                                entityUrl + entity.getId() + "/identifier").bodyForm(typePair, valuePair));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        Entity fetched = null;
        if (resp.getStatusLine().getStatusCode() == 200) {
            fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            boolean found = false;
            if (expectedStatus == 201) {
                // check if identifier exists
                for (AlternativeIdentifier alternativeIdentifier : fetched.getAlternativeIdentifiers()) {
                    if (alternativeIdentifier.getType().equals(ttype) &&
                            alternativeIdentifier.getValue().equals(tvalue)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    fail("identifier not found");
                }
            } else {
                for (AlternativeIdentifier alternativeIdentifier : fetched.getAlternativeIdentifiers()) {
                    if (alternativeIdentifier.getType().equals(ttype) &&
                            alternativeIdentifier.getValue().equals(tvalue)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    fail("identifier found");
                }
            }
        }
        return fetched;
    }

    /**
     * Remove an Identifier from an Entity.
     * 
     * @param entity entity
     * @param type type
     * @param value value
     * @return Entity updated entity
     */
    protected Entity removeIdentifier(Entity entity, String type, String value, int expectedStatus) throws Exception {
        String ttype = type;
        String tvalue = value;
        if (ttype != null && ttype.equals(IGNORE)) {
            ttype = entity.getAlternativeIdentifiers().get(0).getType();
        }
        if (tvalue != null && tvalue.equals(IGNORE)) {
            tvalue = entity.getAlternativeIdentifiers().get(0).getValue();
        }
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Delete(
                                entityUrl + entity.getId() + "/identifier/" + ttype + "/" + tvalue));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        Entity fetched = null;
        if (resp.getStatusLine().getStatusCode() == 200) {
            fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            if (expectedStatus == 200) {
                boolean found = false;
                for (AlternativeIdentifier alternativeIdentifier : fetched.getAlternativeIdentifiers()) {
                    if (alternativeIdentifier.getType().equals(ttype) &&
                            alternativeIdentifier.getValue().equals(tvalue)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    fail("identifier found");
                }
            }
        }
        return fetched;
    }

    /**
     * Add a Relation to an Entity.
     * 
     * @param entity entity
     * @param predicate predicate
     * @param object object
     * @return Entity updated entity
     */
    protected Entity addRelation(Entity entity, String predicate, String object, int expectedStatus) throws Exception {
        String tpredicate = "predicate-" + RandomStringUtils.randomAlphabetic(16);
        String tobject = "object-" + RandomStringUtils.randomAlphabetic(16);
        if (predicate == null || !predicate.equals(IGNORE)) {
            tpredicate = predicate;
        }
        if (object == null || !object.equals(IGNORE)) {
            tobject = object;
        }
        NameValuePair predicatePair = new BasicNameValuePair("predicate", tpredicate);
        NameValuePair objectPair = new BasicNameValuePair("object", tobject);
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(
                                entityUrl + entity.getId() + "/relation").bodyForm(predicatePair, objectPair));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        Entity fetched = null;
        if (resp.getStatusLine().getStatusCode() == 200) {
            fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            if (expectedStatus == 201) {
                // check if relation exists
                assertTrue(fetched.getRelations().containsKey(tpredicate));
                assertTrue(fetched.getRelations().get(tpredicate).contains(tobject));
            } else {
                assertFalse(fetched.getRelations().containsKey(tpredicate));
            }
        }
        return fetched;
    }

    protected HttpResponse executeAsAdmin(Request req) throws IOException {
        return this.adminExecutor.execute(req).returnResponse();
    }

    protected void checkResponseError(HttpResponse response, int statusCode,
            Class<? extends Exception> expectedException,
            String message)
            throws IOException {
        JsonNode error = mapper.readTree(response.getEntity().getContent());
        assertEquals(statusCode, response.getStatusLine().getStatusCode());
        assertEquals(statusCode, error.get("status").asInt());
        assertEquals(expectedException.getName(), error.get("exception").asText());
        assertEquals(message, error.get("message").asText());
    }

    protected void hideLog() {
        System.setOut(new PrintStream(NullOutputStream.getInstance()));
        System.setErr(new PrintStream(NullOutputStream.getInstance()));
    }

    protected void showLog() {
        System.setOut(sysOut);
        System.setErr(sysErr);
    }

    protected static void assertStatusEquals(HttpResponse expected, HttpResponse actual) throws AssertionError {
        int expectedStatus = expected.getStatusLine().getStatusCode();
        int actualStatus = actual.getStatusLine().getStatusCode();
        if (expectedStatus == 302 &&
                (expected.getFirstHeader("Location") == null ||
                        expected.getFirstHeader("Location").getValue() == null || !expected
                        .getFirstHeader("Location")
                        .getValue().contains("login"))) {
            expectedStatus = 200;
        }
        if (actualStatus == 302 &&
                (actual.getFirstHeader("Location") == null ||
                        actual.getFirstHeader("Location").getValue() == null || !actual.getFirstHeader("Location")
                        .getValue().contains("login"))) {
            actualStatus = 200;
        }
        if ((int) (expectedStatus / 100) == 2 &&
                (int) (actualStatus / 100) == 2) {
            return;
        }

        if (expectedStatus != actualStatus) {
            throw new AssertionError(expectedStatus + " not equals " + actualStatus);
        }
    }

    protected Entity archive(Entity e) throws Exception {
        HttpResponse resp = this.executeAsAdmin(Request.Get(entityUrl + "/" + e.getId()));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        final Entity fetched = this.mapper.readValue(EntityUtils.toString(resp.getEntity()), Entity.class);

        resp = this.executeAsAdmin(Request.Put(hostUrl + "/archive/" + fetched.getId() + "/" + fetched.getVersion()));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        return fetched;
    }

    protected Entity ingestAndArchive(Entity e) throws Exception {
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl).bodyString(mapper.writeValueAsString(e),
                                ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String id = EntityUtils.toString(resp.getEntity());

        resp = this.executeAsAdmin(Request.Get(entityUrl + "/" + id));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        final Entity fetched = this.mapper.readValue(EntityUtils.toString(resp.getEntity()), Entity.class);

        resp = this.executeAsAdmin(Request.Put(hostUrl + "/archive/" + fetched.getId() + "/" + fetched.getVersion()));
        assertEquals(201, resp.getStatusLine().getStatusCode());

        return fetched;
    }

    protected Archive retrieveArchive(String id, int version, int expectedStatus) throws Exception {
        HttpResponse resp = this.executeAsAdmin(Request.Get(hostUrl + "/archive/" + id + "/" + version));
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        if (expectedStatus != 200) {
            return null;
        }
        return this.mapper.readValue(resp.getEntity().getContent(), Archive.class);
    }

    public HttpResponse listArchives(int offset, int length) throws IOException {
        return this.executeAsAdmin(Request.Get(hostUrl + "/archive/list/" + offset + "/" + length));
    }

    public ZipInputStream retrieveContent(String id, int version, int expectedStatus) throws IOException {
        HttpResponse resp = this.executeAsAdmin(Request.Get(hostUrl + "/archive/" + id + "/" + version + "/content"));
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        return new ZipInputStream(resp.getEntity().getContent());
    }
}
