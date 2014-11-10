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

import static org.junit.Assert.assertTrue;
import net.objecthunter.larch.integration.helpers.AuthConfigurer;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.ObjectType;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.RoleRestriction;
import net.objecthunter.larch.model.security.UserRequest;

import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizeUserControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeUserControllerIT.class);

    @Test
    public void testRetrieveUserRequest() throws Exception {
        // create user request
        UserRequest userRequest = createUserRequest(null, userPassword);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "confirm/" + userRequest.getToken())
                .build());
    }

    @Test
    public void testConfirmUserRequest() throws Exception {
        // create user request
        UserRequest userRequest = createUserRequest(null, userPassword);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, hostUrl + "confirm/{token}")
                .body(MultipartEntityBuilder.create()
                        .addTextBody("password", userPassword)
                        .addTextBody("passwordRepeat", userPassword)
                        .build())
                .resetState(true)
                .resetStateObjectType(ObjectType.USER_REQUEST)
                .resetStateId(userRequest.getUser().getName() + "|" + userRequest.getToken())
                .build());
    }

    @Test
    public void testDeleteUser() throws Exception {
        // create user
        String username = createUser(null, userPassword);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, hostUrl + "user/" + username)
                .roleRestriction(RoleRestriction.ADMIN)
                .resetState(true)
                .resetStateObjectType(ObjectType.USER)
                .resetStateId(username)
                .build());
    }

    @Test
    public void testCreateUserRequest() throws Exception {
        // TODO
    }

    @Test
    public void testRetrieveUser() throws Exception {
        // create user
        String username = createUser(null, userPassword);

        // try getting user as user
        HttpResponse resp =
                this.executeAsUser(HttpMethod.GET, hostUrl + "user/" + username, null,
                        username, userPassword, false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);
    }

    @Test
    public void testUpdateUserDetails() throws Exception {
        // create user
        String username = createUser(null, userPassword);
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, hostUrl + "user/" + username)
                .body(MultipartEntityBuilder.create()
                        .addTextBody("first_name", "test")
                        .addTextBody("last_name", "test")
                        .addTextBody("email", username + "@fiz.de")
                        .build())
                .roleRestriction(RoleRestriction.ADMIN)
                .build());
    }

}
