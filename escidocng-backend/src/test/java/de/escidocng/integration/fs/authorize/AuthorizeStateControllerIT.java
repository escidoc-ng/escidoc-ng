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


package de.escidocng.integration.fs.authorize;

import de.escidocng.integration.fs.helpers.AuthConfigurer;
import de.escidocng.integration.fs.helpers.AuthConfigurer.RoleRestriction;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizeStateControllerIT extends AbstractAuthorizeEscidocngIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeStateControllerIT.class);

    @Test
    public void testRetrieveState() throws Exception {
        testUserRoleAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "state")
                .roleRestriction(RoleRestriction.ADMIN)
                .build());
    }

}
