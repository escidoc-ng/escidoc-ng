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

public class AuthorizeListControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeListControllerIT.class);

    @Test
    public void testListData() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "list/DATA")
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "list/DATA/1")
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "list/DATA/1/1")
                .build());
    }

    @Test
    public void testListDataHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "list/DATA")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "list/DATA/1")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "list/DATA/1/1")
                .html(true)
                .build());
    }

    @Test
    public void testBrowseDataHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "browse/DATA")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "browse/DATA/1")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "browse/DATA/1/1")
                .html(true)
                .build());
    }

    @Test
    public void testListPermissionData() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + permissionId + "/children/DATA/list")
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + permissionId + "/children/DATA/list/1")
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + permissionId + "/children/DATA/list/1/1")
                .build());
    }

    @Test
    public void testListPermissionDataHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + permissionId + "/children/DATA/list")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + permissionId + "/children/DATA/list/1")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + permissionId + "/children/DATA/list/1/1")
                .html(true)
                .build());
    }

    @Test
    public void testBrowsePermissionDataHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + permissionId + "/children/DATA/browse")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + permissionId + "/children/DATA/browse/1")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + permissionId + "/children/DATA/browse/1/1")
                .html(true)
                .build());
    }

    @Test
    public void testListPermissions() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "list/PERMISSION")
                .roleRestriction(RoleRestriction.LOGGED_IN)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "list/PERMISSION/1")
                .roleRestriction(RoleRestriction.LOGGED_IN)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "list/PERMISSION/1/2")
                .roleRestriction(RoleRestriction.LOGGED_IN)
                .build());
    }

    @Test
    public void testListPermissionsHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "list/PERMISSION")
                .roleRestriction(RoleRestriction.LOGGED_IN)
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "list/PERMISSION/1")
                .roleRestriction(RoleRestriction.LOGGED_IN)
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "list/PERMISSION/1/2")
                .roleRestriction(RoleRestriction.LOGGED_IN)
                .html(true)
                .build());
    }

}
