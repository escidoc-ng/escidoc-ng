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
import net.objecthunter.larch.model.security.role.AreaAdminRole;
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

    private static Role areaAdminRole;

    @Before
    public void initialize() throws Exception {
        if (methodCounter == 0) {
            preparePermission();
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

        // area admin
        userRoles.remove(adminRole);
        userRoles.add(areaAdminRole);
        HttpResponse resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/roles", this.mapper
                        .writeValueAsString(userRoles),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/roles", this.mapper
                        .writeValueAsString(userRoles),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        userRoles.remove(areaAdminRole);
        userRoles.add(userRole);
        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/roles", this.mapper
                        .writeValueAsString(userRoles),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/roles", this.mapper
                        .writeValueAsString(userRoles),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], false);
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
    public void testCreateRolesHtml() throws Exception {
        List<Role> userRoles = new ArrayList<Role>();
        userRoles.add(adminRole);

        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, userUrl + username + "/roles")
                .body(this.mapper.writeValueAsString(userRoles))
                .roleRestriction(RoleRestriction.ADMIN)
                .html(true)
                .build());

        // area admin
        userRoles.remove(adminRole);
        userRoles.add(areaAdminRole);
        HttpResponse resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/roles", this.mapper
                        .writeValueAsString(userRoles),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], true);
        String response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/roles", this.mapper
                        .writeValueAsString(userRoles),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], true);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        userRoles.remove(areaAdminRole);
        userRoles.add(userRole);
        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/roles", this.mapper
                        .writeValueAsString(userRoles),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], true);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/roles", this.mapper
                        .writeValueAsString(userRoles),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], true);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        userRoles.remove(userRole);
        userRoles.add(userAdminRole);
        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/roles", this.mapper
                        .writeValueAsString(userRoles),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], true);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/roles", this.mapper
                        .writeValueAsString(userRoles),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], true);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        // userAdmin
        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/roles", this.mapper
                        .writeValueAsString(userRoles),
                        userAdminRoleUsernames.get("ROLE_USER_ADMIN")[0], userAdminRoleUsernames
                                .get("ROLE_USER_ADMIN")[1], true);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

    }

    @Test
    public void testSetRight() throws Exception {
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, userUrl + username + "/role/role_user_admin/right/" + username)
                .body(this.mapper.writeValueAsString(userAdminRole.getRights().get("")))
                .roleRestriction(RoleRestriction.ADMIN)
                .build());

        // area admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/role/role_area_admin/right/" + areaId1,
                        this.mapper.writeValueAsString(areaAdminRole.getRights().get(areaId1)),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], false);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/role/role_area_admin/right/" + areaId1, this.mapper
                        .writeValueAsString(areaAdminRole.getRights().get(areaId1)),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/role/role_user/right/" + permissionId, this.mapper
                        .writeValueAsString(userRole.getRights().get(permissionId)),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/role/role_user/right/" + permissionId, this.mapper
                        .writeValueAsString(userRole.getRights().get(permissionId)),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        // userAdmin
        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/role/role_user/right/" + permissionId, this.mapper
                        .writeValueAsString(userRole.getRights().get(permissionId)),
                        userAdminRoleUsernames.get("ROLE_USER_ADMIN")[0], userAdminRoleUsernames
                                .get("ROLE_USER_ADMIN")[1], false);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

    }

    @Test
    public void testSetRightHtml() throws Exception {
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, userUrl + username + "/role/role_user_admin/right/" + username)
                .body(this.mapper.writeValueAsString(userAdminRole.getRights().get("")))
                .roleRestriction(RoleRestriction.ADMIN)
                .html(true)
                .build());

        // area admin
        HttpResponse resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/role/role_area_admin/right/" + areaId1,
                        this.mapper.writeValueAsString(areaAdminRole.getRights().get(areaId1)),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], true);
        String response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/role/role_area_admin/right/" + areaId1, this.mapper
                        .writeValueAsString(areaAdminRole.getRights().get(areaId1)),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], true);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/role/role_user/right/" + permissionId, this.mapper
                        .writeValueAsString(userRole.getRights().get(permissionId)),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + Fixtures.AREA_ID)[1], true);
        response = EntityUtils.toString(resp.getEntity());
        assertTrue(resp.getStatusLine().getStatusCode() < 400);

        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/role/role_user/right/" + permissionId, this.mapper
                        .writeValueAsString(userRole.getRights().get(permissionId)),
                        areaAdminRoleUsernames.get("ROLE_AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("ROLE_AREA_ADMIN" + areaId1)[1], true);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

        // userAdmin
        resp =
                this.executeAsUser(HttpMethod.POST, userUrl + username + "/role/role_user/right/" + permissionId, this.mapper
                        .writeValueAsString(userRole.getRights().get(permissionId)),
                        userAdminRoleUsernames.get("ROLE_USER_ADMIN")[0], userAdminRoleUsernames
                                .get("ROLE_USER_ADMIN")[1], true);
        response = EntityUtils.toString(resp.getEntity());
        assertEquals(403, resp.getStatusLine().getStatusCode());

    }

    private void prepareRoles() throws Exception {
        // create user
        username = createUser(null, userPassword);

        // create area-admin role
        areaAdminRole = new AreaAdminRole();
        List<RoleRight> areaAdminRoleRights = new ArrayList<RoleRight>();
        for (RoleRight roleRight : areaAdminRole.allowedRights()) {
            areaAdminRoleRights.add(roleRight);
        }
        if (!areaAdminRoleRights.isEmpty()) {
            Map<String, List<RoleRight>> newRights = new HashMap<String, List<RoleRight>>();
            newRights.put(areaId1, areaAdminRoleRights);
            areaAdminRole.setRights(newRights);
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
            newRights.put(permissionId, userRoleRights);
            userRole.setRights(newRights);
        }

        // create admin role
        adminRole = new AdminRole();

    }

}
