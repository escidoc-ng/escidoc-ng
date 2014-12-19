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

package net.objecthunter.larch.integration.fs;

import static net.objecthunter.larch.test.util.Fixtures.LEVEL1_ID;
import static net.objecthunter.larch.test.util.Fixtures.LEVEL2_ID;
import static net.objecthunter.larch.test.util.Fixtures.createFixtureEntity;
import static net.objecthunter.larch.test.util.Fixtures.createRandomBinary;
import static net.objecthunter.larch.test.util.Fixtures.createRandomDCMetadata;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import net.objecthunter.larch.LarchServerConfiguration;
import net.objecthunter.larch.integration.fs.helpers.NullOutputStream;
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
import net.objecthunter.larch.model.security.role.Right;
import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.model.security.role.Role.RoleRight;
import net.objecthunter.larch.model.source.ByteArraySource;
import net.objecthunter.larch.test.util.Fixtures;
import net.objecthunter.larch.test.util.SftpServerConfiguration;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
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
@SpringApplicationConfiguration(classes = { LarchServerConfiguration.class, SftpServerConfiguration.class })
@IntegrationTest
@WebAppConfiguration
@ActiveProfiles({"archive-sftp", "blobstore-fs"})
public abstract class AbstractFSLarchIT {

    protected static final int port = 8080;

    protected static final String hostUrl = "http://localhost:" + port + "/";

    protected static final String entityUrl = hostUrl + "entity/";

    protected static final String archiveUrl = hostUrl + "archive/";

    protected static final String userUrl = hostUrl + "user/";

    protected static final String contentModelUrl = hostUrl + "content-model/";

    protected static final String confirmUrl = hostUrl + "confirm/";

    protected static final String entitySearchUrl = hostUrl + "search/entities";

    protected static final String userSearchUrl = hostUrl + "search/users";

    protected static final String archiveSearchUrl = hostUrl + "search/archives";

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
            createArchive(level1.getId(), 1);
            // create default level2
            Entity level2 = new Entity();
            level2.setId(LEVEL2_ID);
            level2.setContentModelId(FixedContentModel.LEVEL2.getName());
            level2.setLabel("Test Level2");
            level2.setParentId(LEVEL1_ID);
            r = Request.Post(entityUrl)
                    .bodyString(mapper.writeValueAsString(level2), ContentType.APPLICATION_JSON);
            response = this.executeAsAdmin(r);
            createArchive(level2.getId(), 1);
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

