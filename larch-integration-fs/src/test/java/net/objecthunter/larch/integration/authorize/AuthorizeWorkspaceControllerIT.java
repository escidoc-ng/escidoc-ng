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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import net.objecthunter.larch.integration.helpers.AuthConfigurer;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.MissingPermission;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.ObjectType;
import net.objecthunter.larch.integration.helpers.AuthConfigurer.RoleRestriction;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Workspace;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

public class AuthorizeWorkspaceControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeWorkspaceControllerIT.class);

    @Test
    public void testCreateWorkspace() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.POST, workspaceUrl)
                .roleRestriction(RoleRestriction.LOGGED_IN)
                .body(mapper.writeValueAsString(getWorkspace()))
                .build());
    }

    @Test
    public void testRetrieveWorkspace() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId)
                .neededPermission(MissingPermission.READ_WORKSPACE)
                .build());
    }

    @Test
    public void testRetrieveWorkspaceHtml() throws Exception {
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.GET, workspaceUrl + workspaceId)
                .neededPermission(MissingPermission.READ_WORKSPACE)
                .html(true)
                .build());
    }

    @Test
    public void testUpdateWorkspace() throws Exception {
        // retrieve workspace
        Workspace workspace = retrieveWorkspace(workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PUT, workspaceUrl + workspaceId)
                .body(mapper.writeValueAsString(workspace))
                .neededPermission(MissingPermission.WRITE_WORKSPACE)
                .build());
    }

    @Test
    public void testPatchWorkspace() throws Exception {
        // retrieve workspace
        Workspace workspace = retrieveWorkspace(workspaceId);
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.PATCH, workspaceUrl + workspaceId)
                .body(mapper.writeValueAsString(workspace))
                .neededPermission(MissingPermission.WRITE_WORKSPACE)
                .build());
    }

    @Test
    public void testDeleteWorkspace() throws Exception {
        // create workspace
        Workspace workspace = createWorkspace();
        testAuth(new AuthConfigurer.AuthConfigurerBuilder(
                HttpMethod.DELETE, workspaceUrl + workspace.getId())
                .neededPermission(MissingPermission.WRITE_WORKSPACE)
                .resetState(true)
                .resetStateObjectType(ObjectType.WORKSPACE)
                .resetStateId(workspace.getId())
                .build());
    }

    private Workspace getWorkspace() {
        final Workspace ws = new Workspace();
        ws.setOwner("foo");
        ws.setName("bar");
        return ws;
    }

    private Workspace retrieveWorkspace(String workspaceId) throws IOException {
        HttpResponse resp = this.executeAsAdmin(
                Request.Get(workspaceUrl + workspaceId));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        return mapper.readValue(resp.getEntity().getContent(), Workspace.class);
    }

}
