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

import static net.objecthunter.larch.test.util.Fixtures.AREA_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.objecthunter.larch.integration.AbstractLarchIT;
import net.objecthunter.larch.integration.helpers.AuthConfigurer;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.MissingPermission;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.ObjectType;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.RoleRestriction;
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.Entity.EntityType;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.UserRequest;
import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.model.security.role.Role.RoleRight;
import net.objecthunter.larch.model.security.role.UserRole;
import net.objecthunter.larch.model.source.UrlSource;
import net.objecthunter.larch.test.util.Fixtures;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

/**
 * Class holds methods used by auth-tests.<br>
 * <br>
 * -create workspace where different users have different rights <br>
 * -execute http-request with different auth-headers <br>
 * -execute same request with different users and check response
 * 
 * @author mih
 */
public abstract class AbstractAuthorizeLarchIT extends AbstractLarchIT {

    protected static String permissionId = null;

    /**
     * Holds users with different rights. key: Permission the user does not have, value: String[2]: user password
     */
    protected static Map<MissingPermission, String[]> usernames = new HashMap<MissingPermission, String[]>();

    protected static String userPassword = "ttestt";

    private static int methodCounter = 0;

    @Before
    public void initialize() throws Exception {
        if (methodCounter == 0) {
            preparePermission();
            methodCounter++;
        }
    }

    /**
     * Create Permission.<br>
     * Create users having different rights in the permission.<br>
     * store entityId in variable.<br>
     * 
     * @throws Exception
     */
    protected void preparePermission() throws Exception {
        // create Permission
        final Entity permission = new Entity();
        permission.setId(RandomStringUtils.randomAlphanumeric(16));
        permission.setType(EntityType.PERMISSION);
        permission.setParentId(AREA_ID);
        permission.setLabel("bar");
        HttpResponse resp = this.executeAsAdmin(Request.Post(entityUrl)
                .bodyString(this.mapper.writeValueAsString(permission), ContentType.APPLICATION_JSON));

        String test = EntityUtils.toString(resp.getEntity());
        permissionId = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());
        assertNotNull(permissionId);
        assertEquals(permission.getId(), permissionId);

