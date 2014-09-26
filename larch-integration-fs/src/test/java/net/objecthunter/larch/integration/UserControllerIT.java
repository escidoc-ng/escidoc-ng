/*
 * Copyright 2014 Michael Hoppe
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

import static org.junit.Assert.*;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.role.AreaAdminRole;
import net.objecthunter.larch.model.security.role.Role.RoleName;
import net.objecthunter.larch.model.security.role.UserRole;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserControllerIT extends AbstractLarchIT {

    private static final Logger log = LoggerFactory.getLogger(UserControllerIT.class);

    @Test
    public void testDeleteRights() throws Exception {
        //create areas + permissions
        String areaId = createArea();
        String areaId1 = createArea();
        String permissionId = createPermission(areaId);
        
        // createUsers + roles
        String username = createUser(null, "ttestt");
        String username1 = createUser(null, "ttestt");
        String username2 = createUser(null, "ttestt");
        createRoleForUser(username, new AreaAdminRole(), areaId);
        createRoleForUser(username, new AreaAdminRole(), areaId1);
        createRoleForUser(username, new UserRole(), permissionId);
        createRoleForUser(username1, new UserRole(), permissionId);
        createRoleForUser(username1, new AreaAdminRole(), areaId);
        createRoleForUser(username2, new AreaAdminRole(), areaId1);
        
        // delete area1
        HttpResponse resp = this.executeAsAdmin(Request.Delete(entityUrl + areaId1));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        // check user rights
        resp = this.executeAsAdmin(Request.Get(userUrl + username));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        User user = mapper.readValue(resp.getEntity().getContent(), User.class);
        assertNotNull(user.getRoles());
        assertEquals(2, user.getRoles().size());
        assertNotNull(user.getRole(RoleName.ROLE_AREA_ADMIN));
        assertNotNull(user.getRole(RoleName.ROLE_AREA_ADMIN).getRights());
        assertEquals(1, user.getRole(RoleName.ROLE_AREA_ADMIN).getRights().size());
        assertNotNull(user.getRole(RoleName.ROLE_AREA_ADMIN).getRights().get(areaId));
        assertEquals(2, user.getRole(RoleName.ROLE_AREA_ADMIN).getRights().get(areaId).size());

        resp = this.executeAsAdmin(Request.Get(userUrl + username1));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        User user1 = mapper.readValue(resp.getEntity().getContent(), User.class);
        assertNotNull(user1.getRoles());
        assertEquals(2, user1.getRoles().size());
        assertNotNull(user1.getRole(RoleName.ROLE_AREA_ADMIN));
        assertNotNull(user1.getRole(RoleName.ROLE_AREA_ADMIN).getRights());
        assertEquals(1, user1.getRole(RoleName.ROLE_AREA_ADMIN).getRights().size());
        assertNotNull(user1.getRole(RoleName.ROLE_AREA_ADMIN).getRights().get(areaId));
        assertEquals(2, user1.getRole(RoleName.ROLE_AREA_ADMIN).getRights().get(areaId).size());
        assertNotNull(user1.getRole(RoleName.ROLE_USER));
        assertNotNull(user1.getRole(RoleName.ROLE_USER).getRights());
        assertEquals(1, user1.getRole(RoleName.ROLE_USER).getRights().size());
        assertNotNull(user1.getRole(RoleName.ROLE_USER).getRights().get(permissionId));
        assertNotEquals(0, user1.getRole(RoleName.ROLE_USER).getRights().get(permissionId).size());

        resp = this.executeAsAdmin(Request.Get(userUrl + username2));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        User user2 = mapper.readValue(resp.getEntity().getContent(), User.class);
        assertNotNull(user2.getRoles());
        assertEquals(0, user2.getRoles().size());

        // delete area
        resp = this.executeAsAdmin(Request.Delete(entityUrl + areaId));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        // check user rights
        resp = this.executeAsAdmin(Request.Get(userUrl + username));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        user = mapper.readValue(resp.getEntity().getContent(), User.class);
        assertNotNull(user.getRoles());
        assertEquals(0, user.getRoles().size());

        resp = this.executeAsAdmin(Request.Get(userUrl + username1));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        user1 = mapper.readValue(resp.getEntity().getContent(), User.class);
        assertNotNull(user1.getRoles());
        assertEquals(0, user1.getRoles().size());

        resp = this.executeAsAdmin(Request.Get(userUrl + username2));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        user2 = mapper.readValue(resp.getEntity().getContent(), User.class);
        assertNotNull(user2.getRoles());
        assertEquals(0, user2.getRoles().size());

    }

}
