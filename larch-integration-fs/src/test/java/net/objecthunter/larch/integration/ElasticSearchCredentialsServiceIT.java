
package net.objecthunter.larch.integration;

import static org.junit.Assert.assertEquals;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchCredentialsService;
import net.objecthunter.larch.test.util.Fixtures;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
public class ElasticSearchCredentialsServiceIT extends AbstractLarchIT {

    @Autowired
    private ElasticSearchCredentialsService credentialsService;

    @Test
    public void testAddUser() throws Exception {
        User u = Fixtures.createUser();
        this.credentialsService.createUser(u);
        assertEquals(u, this.credentialsService.retrieveUser(u.getName()));
    }

    @Test
    public void testEditUser() throws Exception {
        User u = Fixtures.createUser();
        this.credentialsService.createUser(u);
        u.setLastName("foo1");
        u.setFirstName("bar1");
        u.setEmail("foo1.bar1@example.com");
        this.credentialsService.updateUser(u);
        assertEquals(u, this.credentialsService.retrieveUser(u.getName()));
    }

    @Test
    public void testRemoveUser() throws Exception {
        User u = Fixtures.createUser();
        this.credentialsService.createUser(u);
        this.credentialsService.deleteUser(u.getName());
    }

    @Test(expected = Exception.class)
    public void testRemoveAndRetrieveUser() throws Exception {
        User u = Fixtures.createUser();
        this.credentialsService.createUser(u);
        this.credentialsService.deleteUser(u.getName());
        this.credentialsService.retrieveUser(u.getName());
    }

    @Test(expected = Exception.class)
    public void testAddUserNoEmail() throws Exception {
        User u = Fixtures.createUser();
        u.setEmail("");
        this.credentialsService.createUser(u);
    }

    @Test(expected = Exception.class)
    public void testAddUserNoUsername() throws Exception {
        User u = Fixtures.createUser();
        u.setName("");
        this.credentialsService.createUser(u);
    }

    @Test(expected = Exception.class)
    public void testEditUserNoEmail() throws Exception {
        User u = Fixtures.createUser();
        this.credentialsService.createUser(u);
        u.setEmail("");
        this.credentialsService.updateUser(u);
    }

    @Test(expected = Exception.class)
    public void testEditUserNoUsername() throws Exception {
        User u = Fixtures.createUser();
        this.credentialsService.createUser(u);
        u.setName("");
        this.credentialsService.updateUser(u);
    }
}
