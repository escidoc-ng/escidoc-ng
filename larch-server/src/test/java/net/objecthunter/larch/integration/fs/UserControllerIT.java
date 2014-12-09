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

import static org.junit.Assert.*;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.role.Level1AdminRole;
import net.objecthunter.larch.model.security.role.Role.RoleName;
import net.objecthunter.larch.model.security.role.UserRole;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserControllerIT extends AbstractFSLarchIT {

    private static final Logger log = LoggerFactory.getLogger(UserControllerIT.class);

    @Test
    public void testDeleteRights() throws Exception {
        //create level1s + level2s
        String level1Id = createLevel1();
        String level1Id1 = createLevel1();
        String level2Id = createLevel2(level1Id);
        
        // createUsers + roles
        String username = createUser(null, "ttestt");
        String username1 = createUser(null, "ttestt");
        String username2 = createUser(null, "ttestt");
        createRoleForUser(username, new Level1AdminRole(), level1Id);
        createRoleForUser(username, new Level1AdminRole(), level1Id1);
        createRoleForUser(username, new UserRole(), level2Id);
        createRoleForUser(username1, new UserRole(), level2Id);
        createRoleForUser(username1, new Level1AdminRole(), level1Id);
        createRoleForUser(username2, new Level1AdminRole(), level1Id1);
        
        // delete level11
        HttpResponse resp = this.executeAsAdmin(Request.Delete(entityUrl + level1Id1));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        // check user rights
        resp = this.executeAsAdmin(Request.Get(userUrl + username));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        User user = mapper.readValue(resp.getEntity().getContent(), User.class);
        assertNotNull(user.getRoles());
        assertEquals(2, user.getRoles().size());
        assertNotNull(user.getRole(RoleName.ROLE_LEVEL1_ADMIN));
        assertNotNull(user.getRole(RoleName.ROLE_LEVEL1_ADMIN).getRights());
        assertEquals(1, user.getRole(RoleName.ROLE_LEVEL1_ADMIN).getRights().size());
        assertNotNull(user.getRole(RoleName.ROLE_LEVEL1_ADMIN).getRights().get(level1Id));
        assertEquals(2, user.getRole(RoleName.ROLE_LEVEL1_ADMIN).getRights().get(level1Id).size());

        resp = this.executeAsAdmin(Request.Get(userUrl + username1));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        User user1 = mapper.readValue(resp.getEntity().getContent(), User.class);
        assertNotNull(user1.getRoles());
        assertEquals(2, user1.getRoles().size());
        assertNotNull(user1.getRole(RoleName.ROLE_LEVEL1_ADMIN));
        assertNotNull(user1.getRole(RoleName.ROLE_LEVEL1_ADMIN).getRights());
        assertEquals(1, user1.getRole(RoleName.ROLE_LEVEL1_ADMIN).getRights().size());
        assertNotNull(user1.getRole(RoleName.ROLE_LEVEL1_ADMIN).getRights().get(level1Id));
        assertEquals(2, user1.getRole(RoleName.ROLE_LEVEL1_ADMIN).getRights().get(level1Id).size());
        assertNotNull(user1.getRole(RoleName.ROLE_USER));
        assertNotNull(user1.getRole(RoleName.ROLE_USER).getRights());
        assertEquals(1, user1.getRole(RoleName.ROLE_USER).getRights().size());
        assertNotNull(user1.getRole(RoleName.ROLE_USER).getRights().get(level2Id));
        assertNotEquals(0, user1.getRole(RoleName.ROLE_USER).getRights().get(level2Id).size());

        resp = this.executeAsAdmin(Request.Get(userUrl + username2));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        User user2 = mapper.readValue(resp.getEntity().getContent(), User.class);
        assertNotNull(user2.getRoles());
        assertEquals(0, user2.getRoles().size());

        // delete level1
        resp = this.executeAsAdmin(Request.Delete(entityUrl + level1Id));
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
