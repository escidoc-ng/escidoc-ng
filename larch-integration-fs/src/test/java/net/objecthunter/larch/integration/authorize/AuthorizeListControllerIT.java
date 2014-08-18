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
    public void testListEntities() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/list")
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/list/1")
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/list/1/1")
                .build());
    }

    @Test
    public void testListEntitiesHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/list")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/list/1")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/list/1/1")
                .html(true)
                .build());
    }

    @Test
    public void testBrowseEntitiesHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/browse")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/browse/1")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/browse/1/1")
                .html(true)
                .build());
    }

    @Test
    public void testListPublishedEntities() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/list/published")
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/list/published/1")
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/list/published/1/1")
                .build());
    }

    @Test
    public void testListPublishedEntitiesHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/list/published")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/list/published/1")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/list/published/1/1")
                .html(true)
                .build());
    }

    @Test
    public void testBrowsePublishedEntitiesHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/browse/published")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/browse/published/1")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "/browse/published/1/1")
                .html(true)
                .build());
    }

    @Test
    public void testListWorkspaceEntities() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/list")
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/list/1")
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/list/1/1")
                .build());
    }

    @Test
    public void testListWorkspaceEntitiesHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/list")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/list/1")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/list/1/1")
                .html(true)
                .build());
    }

    @Test
    public void testBrowseWorkspaceEntitiesHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/browse")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/browse/1")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/browse/1/1")
                .html(true)
                .build());
    }

    @Test
    public void testListPublishedWorkspaceEntities() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/list/published")
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/list/published/1")
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/list/published/1/1")
                .build());
    }

    @Test
    public void testListPublishedWorkspaceEntitiesHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/list/published")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/list/published/1")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/list/published/1/1")
                .html(true)
                .build());
    }

    @Test
    public void testBrowsePublishedWorkspaceEntitiesHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/browse/published")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/browse/published/1")
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId + "/browse/published/1/1")
                .html(true)
                .build());
    }

    @Test
    public void testRetrieveWorkspaces() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "workspace-list")
                .roleRestriction(RoleRestriction.LOGGED_IN)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "workspace-list/1")
                .roleRestriction(RoleRestriction.LOGGED_IN)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "workspace-list/1/2")
                .roleRestriction(RoleRestriction.LOGGED_IN)
                .build());
    }

    @Test
    public void testRetrieveWorkspacesHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "workspace-list")
                .roleRestriction(RoleRestriction.LOGGED_IN)
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "workspace-list/1")
                .roleRestriction(RoleRestriction.LOGGED_IN)
                .html(true)
                .build());
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, hostUrl + "workspace-list/1/2")
                .roleRestriction(RoleRestriction.LOGGED_IN)
                .html(true)
                .build());
    }

}
