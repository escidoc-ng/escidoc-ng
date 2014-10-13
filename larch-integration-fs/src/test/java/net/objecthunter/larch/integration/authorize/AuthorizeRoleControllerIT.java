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

package net.objecthunter.larch.integration.authorize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.objecthunter.larch.integration.helpers.AuthConfigurer;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.RoleRestriction;
import net.objecthunter.larch.model.security.role.AdminRole;
import net.objecthunter.larch.model.security.role.Level1AdminRole;
import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.model.security.role.Role.RoleRight;
import net.objecthunter.larch.model.security.role.UserAdminRole;
import net.objecthunter.larch.model.security.role.UserRole;
import net.objecthunter.larch.test.util.Fixtures;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizeRoleControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeRoleControllerIT.class);

    private static int methodCounter = 0;

    private static String username;

    private static Role userRole;

    private static Role adminRole;

    private static Role userAdminRole;

    private static Role level1AdminRole;

    @Before
    public void initialize() throws Exception {
        if (methodCounter == 0) {
            prepareLevel2();
            prepareRoles();
            methodCounter++;
        }
    }

    @Test
    public void testCreateRoles() throws Exception {
        List<Role> userRoles = new ArrayList<Role>();
        userRoles.add(adminRole);

        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, userUrl + username + "/roles")
                .body(this.mapper.writeValueAsString(userRoles))
                .roleRestriction(RoleRestriction.ADMIN)
                .build());

        // level1 admin
        userRoles.remove(adminRole);
        userRoles.add(level1AdminRole);
        HttpResponse resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/roles", this.mapper
                        .writeValueAsString(userRoles),
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + level1Id1)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/roles", this.mapper
                        .writeValueAsString(userRoles),
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        userRoles.remove(level1AdminRole);
        userRoles.add(userRole);
        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/roles", this.mapper
                        .writeValueAsString(userRoles),
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/roles", this.mapper
                        .writeValueAsString(userRoles),
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + level1Id1)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        // userAdmin
        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/roles", this.mapper
                        .writeValueAsString(userRoles),
                        userAdminRoleUsernames.get("ROLE_USER_ADMIN")[0], userAdminRoleUsernames
                                .get("ROLE_USER_ADMIN")[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

    }

    @Test
    public void testSetRight() throws Exception {
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, userUrl + username + "/role/role_user_admin/rights/" + username)
                .body(this.mapper.writeValueAsString(userAdminRole.getRights().get("")))
                .roleRestriction(RoleRestriction.ADMIN)
                .build());

        // level1 admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/role/role_level1_admin/rights/" + level1Id1,
                        this.mapper.writeValueAsString(level1AdminRole.getRights().get(level1Id1)),
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + level1Id1)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/role/role_admin/rights/", "{}",
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/role/role_level1_admin/rights/" + level1Id1, this.mapper
                        .writeValueAsString(level1AdminRole.getRights().get(level1Id1)),
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/role/role_user/rights/" + level2Id, this.mapper
                        .writeValueAsString(userRole.getRights().get(level2Id)),
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/role/role_user/rights/" + level2Id, this.mapper
                        .writeValueAsString(userRole.getRights().get(level2Id)),
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + level1Id1)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        // userAdmin
        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/role/role_user/rights/" + level2Id, this.mapper
                        .writeValueAsString(userRole.getRights().get(level2Id)),
                        userAdminRoleUsernames.get("ROLE_USER_ADMIN")[0], userAdminRoleUsernames
                                .get("ROLE_USER_ADMIN")[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/role/role_admin/rights/", "{}",
                        userAdminRoleUsernames.get("ROLE_USER_ADMIN")[0], userAdminRoleUsernames
                        .get("ROLE_USER_ADMIN")[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

    }

    private void prepareRoles() throws Exception {
        // create user
        username = createUser(null, userPassword);

        // create level1-admin role
        level1AdminRole = new Level1AdminRole();
        List<RoleRight> level1AdminRoleRights = new ArrayList<RoleRight>();
        for (RoleRight roleRight : level1AdminRole.allowedRights()) {
            level1AdminRoleRights.add(roleRight);
        }
        if (!level1AdminRoleRights.isEmpty()) {
            Map<String, List<RoleRight>> newRights = new HashMap<String, List<RoleRight>>();
            newRights.put(level1Id1, level1AdminRoleRights);
            level1AdminRole.setRights(newRights);
        }

        // create user-admin role
        userAdminRole = new UserAdminRole();
        List<RoleRight> userAdminRoleRights = new ArrayList<RoleRight>();
        for (RoleRight roleRight : userAdminRole.allowedRights()) {
            userAdminRoleRights.add(roleRight);
        }
        if (!userAdminRoleRights.isEmpty()) {
            Map<String, List<RoleRight>> newRights = new HashMap<String, List<RoleRight>>();
            newRights.put("", userAdminRoleRights);
            userAdminRole.setRights(newRights);
        }

        // create user role
        userRole = new UserRole();
        List<RoleRight> userRoleRights = new ArrayList<RoleRight>();
        for (RoleRight roleRight : userRole.allowedRights()) {
            userRoleRights.add(roleRight);
        }
        if (!userRoleRights.isEmpty()) {
            Map<String, List<RoleRight>> newRights = new HashMap<String, List<RoleRight>>();
            newRights.put(level2Id, userRoleRights);
            userRole.setRights(newRights);
        }

        // create admin role
        adminRole = new AdminRole();

    }

}
