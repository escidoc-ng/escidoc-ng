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

import net.objecthunter.larch.integration.helpers.AuthConfigurer;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.RoleRestriction;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizeSettingsControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeSettingsControllerIT.class);

    @Test
    public void testRetrieveSettings() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "settings")
                .roleRestriction(RoleRestriction.ADMIN)
                .build());
    }

    @Test
    public void testRetrieveSettingsHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "settings")
                .roleRestriction(RoleRestriction.ADMIN)
                .html(true)
                .build());
    }

}
