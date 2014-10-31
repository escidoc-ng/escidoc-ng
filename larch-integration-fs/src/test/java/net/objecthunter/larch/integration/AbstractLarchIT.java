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
import static net.objecthunter.larch.test.util.Fixtures.createFixtureEntity;
import static net.objecthunter.larch.test.util.Fixtures.createRandomDCMetadata;
import static net.objecthunter.larch.test.util.Fixtures.createRandomBinary;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

    protected String createContentModel() throws IOException {
        ContentModel contentModel = Fixtures.createContentModel();
        HttpResponse resp = this.executeAsAdmin(Request.Post(contentModelUrl)
                .bodyString(this.mapper.writeValueAsString(contentModel), ContentType.APPLICATION_JSON));
        String test = EntityUtils.toString(resp.getEntity());
        String contentModelId = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());
        assertNotNull(contentModelId);
        return contentModelId;
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
    protected Entity addMetadata(Entity entity, String mdName) throws Exception {
        Metadata metadata = createRandomDCMetadata();
        if (StringUtils.isNotBlank(mdName)) {
            metadata.setName(mdName);
        }
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl + entity.getId() + "/metadata").bodyString(
                                mapper.writeValueAsString(metadata),
                                ContentType.APPLICATION_JSON));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        return fetched;
    }

    /**
     * Remove Metadata from an Entity.
     * 
     * @param entity entity
     * @param mdName name of mdRecord to remove
     * @return Entity updated entity
     */
    protected Entity removeMetadata(Entity entity, String mdName) throws Exception {
        String tmdName = mdName;
        if (StringUtils.isBlank(tmdName)) {
            tmdName = entity.getMetadata().keySet().iterator().next();
        }
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Delete(entityUrl + entity.getId() + "/metadata/" + tmdName));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
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
    protected Entity addBinaryMetadata(Entity entity, String binaryName, String mdName) throws Exception {
        String bName = binaryName;
        if (StringUtils.isBlank(bName)) {
            bName = entity.getBinaries().keySet().iterator().next();
        }
        Metadata metadata = createRandomDCMetadata();
        if (StringUtils.isNotBlank(mdName)) {
            metadata.setName(mdName);
        }
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(
                                entityUrl + entity.getId() + "/binary/" +
                                        bName + "/metadata").bodyString(
                                mapper.writeValueAsString(metadata),
                                ContentType.APPLICATION_JSON));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
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
    protected Entity removeBinaryMetadata(Entity entity, String binaryName, String mdName) throws Exception {
        String bName = binaryName;
        String mName = mdName;
        Binary binary = null;
        if (StringUtils.isBlank(bName)) {
            binary = entity.getBinaries().values().iterator().next();
        } else {
            binary = entity.getBinaries().get(bName);
        }
        bName = binary.getName();
        if (StringUtils.isBlank(mName)) {
            mName = binary.getMetadata().keySet().iterator().next();
        }
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Delete(
                                entityUrl + entity.getId() + "/binary/" +
                                        bName + "/metadata/" + mName));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        return fetched;
    }

    /**
     * Add Binary to an Entity.
     * 
     * @param entity entity
     * @param binaryName name of the binary to add
     * @return Entity updated entity
     */
    protected Entity addBinary(Entity entity, String binaryName) throws Exception {
        Binary binary = createRandomBinary();
        if (StringUtils.isNotBlank(binaryName)) {
            binary.setName(binaryName);
        }
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(
                                entityUrl + entity.getId() + "/binary").bodyString(
                                mapper.writeValueAsString(binary),
                                ContentType.APPLICATION_JSON));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        return fetched;
    }

    /**
     * Remove Binary from an Entity.
     * 
     * @param entity entity
     * @param binaryName name of the binary to remove
     * @return Entity updated entity
     */
    protected Entity removeBinary(Entity entity, String binaryName) throws Exception {
        String bName = binaryName;
        if (StringUtils.isBlank(bName)) {
            bName = entity.getBinaries().keySet().iterator().next();
        }
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Delete(
                                entityUrl + entity.getId() + "/binary/" + bName));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
        return fetched;
    }

    /**
     * Add an Identifier to an Entity.
     * 
     * @param entity entity
     * @param type type
     * @param value value
     * @return Entity updated entity
     */
    protected Entity addIdentifier(Entity entity, String type, String value) throws Exception {
        String ttype = AlternativeIdentifier.IdentifierType.DOI.name();
        String tvalue = "identifier-" + RandomStringUtils.randomAlphabetic(16);
        if (StringUtils.isNotBlank(type)) {
            ttype = type;
        }
        if (StringUtils.isNotBlank(value)) {
            tvalue = value;
        }
        NameValuePair typePair = new BasicNameValuePair("type", ttype);
        NameValuePair valuePair = new BasicNameValuePair("value", tvalue);
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(
                                entityUrl + entity.getId() + "/identifier").bodyForm(typePair, valuePair));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
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
    protected Entity removeIdentifier(Entity entity, String type, String value) throws Exception {
        String ttype = type;
        String tvalue = value;
        if (StringUtils.isBlank(ttype)) {
            ttype = entity.getAlternativeIdentifiers().get(0).getType();
        }
        if (StringUtils.isBlank(tvalue)) {
            tvalue = entity.getAlternativeIdentifiers().get(0).getValue();
        }
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Delete(
                                entityUrl + entity.getId() + "/identifier/" + ttype + "/" + tvalue));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
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
    protected Entity addRelation(Entity entity, String predicate, String object) throws Exception {
        String tpredicate = "predicate-" + RandomStringUtils.randomAlphabetic(16);
        String tobject = "object-" + RandomStringUtils.randomAlphabetic(16);
        if (StringUtils.isNotBlank(predicate)) {
            tpredicate = predicate;
        }
        if (StringUtils.isNotBlank(object)) {
            tobject = object;
        }
        NameValuePair predicatePair = new BasicNameValuePair("predicate", tpredicate);
        NameValuePair objectPair = new BasicNameValuePair("object", tobject);
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(
                                entityUrl + entity.getId() + "/relation").bodyForm(predicatePair, objectPair));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());
        // get entity
        resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId()));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);
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
