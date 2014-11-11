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


package net.objecthunter.larch.integration.authorize;

import static net.objecthunter.larch.test.util.Fixtures.LEVEL1_ID;
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
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.UserRequest;
import net.objecthunter.larch.model.security.role.Level1AdminRole;
import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.model.security.role.Role.RoleRight;
import net.objecthunter.larch.model.security.role.UserAdminRole;
import net.objecthunter.larch.model.security.role.UserRole;
import net.objecthunter.larch.model.source.UrlSource;
import net.objecthunter.larch.test.util.Fixtures;

import org.apache.commons.codec.binary.Base64;
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
 * -create level2 where different users have different rights <br>
 * -execute http-request with different auth-headers <br>
 * -execute same request with different users and check response
 * 
 * @author mih
 */
public abstract class AbstractAuthorizeLarchIT extends AbstractLarchIT {

    protected static String level2Id = null;

    protected static String level1Id1 = null;

    protected static String level1Id2 = null;

    protected static String unusedUserId = null;

    protected static String level2Id1 = null;

    /**
     * Holds users with different rights. key: Permission the user does not have, value: String[2]: user password
     */
    protected static Map<MissingPermission, String[]> userRoleUsernames = new HashMap<MissingPermission, String[]>();

    protected static Map<String, String[]> level1AdminRoleUsernames = new HashMap<String, String[]>();

    protected static Map<String, String[]> userAdminRoleUsernames = new HashMap<String, String[]>();

    protected static String userPassword = "ttestt";

    private static int methodCounter = 0;

    @Before
    public void initialize() throws Exception {
        if (methodCounter == 0) {
            prepareLevel2();
            methodCounter++;
        }
    }