    protected void createArchive(String entityId, int version) throws IOException {
        HttpResponse resp = this.executeAsAdmin(Request.Put(hostUrl + "/archive/" + entityId + "/" + version));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        EntityUtils.consume(resp.getEntity());
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
        } else {
            EntityUtils.consume(resp.getEntity());
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
        } else {
            EntityUtils.consume(resp.getEntity());
        }
        return fetched;
    }

    protected void deleteContentModel(String id, int expectedStatus) throws IOException {
        HttpResponse resp = this.executeAsAdmin(Request.Delete(contentModelUrl + id));
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        EntityUtils.consume(resp.getEntity());
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
        EntityUtils.consume(resp.getEntity());
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
        EntityUtils.consume(resp.getEntity());
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
        List<Right> newRights = new ArrayList<Right>();
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
                newRights.add(new Right(anchorId, roleRights));
                role.setRights(newRights);
                userRoles.add(role);
            }
        }

        // set roles
        resp = this.executeAsAdmin(Request.Post(userUrl + username + "/roles")
                .bodyString(this.mapper.writeValueAsString(userRoles), ContentType.APPLICATION_JSON));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        EntityUtils.consume(resp.getEntity());
    }

    /**
     * @param status
     * @param indexInline TODO
     * @return String entityId
     */
    protected Entity createEntity(EntityState status, String contentModelId, String parentId, boolean indexInline)
            throws Exception {
        if (status == null ||
                (!status.equals(EntityState.PENDING) && !status.equals(EntityState.SUBMITTED) &&
                        !status.equals(EntityState.PUBLISHED) && !status.equals(EntityState.WITHDRAWN))) {
            throw new Exception("given status not valid");
        }
        Entity e = createFixtureEntity(indexInline);
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
     * @param entity entity-JSON
     * @param expectedStatus expected resonse-Status
     */
    protected Entity createEntity(Entity entity, int expectedStatus) throws Exception {
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl).bodyString(
                                mapper.writeValueAsString(entity),
                                ContentType.APPLICATION_JSON));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        if (expectedStatus < 400) {
            // get entity
            String entityId = EntityUtils.toString(resp.getEntity());
            resp =
                    this.executeAsAdmin(
                            Request.Get(entityUrl + entityId));
            String response = EntityUtils.toString(resp.getEntity());
            assertEquals(200, resp.getStatusLine().getStatusCode());
            Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            return fetched;
        }
        return null;
    }

    /**
     * @param entity entity-JSON
     * @param expectedStatus expected resonse-Status
     */
    protected Entity updateEntity(Entity entity, int expectedStatus) throws Exception {
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Put(entityUrl + entity.getId()).bodyString(
                                mapper.writeValueAsString(entity),
                                ContentType.APPLICATION_JSON));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        if (expectedStatus < 400) {
            // get entity
            resp =
                    this.executeAsAdmin(
                            Request.Get(entityUrl + entity.getId()));
            String response = EntityUtils.toString(resp.getEntity());
            assertEquals(200, resp.getStatusLine().getStatusCode());
            Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            return fetched;
        }
        return null;
    }

    /**
     * @param entityId entityId
     * @param expectedStatus expected resonse-Status
     */
    protected Entity retrieveEntity(String entityId, int expectedStatus) throws Exception {
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entityId));
        String response = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        if (expectedStatus < 400) {
            Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            return fetched;
        } else {
            return null;
        }
    }

    /**
     * @param entityId entityId
     * @param expectedStatus expected resonse-Status
     */
    protected Entity retrieveVersion(String entityId, int version, int expectedStatus) throws Exception {
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entityId + "/version/" + version));
        String response = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        if (expectedStatus < 400) {
            Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            return fetched;
        } else {
            return null;
        }
    }

    /**
     * @param entityId entityId
     * @param state state
     * @param expectedStatus expected resonse-Status
     */
    protected void setEntityStatus(String entityId, EntityState state, int expectedStatus) throws Exception {
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Put(getStateUrl(entityId, state)));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
    }

    /**
     * @param entityId entityId
     */
    protected EntityState getEntityStatus(String entityId) throws Exception {
        Entity entity = retrieveEntity(entityId, 200);
        return entity.getState();
    }

    /**
     * Add Metadata to an Entity.
     * 
     * @param entity entity
     * @param mdName name of mdRecord to add
     * @param indexInline TODO
     * @return Entity updated entity
     */
    protected Entity addMetadataStream(Entity entity, String mdName, String mdType, boolean indexInline,
            int expectedStatus)
            throws Exception {
        Metadata metadata = createRandomDCMetadata(indexInline);
        if (mdName == null || !mdName.equals(IGNORE)) {
            metadata.setName(mdName);
        }
        if (mdType == null || !mdType.equals(IGNORE)) {
            metadata.setType(mdType);
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
                assertTrue(fetched.hasMetadata(metadata.getName()));
                assertEquals(metadata.getType(), fetched.getMetadata(metadata.getName()).getType());
                assertEquals(retrieveMetadataContent(entity.getId(), metadata, 200),
                        retrieveMetadataContent(fetched.getId(), fetched.getMetadata(metadata.getName()), 200));
            } else {
                assertFalse(fetched.hasMetadata(metadata.getName()));
            }
        }
        return fetched;
    }

    /**
     * Add Metadata to an Entity.
     * 
     * @param entity entity
     * @param mdName name of mdRecord to add
     * @param indexInline TODO
     * @return Entity updated entity
     */
    protected
            Entity
            addMetadataMultipart(Entity entity, String mdName, String mdType, boolean indexInline, int expectedStatus)
                    throws Exception {
        Metadata metadata = createRandomDCMetadata(indexInline);
        if (mdName == null || !mdName.equals(IGNORE)) {
            metadata.setName(mdName);
        }
        if (mdType == null || !mdType.equals(IGNORE)) {
            metadata.setType(mdType);
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(((ByteArraySource) metadata.getSource()).getBytes());
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl + entity.getId() + "/metadata").body(MultipartEntityBuilder.create()
                                .addTextBody("name", metadata.getName())
                                .addTextBody("type", metadata.getType())
                                .addTextBody("indexInline", new Boolean(indexInline).toString())
                                .addBinaryBody(
                                        "data",
                                        bais, ContentType.APPLICATION_XML, metadata.getFilename())
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
                assertTrue(fetched.hasMetadata(metadata.getName()));
                assertEquals(metadata.getType(), fetched.getMetadata(metadata.getName()).getType());
                assertEquals(retrieveMetadataContent(entity.getId(), metadata, 200),
                        retrieveMetadataContent(fetched.getId(), fetched.getMetadata(metadata.getName()), 200));
            } else {
                assertFalse(fetched.hasMetadata(metadata.getName()));
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
            tmdName = entity.getMetadata().iterator().next().getName();
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
                assertFalse(fetched.hasMetadata(tmdName));
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
     * @param indexInline TODO
     * @return Entity updated entity
     */
    protected Entity addBinaryMetadataStream(Entity entity, String binaryName, String mdName, String mdType,
            boolean indexInline, int expectedStatus) throws Exception {
        String bName = binaryName;
        if (bName != null && bName.equals(IGNORE)) {
            bName = entity.getBinaries().iterator().next().getName();
        }
        Metadata metadata = createRandomDCMetadata(indexInline);
        if (mdName == null || !mdName.equals(IGNORE)) {
            metadata.setName(mdName);
        }
        if (mdType == null || !mdType.equals(IGNORE)) {
            metadata.setType(mdType);
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
                assertTrue(fetched.getBinary(bName).hasMetadata(metadata.getName()));
                assertEquals(metadata.getType(), fetched.getBinary(bName).getMetadata(
                        metadata.getName()).getType());
                assertEquals(retrieveBinaryMetadataContent(entity.getId(), bName, metadata, 200),
                        retrieveBinaryMetadataContent(fetched.getId(), bName, fetched.getBinary(bName)
                                .getMetadata(metadata.getName()), 200));
            } else {
                if (fetched.hasBinary(bName)) {
                    assertFalse(fetched.getBinary(bName).hasMetadata(metadata.getName()));
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
     * @param indexInline TODO
     * @return Entity updated entity
     */
    protected Entity addBinaryMetadataMultipart(Entity entity, String binaryName, String mdName, String mdType,
            boolean indexInline, int expectedStatus) throws Exception {
        String bName = binaryName;
        if (bName != null && bName.equals(IGNORE)) {
            bName = entity.getBinaries().iterator().next().getName();
        }
        Metadata metadata = createRandomDCMetadata(false);
        if (mdName == null || !mdName.equals(IGNORE)) {
            metadata.setName(mdName);
        }
        if (mdType == null || !mdType.equals(IGNORE)) {
            metadata.setType(mdType);
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(((ByteArraySource) metadata.getSource()).getBytes());
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(
                                entityUrl + entity.getId() + "/binary/" +
                                        bName + "/metadata").body(
                                MultipartEntityBuilder.create()
                                        .addTextBody("name", metadata.getName())
                                        .addTextBody("type", metadata.getType())
                                        .addTextBody("indexInline", new Boolean(indexInline).toString())
                                        .addBinaryBody(
                                                "data",
                                                bais, ContentType.APPLICATION_XML, metadata.getFilename())
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
                assertTrue(fetched.getBinary(bName).hasMetadata(metadata.getName()));
                assertEquals(metadata.getType(), fetched.getBinary(bName).getMetadata(
                        metadata.getName()).getType());
                assertEquals(retrieveBinaryMetadataContent(entity.getId(), bName, metadata, 200),
                        retrieveBinaryMetadataContent(fetched.getId(), bName, fetched.getBinary(bName)
                                .getMetadata(metadata.getName()), 200));
            } else {
                if (fetched.hasBinary(bName)) {
                    assertFalse(fetched.getBinary(bName).hasMetadata(metadata.getName()));
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
            binary = entity.getBinaries().iterator().next();
        } else {
            binary = entity.getBinary(bName);
        }
        bName = binary.getName();
        if (mName != null && mName.equals(IGNORE)) {
            mName = binary.getMetadata().iterator().next().getName();
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
                assertFalse(fetched.getBinary(bName).hasMetadata(mName));
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
                binary.setSource(new ByteArraySource(IOUtils.toByteArray(Fixtures.class.getClassLoader().getResource(resource).openStream())));
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
                assertTrue(fetched.hasBinary(binary.getName()));
                assertEquals(binary.getMimetype(), fetched.getBinary(binary.getName()).getMimetype());
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
                assertTrue(fetched.hasBinary(binary.getName()));
                assertEquals(binary.getMimetype(), fetched.getBinary(binary.getName()).getMimetype());
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
            bName = entity.getBinaries().iterator().next().getName();
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
                assertFalse(fetched.hasBinary(bName));
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
                assertTrue(fetched.hasRelation(tpredicate));
                assertTrue(fetched.getRelation(tpredicate).getObjects().contains(tobject));
            } else {
                assertFalse(fetched.hasRelation(tpredicate));
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

    public ZipInputStream retrieveContent(String id, int version, int expectedStatus) throws IOException {
        HttpResponse resp = this.executeAsAdmin(Request.Get(hostUrl + "/archive/" + id + "/" + version + "/content"));
        assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
        return new ZipInputStream(resp.getEntity().getContent());
    }

    private String retrieveMetadataContent(String entityId, Metadata md, int expectedStatus) throws IOException {
        if (md.getSource().isInternal()) {
            HttpResponse resp =
                    this.executeAsAdmin(Request.Get(entityUrl + entityId + "/metadata/" + md.getName() + "/content"));
            assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
            return IOUtils.toString(resp.getEntity().getContent(), "UTF-8");
        } else {
            return IOUtils.toString(md.getSource()
                    .getInputStream(), "UTF-8");
        }
    }

    private String retrieveBinaryMetadataContent(String entityId, String binaryName, Metadata md, int expectedStatus)
            throws IOException {
        if (md.getSource().isInternal()) {
            HttpResponse resp =
                    this.executeAsAdmin(Request.Get(entityUrl + entityId + "/binary/" + binaryName + "/metadata/" +
                            md.getName() + "/content"));
            assertEquals(expectedStatus, resp.getStatusLine().getStatusCode());
            return IOUtils.toString(resp.getEntity().getContent(), "UTF-8");
        } else {
            return IOUtils.toString(md.getSource()
                    .getInputStream(), "UTF-8");
        }
    }

    private String getStateUrl(String entityId, EntityState state) {
        if (EntityState.PENDING.equals(state)) {
            return entityUrl + entityId + "/pending";
        } else if (EntityState.SUBMITTED.equals(state)) {
            return entityUrl + entityId + "/submit";
        } else if (EntityState.PUBLISHED.equals(state)) {
            return entityUrl + entityId + "/publish";
        } else if (EntityState.WITHDRAWN.equals(state)) {
            return entityUrl + entityId + "/withdraw";
        } else {
            return null;
        }
    }

    protected void checkMetadata(Entity e) throws IOException {
        XMLSerializer serializer = new XMLSerializer();
        serializer.setRemoveNamespacePrefixFromElements(true);
        serializer.setForceTopLevelObject(true);
        serializer.setSkipNamespaces(true);
        if (e.getMetadata() != null) {
            for (Metadata metadata : e.getMetadata()) {
                String content = retrieveMetadataContent(e.getId(), metadata, 200);
                assertNotNull(content);
                if (metadata.getFilename() != null) {
                    String content1 = readFromUrl(Fixtures.class.getClassLoader().getResource("fixtures/" + metadata.getFilename()));
                    assertNotNull(content1);
                    assertEquals(content.replaceAll("\n", ""), content1.replaceAll("\n", ""));
                }
                if (metadata.isIndexInline()) {
                    assertNotNull(metadata.getJsonData());
                    assertEquals(serializer.read(content).toString(), metadata.getJsonData().toString());
                } else {
                    assertEquals("null", metadata.getJsonData().toString());
                }
            }
        }
        if (e.getBinaries() != null) {
            for (Binary binary : e.getBinaries()) {
                if (binary.getMetadata() != null) {
                    for (Metadata metadata : binary.getMetadata()) {
                        String content =
                                retrieveBinaryMetadataContent(e.getId(), binary.getName(), metadata, 200);
                        assertNotNull(content);
                        if (metadata.getFilename() != null) {
                            String content1 = readFromUrl(Fixtures.class.getClassLoader().getResource("fixtures/" + metadata.getFilename()));
                            assertNotNull(content1);
                            assertEquals(content.replaceAll("\n", ""), content1.replaceAll("\n", ""));
                        }
                        if (metadata.isIndexInline()) {
                            assertNotNull(metadata.getJsonData());
                            assertEquals(serializer.read(content).toString(), metadata.getJsonData().toString());
                        } else {
                            assertEquals("null", metadata.getJsonData().toString());
                        }
                    }
                }
            }
        }
    }

    private String readFromUrl(URL url) throws IOException {
        BufferedReader in = null;
        StringBuilder builder = new StringBuilder();
        try {
            in = new BufferedReader(
                    new InputStreamReader(url.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                builder.append(inputLine).append("\n");
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {}
            }
        }
        return builder.toString();
    }
}