        // create users
        for (MissingPermission missingPermission : MissingPermission.values()) {
            usernames.put(missingPermission, new String[] { createUser(null, userPassword), userPassword });
        }
        // create permissions for users in workspace
        for (Entry<MissingPermission, String[]> e : usernames.entrySet()) {
            if (e.getKey().equals(MissingPermission.NONE)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, null);
            } else if (e.getKey().equals(MissingPermission.READ_PENDING_BINARY)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.READ_PENDING_BINARY);
            } else if (e.getKey().equals(MissingPermission.READ_PENDING_METADATA)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.READ_PENDING_METADATA);
            } else if (e.getKey().equals(MissingPermission.READ_PUBLISHED_BINARY)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.READ_PUBLISHED_BINARY);
            } else if (e.getKey().equals(MissingPermission.READ_PUBLISHED_METADATA)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.READ_PUBLISHED_METADATA);
            } else if (e.getKey().equals(MissingPermission.READ_SUBMITTED_BINARY)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.READ_SUBMITTED_BINARY);
            } else if (e.getKey().equals(MissingPermission.READ_SUBMITTED_METADATA)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.READ_SUBMITTED_METADATA);
            } else if (e.getKey().equals(MissingPermission.READ_WITHDRAWN_BINARY)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.READ_WITHDRAWN_BINARY);
            } else if (e.getKey().equals(MissingPermission.READ_WITHDRAWN_METADATA)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.READ_WITHDRAWN_METADATA);
            } else if (e.getKey().equals(MissingPermission.READ_PERMISSION)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.READ_PERMISSION);
            } else if (e.getKey().equals(MissingPermission.WRITE_PENDING_BINARY)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.WRITE_PENDING_BINARY);
            } else if (e.getKey().equals(MissingPermission.WRITE_PENDING_METADATA)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.WRITE_PENDING_METADATA);
            } else if (e.getKey().equals(MissingPermission.WRITE_PUBLISHED_BINARY)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.WRITE_PUBLISHED_BINARY);
            } else if (e.getKey().equals(MissingPermission.WRITE_PUBLISHED_METADATA)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.WRITE_PUBLISHED_METADATA);
            } else if (e.getKey().equals(MissingPermission.WRITE_SUBMITTED_BINARY)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.WRITE_SUBMITTED_BINARY);
            } else if (e.getKey().equals(MissingPermission.WRITE_SUBMITTED_METADATA)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.WRITE_SUBMITTED_METADATA);
            } else if (e.getKey().equals(MissingPermission.WRITE_WITHDRAWN_BINARY)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.WRITE_WITHDRAWN_BINARY);
            } else if (e.getKey().equals(MissingPermission.WRITE_WITHDRAWN_METADATA)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.WRITE_WITHDRAWN_METADATA);
            } else if (e.getKey().equals(MissingPermission.WRITE_PERMISSION)) {
                createMissingPermissionRightsForUser(e.getValue()[0], permissionId, RoleRight.WRITE_PERMISSION);
            }
        }
    }

    /**
     * Execute Http-Request as user with given username/password.
     * 
     * @param method HttpMethod
     * @param url
     * @param body String-body for PUT/POST requests
     * @param username
     * @param password
     * @return HttpResponse
     * @throws IOException
     */
    protected HttpResponse executeAsUser(HttpMethod method, String url, Object body, String username,
            String password, boolean isHtml)
            throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpUriRequest request = getRequest(method, url, body, isHtml);
        if (request != null) {
            byte[] encodedBytes = Base64.encodeBase64((username + ":" + password).getBytes());
            String authorization = "Basic " + new String(encodedBytes);
            request.setHeader("Authorization", authorization);
            return httpClient.execute(request);
        }
        return null;
    }

    /**
     * Execute Http-Request with no auth-header (anonymous).
     * 
     * @param method HttpMethod
     * @param url
     * @param body String-body for PUT/POST requests
     * @return
     * @throws IOException
     */
    protected HttpResponse executeAsAnonymous(HttpMethod method, String url, Object body, boolean isHtml)
            throws IOException {
        HttpClient httpClient = HttpClientBuilder.create()
                .disableRedirectHandling().build();
        HttpUriRequest request = getRequest(method, url, body, isHtml);
        if (request != null) {
            return httpClient.execute(request);
        }
        return null;
    }

    /**
     * Create Workspace-Rights where user with provided username has all rights except for provided permission.
     * 
     * @param username
     * @param permission permission
     * @return String workspaceId
     * @throws Exception
     */
    protected void createMissingPermissionRightsForUser(String username, String permissionId, RoleRight roleRight)
            throws Exception {
        // try to retrieve user
        HttpResponse resp = this.executeAsAdmin(Request.Get(userUrl + username));
        String result = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());
        User fetched = this.mapper.readValue(resp.getEntity().getContent(), User.class);

        // Set permissions for user
        List<Role> roles = new ArrayList<Role>();
        UserRole userRole = new UserRole();
        Map<String, List<RoleRight>> rights = new HashMap<String, List<RoleRight>>();
        // permissions
        List<RoleRight> roleRights = new ArrayList<RoleRight>();
        for (RoleRight allowedRoleRight : userRole.allowedRights()) {
            if (!allowedRoleRight.equals(roleRight)) {
                roleRights.add(allowedRoleRight);
            }
        }
        rights.put(permissionId, roleRights);
        userRole.setRights(rights);
        roles.add(userRole);

        // add rights
        resp = this.executeAsAdmin(Request.Post(userUrl + username + "/roles")
                .bodyString(this.mapper.writeValueAsString(roles), ContentType.APPLICATION_JSON));
        result = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());

    }

    /**
     * Get HttpUriRequest for given parameters.
     * 
     * @param method
     * @param url
     * @param body
     * @return
     * @throws IOException
     */
    private HttpUriRequest getRequest(HttpMethod method, String url, Object body, boolean isHtml) throws IOException {
        if (method.equals(HttpMethod.POST)) {
            HttpPost httpPost =
                    new HttpPost(url);
            setBody(httpPost, body);
            setAcceptHeader(httpPost, isHtml);
            return httpPost;
        } else if (method.equals(HttpMethod.PUT)) {
            HttpPut httpPut =
                    new HttpPut(url);
            setBody(httpPut, body);
            setAcceptHeader(httpPut, isHtml);
            return httpPut;
        } else if (method.equals(HttpMethod.PATCH)) {
            HttpPatch httpPatch =
                    new HttpPatch(url);
            setBody(httpPatch, body);
            setAcceptHeader(httpPatch, isHtml);
            return httpPatch;
        } else if (method.equals(HttpMethod.GET)) {
            HttpGet httpGet = new HttpGet(url);
            setAcceptHeader(httpGet, isHtml);
            return httpGet;
        } else if (method.equals(HttpMethod.DELETE)) {
            HttpDelete httpDelete = new HttpDelete(url);
            setAcceptHeader(httpDelete, isHtml);
            return httpDelete;
        }
        return null;
    }

    /**
     * Sets the Accept- header to text/html if isHtml = true.
     * 
     * @param request
     * @param isHtml
     * @throws IOException
     */
    private void setAcceptHeader(HttpUriRequest request, boolean isHtml)
            throws IOException {
        if (isHtml) {
            request.setHeader("Accept", "text/html");
        }
    }

    /**
     * Sets the body of the request.
     * 
     * @param request
     * @param body
     * @return HttpEntityEnclosingRequestBase
     * @throws IOException
     */
    private void setBody(HttpEntityEnclosingRequestBase request, Object body)
            throws IOException {
        if (body != null) {
            if (body instanceof String) {
                String bodyString = (String) body;
                if (StringUtils.isNotBlank(bodyString)) {
                    request.setEntity(new StringEntity(bodyString));
                    if (isJson(bodyString)) {
                        request.setHeader("Content-type", "application/json; charset=UTF-8");
                    } else {
                        request.setHeader("Content-type", ContentType.APPLICATION_FORM_URLENCODED.toString());
                    }
                }
            } else if (body instanceof HttpEntity) {
                request.setEntity((HttpEntity) body);
            }
        }
    }

    /**
     * Tests calling the given url with different user-permissions.<br>
     * neededPermission indicates the permission that is needed to be allowed to call the request.<br>
     * If neededPermission is null, everybody may call the url. If adminOnly is true, only admin may call the url,
     * independent of workspace-rights.
     * 
     * @param method
     * @param url
     * @param object body
     * @param neededPermission
     * @param resetState to with state the entity should get reset after url was called
     * @param resetStateEntityId id of the entity to reset the state
     * @param adminOnly
     * @throws Exception
     */
    protected void testAuth(AuthConfigurer authConfigurer)
            throws Exception {
        // get entity for reset
        Object resetObject = getResetObject(authConfigurer);

        // try as admin
        String url = manipulateUrl(authConfigurer.getUrl(), resetObject);
        HttpResponse resp =
                this.executeAsUser(authConfigurer.getMethod(), url, authConfigurer.getBody(),
                        adminUsername, adminPassword, authConfigurer.isHtml());
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);
        resetState(authConfigurer.isResetState(), resetObject);
        // try as user with all rights
        url = manipulateUrl(authConfigurer.getUrl(), resetObject);
        resp =
                this.executeAsUser(authConfigurer.getMethod(), url, authConfigurer.getBody(),
                        usernames.get(MissingPermission.NONE)[0], usernames
                                .get(MissingPermission.NONE)[1], authConfigurer.isHtml());
        response = EntityUtils.toString(resp.getEntity());
        if (authConfigurer.getRoleRestriction() != null &&
                authConfigurer.getRoleRestriction().equals(RoleRestriction.ADMIN)) {
            assertEquals(403, resp.getStatusLine().getStatusCode());
        } else {
            assertTrue(resp.getStatusLine().getStatusCode() < 400);
        }
        resetState(authConfigurer.isResetState(), resetObject);
        // try as user with no rights
        url = manipulateUrl(authConfigurer.getUrl(), resetObject);
        resp =
                this.executeAsUser(authConfigurer.getMethod(), url, authConfigurer.getBody(),
                        usernames.get(MissingPermission.ALL)[0], usernames
                                .get(MissingPermission.ALL)[1], authConfigurer.isHtml());
        response = EntityUtils.toString(resp.getEntity());
        if (authConfigurer.getRoleRestriction() != null &&
                authConfigurer.getRoleRestriction().equals(RoleRestriction.ADMIN)) {
            assertEquals(403, resp.getStatusLine().getStatusCode());
        } else if ((authConfigurer.getRoleRestriction() == null || !authConfigurer.getRoleRestriction().equals(
                RoleRestriction.ADMIN)) &&
                authConfigurer.getNeededPermission() == null) {
            assertTrue(resp.getStatusLine().getStatusCode() < 400);
        } else {
            assertEquals(403, resp.getStatusLine().getStatusCode());
        }
        resetState(authConfigurer.isResetState(), resetObject);
        // try as anonymous user
        url = manipulateUrl(authConfigurer.getUrl(), resetObject);
        resp =
                this.executeAsAnonymous(authConfigurer.getMethod(), url,
                        authConfigurer.getBody(), authConfigurer.isHtml());
        response = EntityUtils.toString(resp.getEntity());
        if (authConfigurer.getNeededPermission() == null && authConfigurer.getRoleRestriction() == null) {
            assertTrue(resp.getStatusLine().getStatusCode() < 400);
        } else {
            if (!authConfigurer.isHtml()) {
                assertEquals(401, resp.getStatusLine().getStatusCode());
            } else {
                assertEquals(302, resp.getStatusLine().getStatusCode());
                Header[] headers = resp.getHeaders("location");
                assertNotNull(headers);
                assertTrue(headers.length > 0);
                assertEquals(true, headers[0].getValue().matches(".*login-page.*"));
            }
        }
        resetState(authConfigurer.isResetState(), resetObject);
        // try as user with wrong workspace rights
        String[] userparams = null;
        if (authConfigurer.getNeededPermission() != null) {
            userparams = usernames.get(authConfigurer.getNeededPermission());
        } else {
            userparams = usernames.get(MissingPermission.ALL);
        }
        url = manipulateUrl(authConfigurer.getUrl(), resetObject);
        resp =
                this.executeAsUser(authConfigurer.getMethod(), url, authConfigurer.getBody(),
                        userparams[0], userparams[1], authConfigurer.isHtml());
        response = EntityUtils.toString(resp.getEntity());
        if ((authConfigurer.getRoleRestriction() == null ||
                !authConfigurer.getRoleRestriction().equals(RoleRestriction.ADMIN)) &&
                authConfigurer.getNeededPermission() == null) {
            assertTrue(resp.getStatusLine().getStatusCode() < 400);
        } else {
            assertEquals(403, resp.getStatusLine().getStatusCode());
        }
        resetState(authConfigurer.isResetState(), resetObject);
        // try as user with correct workspace rights
        // for (Entry<MissingPermission, String[]> entry : usernames.entrySet()) {
        // if (authConfigurer.getNeededPermission() == null || (!entry.getKey().equals(MissingPermission.ALL) &&
        // !entry.getKey().equals(authConfigurer.getNeededPermission()))) {
        // url = manipulateUrl(authConfigurer.getUrl(), resetObject);
        // resp =
        // this.executeAsUser(authConfigurer.getMethod(), url, authConfigurer
        // .getBody(), entry.getValue()[0], entry.getValue()[1], authConfigurer.isHtml());
        // response = EntityUtils.toString(resp.getEntity());
        // if (authConfigurer.getRoleRestriction() != null &&
        // authConfigurer.getRoleRestriction().equals(RoleRestriction.ADMIN)) {
        // assertEquals(403, resp.getStatusLine().getStatusCode());
        // } else {
        // assertTrue(resp.getStatusLine().getStatusCode() < 400);
        // }
        // resetState(authConfigurer.isResetState(), resetObject);
        // }
        // }
    }

    private Object getResetObject(AuthConfigurer authConfigurer) throws Exception {
        Object resetObject = null;
        if (authConfigurer.isResetState()) {
            String url = null;
            if (authConfigurer.getResetStateObjectType().equals(ObjectType.ENTITY)) {
                url = entityUrl +
                        authConfigurer.getResetStateId();
            } else if (authConfigurer.getResetStateObjectType().equals(ObjectType.USER)) {
                url = hostUrl + "user/" + authConfigurer.getResetStateId();
            } else if (authConfigurer.getResetStateObjectType().equals(ObjectType.USER_REQUEST)) {
                if (StringUtils.isNotBlank(authConfigurer.getResetStateId())) {
                    UserRequest userRequest = new UserRequest();
                    User user = new User();
                    userRequest.setToken(authConfigurer.getResetStateId().replaceFirst(".*?\\|", ""));
                    user.setName(authConfigurer.getResetStateId().replaceFirst("(.*?)\\|.*", "$1"));
                    userRequest.setUser(user);
                    return userRequest;
                }
            } else {
                throw new IOException("objectType not correct");
            }
            HttpResponse resp = this.executeAsAdmin(Request.Get(url));
            assertEquals(200, resp.getStatusLine().getStatusCode());
            if (authConfigurer.getResetStateObjectType().equals(ObjectType.ENTITY)) {
                resetObject = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            } else if (authConfigurer.getResetStateObjectType().equals(ObjectType.USER)) {
                resetObject = mapper.readValue(resp.getEntity().getContent(), User.class);
            }
        }
        return resetObject;
    }

    /**
     * Resets the state of an entity.
     * 
     * @param resetState
     * @param resetStateEntityId
     * @throws Exception
     */
    private void resetState(boolean resetState, Object resetObject) throws Exception {
        if (resetState && resetObject != null) {
            if (resetObject instanceof Entity) {
                resetEntity((Entity) resetObject);
            } else if (resetObject instanceof User) {
                resetUser((User) resetObject);
            } else if (resetObject instanceof UserRequest) {
                resetUserRequest((UserRequest) resetObject);
            }
        }
    }

    private void resetEntity(Entity resetEntity) throws Exception {
        // check if entity is there
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + resetEntity.getId()));
        String response = EntityUtils.toString(resp.getEntity());
        if (resetEntity.getBinaries() != null) {
            for (Entry<String, Binary> binary : resetEntity.getBinaries().entrySet()) {
                binary.getValue()
                        .setSource(
                                new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png")
                                        .toURI()));
            }
        }
        if (resp.getStatusLine().getStatusCode() == HttpStatus.NOT_FOUND.value()) {
            // recreate entity
            resp =
                    this.executeAsAdmin(
                            Request.Post(entityUrl).bodyString(
                                    mapper.writeValueAsString(resetEntity),
                                    ContentType.APPLICATION_JSON));
            response = EntityUtils.toString(resp.getEntity());
            assertEquals(201, resp.getStatusLine().getStatusCode());
        } else {
            assertEquals(200, resp.getStatusLine().getStatusCode());
            // update
            resp =
                    this.executeAsAdmin(
                            Request.Put(entityUrl + resetEntity.getId())
                                    .bodyString(
                                            mapper.writeValueAsString(resetEntity),
                                            ContentType.APPLICATION_JSON));
            response = EntityUtils.toString(resp.getEntity());
            assertEquals(200, resp.getStatusLine().getStatusCode());
        }
        String urlSuffix = null;
        if (resetEntity.getState().equals(EntityState.SUBMITTED)) {
            urlSuffix = "submit";
        } else if (resetEntity.getState().equals(EntityState.PUBLISHED)) {
            urlSuffix = "publish";
        } else {
            return;
        }
        resp =
                this.executeAsAdmin(
                        Request.Put(entityUrl + resetEntity.getId() + "/" +
                                urlSuffix));
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(200, resp.getStatusLine().getStatusCode());
    }

    private void resetUser(User resetUser) throws Exception {
        // check if user is there
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Get(hostUrl + "user/" + resetUser.getName()));
        String response = EntityUtils.toString(resp.getEntity());
        if (resp.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            // delete user
            resp =
                    this.executeAsAdmin(
                            Request.Delete(hostUrl + "user/" + resetUser.getName()));
            response = EntityUtils.toString(resp.getEntity());
            assertEquals(200, resp.getStatusLine().getStatusCode());
        }
        // recreate user
        createUser(resetUser.getName(), userPassword);
    }

    private void resetUserRequest(UserRequest resetUserRequest) throws Exception {
        // check if user-request exists
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Get(hostUrl + "confirm/" + resetUserRequest.getToken()));
        String response = EntityUtils.toString(resp.getEntity());
        if (resp.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            // delete user-request
            resp =
                    executeAsAdmin(
                    Request.Post(confirmUrl + resetUserRequest.getToken())
                            .body(MultipartEntityBuilder.create()
                                    .addTextBody("password", userPassword)
                                    .addTextBody("passwordRepeat", userPassword)
                                    .build()
                            ));
            assertEquals(200, resp.getStatusLine().getStatusCode());
        }
        resp =
                executeAsAdmin(
                Request.Get(hostUrl + "user/" + resetUserRequest.getUser().getName()));
        if (resp.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            // delete user
            resp =
                    executeAsAdmin(
                    Request.Delete(hostUrl + "user/" + resetUserRequest.getUser().getName()));
            assertEquals(200, resp.getStatusLine().getStatusCode());
        }
        // recreate user request
        UserRequest newRequest = createUserRequest(resetUserRequest.getUser().getName(), userPassword);
        resetUserRequest.setToken(newRequest.getToken());
    }

    private String manipulateUrl(String url, Object resetObject) {
        if (url.contains("{token}") && resetObject != null && resetObject instanceof UserRequest) {
            return url.replaceAll("\\{token\\}", ((UserRequest) resetObject).getToken());
        }
        return url;
    }

    private boolean isJson(String text) {
        boolean isJson = false;
        JsonFactory f = new JsonFactory();
        try {
            JsonParser parser = f.createParser(text);
            while (parser.nextToken() != null) {
            }
            isJson = true;
        } catch (Exception e) {
        }
        return isJson;
    }

}