    /**
     * Create Level2.<br>
     * Create users having different rights in the level2.<br>
     * store entityId in variable.<br>
     * 
     * @throws Exception
     */
    protected void prepareLevel2() throws Exception {
        // create level1s
        level1Id1 = createLevel1();
        level1Id2 = createLevel1();

        // create level2s
        level2Id = createLevel2(LEVEL1_ID);
        level2Id1 = createLevel2(level1Id1);

        // create users with User-Role
        for (MissingPermission missingPermission : MissingPermission.values()) {
            userRoleUsernames.put(missingPermission, new String[] { createUser(null, userPassword), userPassword });
        }
        unusedUserId = createUser(null, userPassword);

        // create permissions for users in level2
        for (Entry<MissingPermission, String[]> e : userRoleUsernames.entrySet()) {
            if (e.getKey().equals(MissingPermission.NONE)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, null);
            } else if (e.getKey().equals(MissingPermission.READ_PENDING_BINARY)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.READ_PENDING_BINARY);
            } else if (e.getKey().equals(MissingPermission.READ_PENDING_METADATA)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.READ_PENDING_METADATA);
            } else if (e.getKey().equals(MissingPermission.READ_PUBLISHED_BINARY)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.READ_PUBLISHED_BINARY);
            } else if (e.getKey().equals(MissingPermission.READ_PUBLISHED_METADATA)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.READ_PUBLISHED_METADATA);
            } else if (e.getKey().equals(MissingPermission.READ_SUBMITTED_BINARY)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.READ_SUBMITTED_BINARY);
            } else if (e.getKey().equals(MissingPermission.READ_SUBMITTED_METADATA)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.READ_SUBMITTED_METADATA);
            } else if (e.getKey().equals(MissingPermission.READ_WITHDRAWN_BINARY)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.READ_WITHDRAWN_BINARY);
            } else if (e.getKey().equals(MissingPermission.READ_WITHDRAWN_METADATA)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.READ_WITHDRAWN_METADATA);
            } else if (e.getKey().equals(MissingPermission.READ_PERMISSION)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.READ_LEVEL2);
            } else if (e.getKey().equals(MissingPermission.WRITE_PENDING_BINARY)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.WRITE_PENDING_BINARY);
            } else if (e.getKey().equals(MissingPermission.WRITE_PENDING_METADATA)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.WRITE_PENDING_METADATA);
            } else if (e.getKey().equals(MissingPermission.WRITE_PUBLISHED_BINARY)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.WRITE_PUBLISHED_BINARY);
            } else if (e.getKey().equals(MissingPermission.WRITE_PUBLISHED_METADATA)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.WRITE_PUBLISHED_METADATA);
            } else if (e.getKey().equals(MissingPermission.WRITE_SUBMITTED_BINARY)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.WRITE_SUBMITTED_BINARY);
            } else if (e.getKey().equals(MissingPermission.WRITE_SUBMITTED_METADATA)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.WRITE_SUBMITTED_METADATA);
            } else if (e.getKey().equals(MissingPermission.WRITE_WITHDRAWN_BINARY)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.WRITE_WITHDRAWN_BINARY);
            } else if (e.getKey().equals(MissingPermission.WRITE_WITHDRAWN_METADATA)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.WRITE_WITHDRAWN_METADATA);
            } else if (e.getKey().equals(MissingPermission.WRITE_PERMISSION)) {
                createMissingPermissionRightsForUser(e.getValue()[0], level2Id, RoleRight.WRITE_LEVEL2);
            }
        }

        // create users with level1Admin + userAdmin Roles
        level1AdminRoleUsernames.put("ROLE_LEVEL1_ADMIN" + LEVEL1_ID, new String[] { createUser(null, userPassword),
            userPassword });
        level1AdminRoleUsernames.put("ROLE_LEVEL1_ADMIN" + level1Id1, new String[] { createUser(null, userPassword),
            userPassword });
        userAdminRoleUsernames.put("ROLE_USER_ADMIN" + userRoleUsernames.get(MissingPermission.READ_PENDING_BINARY),
                new String[] { createUser(null, userPassword), userPassword });
        userAdminRoleUsernames.put("ROLE_USER_ADMIN", new String[] { createUser(null, userPassword), userPassword });
        createRoleForUser(level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + LEVEL1_ID)[0], new Level1AdminRole(),
                LEVEL1_ID);
        createRoleForUser(level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + LEVEL1_ID)[0], new UserRole(), level2Id1);
        createRoleForUser(level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + LEVEL1_ID)[0], new UserAdminRole(),
                unusedUserId);

        createRoleForUser(level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], new Level1AdminRole(),
                level1Id1);
        createRoleForUser(level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], new UserRole(), level2Id1);
        createRoleForUser(level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], new UserAdminRole(),
                unusedUserId);

        createRoleForUser(userAdminRoleUsernames.get("ROLE_USER_ADMIN" +
                userRoleUsernames.get(MissingPermission.READ_PENDING_BINARY))[0], new UserAdminRole(),
                userRoleUsernames.get(MissingPermission.WRITE_PENDING_BINARY)[0]);
        createRoleForUser(userAdminRoleUsernames.get("ROLE_USER_ADMIN" +
                userRoleUsernames.get(MissingPermission.READ_PENDING_BINARY))[0], new Level1AdminRole(), level1Id2);
        createRoleForUser(userAdminRoleUsernames.get("ROLE_USER_ADMIN" +
                userRoleUsernames.get(MissingPermission.READ_PENDING_BINARY))[0], new UserRole(), level2Id1);

        createRoleForUser(userAdminRoleUsernames.get("ROLE_USER_ADMIN")[0], new UserAdminRole(), "");
        createRoleForUser(userAdminRoleUsernames.get("ROLE_USER_ADMIN")[0], new Level1AdminRole(), level1Id2);
        createRoleForUser(userAdminRoleUsernames.get("ROLE_USER_ADMIN")[0], new UserRole(), level2Id1);

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
        HttpUriRequest authrequest =
                getRequest(method, url.replaceFirst(hostUrl, hostUrl + "authorize/"), body, isHtml);
        HttpUriRequest request = getRequest(method, url, body, isHtml);
        if (request != null && authrequest != null) {
            byte[] encodedBytes = Base64.encodeBase64((username + ":" + password).getBytes());
            String authorization = "Basic " + new String(encodedBytes);
            authrequest.setHeader("Authorization", authorization);
            request.setHeader("Authorization", authorization);
            HttpResponse authresp = httpClient.execute(authrequest);
            HttpResponse resp = httpClient.execute(request);
            int respstatus = resp.getStatusLine().getStatusCode();
            int authrespstatus = authresp.getStatusLine().getStatusCode();
            // if (respstatus > 400 && authrespstatus > 400 && respstatus != authrespstatus) {
//             String response = EntityUtils.toString(resp.getEntity());
            // String authresponse = EntityUtils.toString(authresp.getEntity());
            // System.out.println("");
            // }
            assertStatusEquals(resp, authresp);
            return resp;
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
        HttpUriRequest authrequest =
                getRequest(method, url.replaceFirst(hostUrl, hostUrl + "authorize/"), body, isHtml);
        if (request != null && authrequest != null) {
            HttpResponse resp = httpClient.execute(request);
            HttpResponse authresp = httpClient.execute(authrequest);
            // String response = EntityUtils.toString(resp.getEntity());
            // String authresponse = EntityUtils.toString(authresp.getEntity());
            assertStatusEquals(resp, authresp);
            return resp;
        }
        return null;
    }

    /**
     * Create Level2-Rights where user with provided username has all rights except for provided permission.
     * 
     * @param username
     * @param level2Id level2Id
     * @throws Exception
     */
    protected void createMissingPermissionRightsForUser(String username, String level2Id, RoleRight roleRight)
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
        rights.put(level2Id, roleRights);
        userRole.setRights(rights);
        roles.add(userRole);

        // set other roles
        // user-admin
        UserAdminRole userAdminRole = new UserAdminRole();
        Map<String, List<RoleRight>> userAdminRights = new HashMap<String, List<RoleRight>>();
        List<RoleRight> userAdminRoleRights = new ArrayList<RoleRight>();
        for (RoleRight userAdminRoleRight : userAdminRole.allowedRights()) {
            userAdminRoleRights.add(userAdminRoleRight);
        }
        userAdminRights.put(unusedUserId, userAdminRoleRights);
        userAdminRole.setRights(userAdminRights);
        roles.add(userAdminRole);
        // level1-admin
        Level1AdminRole level1AdminRole = new Level1AdminRole();
        Map<String, List<RoleRight>> level1AdminRights = new HashMap<String, List<RoleRight>>();
        List<RoleRight> level1AdminRoleRights = new ArrayList<RoleRight>();
        for (RoleRight level1AdminRoleRight : level1AdminRole.allowedRights()) {
            level1AdminRoleRights.add(level1AdminRoleRight);
        }
        level1AdminRights.put(level1Id2, level1AdminRoleRights);
        level1AdminRole.setRights(level1AdminRights);
        roles.add(level1AdminRole);

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
     * independent of level2-rights.
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
    protected void testUserRoleAuth(AuthConfigurer authConfigurer)
            throws Exception {
        // get entity for reset
        Object resetObject = getResetObject(authConfigurer);

        // try as admin
        String url = manipulateUrl(authConfigurer.getUrl(), resetObject);
        HttpResponse resp =
                this.executeAsUser(authConfigurer.getMethod(), url, authConfigurer.getBody(),
                        adminUsername, adminPassword, authConfigurer.isHtml());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);
        resetObject = resetState(authConfigurer.isResetState(), resetObject);
        // try as user with all rights
        url = manipulateUrl(authConfigurer.getUrl(), resetObject);
        resp =
                this.executeAsUser(authConfigurer.getMethod(), url, authConfigurer.getBody(),
                        userRoleUsernames.get(MissingPermission.NONE)[0], userRoleUsernames
                                .get(MissingPermission.NONE)[1], authConfigurer.isHtml());
        if (authConfigurer.getRoleRestriction() != null &&
                authConfigurer.getRoleRestriction().equals(RoleRestriction.ADMIN)) {
            assertEquals(403, resp.getStatusLine().getStatusCode());
        } else {
            assertTrue(resp.getStatusLine().getStatusCode() < 400);
        }
        resetObject = resetState(authConfigurer.isResetState(), resetObject);
        // try as user with no rights
        url = manipulateUrl(authConfigurer.getUrl(), resetObject);
        resp =
                this.executeAsUser(authConfigurer.getMethod(), url, authConfigurer.getBody(),
                        userRoleUsernames.get(MissingPermission.ALL)[0], userRoleUsernames
                                .get(MissingPermission.ALL)[1], authConfigurer.isHtml());
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
        resetObject = resetState(authConfigurer.isResetState(), resetObject);
        // try as anonymous user
        url = manipulateUrl(authConfigurer.getUrl(), resetObject);
        resp =
                this.executeAsAnonymous(authConfigurer.getMethod(), url,
                        authConfigurer.getBody(), authConfigurer.isHtml());
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
        resetObject = resetState(authConfigurer.isResetState(), resetObject);
        // try as user with wrong level2 rights
        String[] userparams = null;
        if (authConfigurer.getNeededPermission() != null) {
            userparams = userRoleUsernames.get(authConfigurer.getNeededPermission());
        } else {
            userparams = userRoleUsernames.get(MissingPermission.ALL);
        }
        url = manipulateUrl(authConfigurer.getUrl(), resetObject);
        resp =
                this.executeAsUser(authConfigurer.getMethod(), url, authConfigurer.getBody(),
                        userparams[0], userparams[1], authConfigurer.isHtml());
        if ((authConfigurer.getRoleRestriction() == null ||
                !authConfigurer.getRoleRestriction().equals(RoleRestriction.ADMIN)) &&
                authConfigurer.getNeededPermission() == null) {
            assertTrue(resp.getStatusLine().getStatusCode() < 400);
        } else {
            assertEquals(403, resp.getStatusLine().getStatusCode());
        }
        resetObject = resetState(authConfigurer.isResetState(), resetObject);
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
    private Object resetState(boolean resetState, Object resetObject) throws Exception {
        if (resetState && resetObject != null) {
            if (resetObject instanceof Entity) {
                resetObject = resetEntity((Entity) resetObject);
            } else if (resetObject instanceof User) {
                resetUser((User) resetObject);
            } else if (resetObject instanceof UserRequest) {
                resetUserRequest((UserRequest) resetObject);
            }
        }
        return resetObject;
    }

    private Entity resetEntity(Entity resetEntity) throws Exception {
        // check if entity is there
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + resetEntity.getId()));
        if (resetEntity.getBinaries() != null) {
            for (Entry<String, Binary> binary : resetEntity.getBinaries().entrySet()) {
                binary.getValue()
                        .setSource(
                                new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/image_1.png")
                                        .toURI()));
            }
        }
        if (resp.getStatusLine().getStatusCode() == HttpStatus.NOT_FOUND.value()) {
            String response = EntityUtils.toString(resp.getEntity());
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
            Entity storedEntity = mapper.readValue(resp.getEntity().getContent(), Entity.class);
            if (EntityState.PUBLISHED.equals(storedEntity.getState()) ||
                    EntityState.WITHDRAWN.equals(storedEntity.getState())) {
                // recreate with different id
                resetEntity.setBinaries(new HashMap<String, Binary>());
                resetEntity.setId(null);
                resp =
                        this.executeAsAdmin(
                                Request.Post(entityUrl).bodyString(
                                        mapper.writeValueAsString(resetEntity),
                                        ContentType.APPLICATION_JSON));
                assertEquals(201, resp.getStatusLine().getStatusCode());
                String entityId = EntityUtils.toString(resp.getEntity());
                resetEntity.setId(entityId);
            } else {
                String urlSuffix = null;
                if (resetEntity.getState().equals(EntityState.SUBMITTED)) {
                    urlSuffix = "submit";
                } else if (resetEntity.getState().equals(EntityState.PUBLISHED)) {
                    urlSuffix = "publish";
                } else if (resetEntity.getState().equals(EntityState.WITHDRAWN)) {
                    urlSuffix = "withdraw";
                } else if (resetEntity.getState().equals(EntityState.PENDING)) {
                    urlSuffix = "pending";
                }
                resp =
                        this.executeAsAdmin(
                                Request.Put(entityUrl + resetEntity.getId() + "/" +
                                        urlSuffix));
                String response = EntityUtils.toString(resp.getEntity());
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
        }
        return resetEntity;
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
        } else if (url.matches(".*(publish|withdraw).*")) {
            url = url.replaceFirst("(/entity/).*?/", "$1" + ((Entity)resetObject).getId() + "/");
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
