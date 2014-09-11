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

package net.objecthunter.larch.service.elasticsearch;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import net.objecthunter.larch.model.security.Group;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchCredentialsService;
import net.objecthunter.larch.test.util.Fixtures;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticSearchCredentialsServiceTest {

    private ElasticSearchCredentialsService credentialsService;

    private Client mockClient;

    private AdminClient mockAdminClient;

    private IndicesAdminClient mockIndicesAdminClient;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        this.credentialsService = new ElasticSearchCredentialsService();
        this.mockClient = createMock(Client.class);
        mockAdminClient = createMock(AdminClient.class);
        mockIndicesAdminClient = createMock(IndicesAdminClient.class);
        ReflectionTestUtils.setField(credentialsService, "mapper", mapper);
        ReflectionTestUtils.setField(credentialsService, "client", mockClient);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAuthenticate() throws Exception {
        User u = Fixtures.createUser();
        GetResponse mockResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);

        expect(mockClient.prepareGet(ElasticSearchCredentialsService.INDEX_USERS,
                ElasticSearchCredentialsService.INDEX_USERS_TYPE, u.getName())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockResponse);
        expect(mockResponse.isExists()).andReturn(true);
        expect(mockResponse.getSourceAsBytes()).andReturn(mapper.writeValueAsBytes(u));

        replay(mockClient, mockFuture, mockGetRequestBuilder, mockResponse);
        Authentication auth =
                this.credentialsService.authenticate(new UsernamePasswordAuthenticationToken(u.getName(),
                        "test"));
        verify(mockClient, mockFuture, mockGetRequestBuilder, mockResponse);

        assertNotNull(auth);
        assertTrue(auth.isAuthenticated());
        assertEquals(u.getName(), ((User) auth.getPrincipal()).getName());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateUser() throws Exception {
        User u = Fixtures.createUser();
        GetResponse mockGetResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        IndexRequestBuilder mockIndexRequestBuilder = createMock(IndexRequestBuilder.class);

        /* existence check */
        expect(mockClient.prepareGet(ElasticSearchCredentialsService.INDEX_USERS,
                ElasticSearchCredentialsService.INDEX_USERS_TYPE, u.getName())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.isExists()).andReturn(false);

        /* user indexing */
        expect(mockClient.prepareIndex(ElasticSearchCredentialsService.INDEX_USERS,
                ElasticSearchCredentialsService.INDEX_USERS_TYPE,
                u.getName())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.setSource((byte[]) anyObject())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        /* index refresh */
        expect(mockClient.admin()).andReturn(mockAdminClient);
        expect(mockAdminClient.indices()).andReturn(mockIndicesAdminClient);
        expect(mockIndicesAdminClient.refresh(anyObject())).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        replay(mockClient, mockAdminClient, mockIndicesAdminClient, mockGetRequestBuilder, mockGetResponse,
                mockFuture, mockIndexRequestBuilder);
        this.credentialsService.createUser(u);
        verify(mockClient, mockAdminClient, mockIndicesAdminClient, mockGetRequestBuilder, mockGetResponse,
                mockFuture, mockIndexRequestBuilder);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateGroup() throws Exception {
        Group g = Fixtures.createGroup();
        GetResponse mockGetResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        IndexRequestBuilder mockIndexRequestBuilder = createMock(IndexRequestBuilder.class);

        /* existence check */
        expect(mockClient.prepareGet(ElasticSearchCredentialsService.INDEX_GROUPS,
                ElasticSearchCredentialsService.INDEX_GROUPS_TYPE, g.getName())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.isExists()).andReturn(false);

        /* group indexing */
        expect(mockClient.prepareIndex(ElasticSearchCredentialsService.INDEX_GROUPS,
                ElasticSearchCredentialsService.INDEX_GROUPS_TYPE,
                g.getName())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.setSource((byte[]) anyObject())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        /* index refresh */
        expect(mockClient.admin()).andReturn(mockAdminClient);
        expect(mockAdminClient.indices()).andReturn(mockIndicesAdminClient);
        expect(mockIndicesAdminClient.refresh(anyObject())).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        replay(mockClient, mockAdminClient, mockIndicesAdminClient, mockGetRequestBuilder, mockGetResponse,
                mockFuture, mockIndexRequestBuilder);
        this.credentialsService.createGroup(g);
        verify(mockClient, mockAdminClient, mockIndicesAdminClient, mockGetRequestBuilder, mockGetResponse,
                mockFuture, mockIndexRequestBuilder);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateUser() throws Exception {
        User u = Fixtures.createUser();
        u.setFirstName("foo");
        GetResponse mockGetResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        IndexRequestBuilder mockIndexRequestBuilder = createMock(IndexRequestBuilder.class);

        /* existence check */
        expect(mockClient.prepareGet(ElasticSearchCredentialsService.INDEX_USERS,
                ElasticSearchCredentialsService.INDEX_USERS_TYPE, u.getName())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.isExists()).andReturn(true);

        /* user indexing */
        expect(mockClient.prepareIndex(ElasticSearchCredentialsService.INDEX_USERS,
                ElasticSearchCredentialsService.INDEX_USERS_TYPE,
                u.getName())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.setSource((byte[]) anyObject())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        /* index refresh */
        expect(mockClient.admin()).andReturn(mockAdminClient);
        expect(mockAdminClient.indices()).andReturn(mockIndicesAdminClient);
        expect(mockIndicesAdminClient.refresh(anyObject())).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        replay(mockClient, mockAdminClient, mockIndicesAdminClient, mockGetRequestBuilder, mockGetResponse,
                mockFuture, mockIndexRequestBuilder);
        this.credentialsService.updateUser(u);
        verify(mockClient, mockAdminClient, mockIndicesAdminClient, mockGetRequestBuilder, mockGetResponse,
                mockFuture, mockIndexRequestBuilder);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateGroup() throws Exception {
        Group g = Fixtures.createGroup();
        GetResponse mockGetResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        IndexRequestBuilder mockIndexRequestBuilder = createMock(IndexRequestBuilder.class);

        /* existence check */
        expect(mockClient.prepareGet(ElasticSearchCredentialsService.INDEX_GROUPS,
                ElasticSearchCredentialsService.INDEX_GROUPS_TYPE, g.getName())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.isExists()).andReturn(true);

        /* group indexing */
        expect(mockClient.prepareIndex(ElasticSearchCredentialsService.INDEX_GROUPS,
                ElasticSearchCredentialsService.INDEX_GROUPS_TYPE,
                g.getName())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.setSource((byte[]) anyObject())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        replay(mockClient, mockGetRequestBuilder, mockGetResponse, mockFuture, mockIndexRequestBuilder);
        this.credentialsService.updateGroup(g);
        verify(mockClient, mockGetRequestBuilder, mockGetResponse, mockFuture, mockIndexRequestBuilder);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteUser() throws Exception {
        User u = Fixtures.createUser();
        GetResponse mockGetResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        DeleteRequestBuilder mockDeleteRequestBuilder = createMock(DeleteRequestBuilder.class);

        /* existence check */
        expect(mockClient.prepareGet(ElasticSearchCredentialsService.INDEX_USERS,
                ElasticSearchCredentialsService.INDEX_USERS_TYPE, u.getName())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.isExists()).andReturn(true);

        /* delete check */
        expect(mockClient.prepareGet(ElasticSearchCredentialsService.INDEX_USERS,
                ElasticSearchCredentialsService.INDEX_USERS_TYPE, u.getName())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.isExists()).andReturn(true);
        expect(mockGetResponse.getSourceAsBytes()).andReturn(mapper.writeValueAsBytes(u));

        Group adminGroup = new Group();
        adminGroup.setName("ROLE_ADMIN");
        expect(mockClient.prepareGet(ElasticSearchCredentialsService.INDEX_GROUPS,
                ElasticSearchCredentialsService.INDEX_GROUPS_TYPE, adminGroup.getName())).andReturn(
                mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.isExists()).andReturn(true);
        expect(mockGetResponse.getSourceAsBytes()).andReturn(mapper.writeValueAsBytes(adminGroup));

        expect(mockClient.prepareDelete(ElasticSearchCredentialsService.INDEX_USERS,
                ElasticSearchCredentialsService.INDEX_USERS_TYPE, u.getName())).andReturn(mockDeleteRequestBuilder);
        expect(mockDeleteRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        /* index refresh */
        expect(mockClient.admin()).andReturn(mockAdminClient);
        expect(mockAdminClient.indices()).andReturn(mockIndicesAdminClient);
        expect(mockIndicesAdminClient.refresh(anyObject())).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        replay(mockClient, mockAdminClient, mockIndicesAdminClient, mockGetRequestBuilder, mockGetResponse,
                mockFuture, mockDeleteRequestBuilder);
        this.credentialsService.deleteUser(u.getName());
        verify(mockClient, mockAdminClient, mockIndicesAdminClient, mockGetRequestBuilder, mockGetResponse,
                mockFuture, mockDeleteRequestBuilder);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteGroup() throws Exception {
        Group g = Fixtures.createGroup();
        GetResponse mockGetResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        DeleteRequestBuilder mockDeleteRequestBuilder = createMock(DeleteRequestBuilder.class);

        /* existence check */
        expect(mockClient.prepareGet(ElasticSearchCredentialsService.INDEX_GROUPS,
                ElasticSearchCredentialsService.INDEX_GROUPS_TYPE, g.getName())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.isExists()).andReturn(true);

        /* delete check */
        expect(mockClient.prepareDelete(ElasticSearchCredentialsService.INDEX_GROUPS,
                ElasticSearchCredentialsService.INDEX_GROUPS_TYPE, g.getName())).andReturn(mockDeleteRequestBuilder);
        expect(mockDeleteRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        /* index refresh */
        expect(mockClient.admin()).andReturn(mockAdminClient);
        expect(mockAdminClient.indices()).andReturn(mockIndicesAdminClient);
        expect(mockIndicesAdminClient.refresh(anyObject())).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        replay(mockClient, mockAdminClient, mockIndicesAdminClient, mockGetRequestBuilder, mockGetResponse,
                mockFuture, mockDeleteRequestBuilder);
        this.credentialsService.deleteGroup(g.getName());
        verify(mockClient, mockAdminClient, mockIndicesAdminClient, mockGetRequestBuilder, mockGetResponse,
                mockFuture, mockDeleteRequestBuilder);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRetrieveUser() throws Exception {
        User u = Fixtures.createUser();
        GetResponse mockGetResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);

        /* retrieve user */
        expect(mockClient.prepareGet(ElasticSearchCredentialsService.INDEX_USERS,
                ElasticSearchCredentialsService.INDEX_USERS_TYPE, u.getName())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.isExists()).andReturn(true);
        expect(mockGetResponse.getSourceAsBytes()).andReturn(mapper.writeValueAsBytes(u));

        replay(mockClient, mockGetRequestBuilder, mockGetResponse, mockFuture);
        this.credentialsService.retrieveUser(u.getName());
        verify(mockClient, mockGetRequestBuilder, mockGetResponse, mockFuture);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRetrieveGroup() throws Exception {
        Group g = Fixtures.createGroup();
        GetResponse mockGetResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);

        /* retrieve group */
        expect(mockClient.prepareGet(ElasticSearchCredentialsService.INDEX_GROUPS,
                ElasticSearchCredentialsService.INDEX_GROUPS_TYPE, g.getName())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.isExists()).andReturn(true);
        expect(mockGetResponse.getSourceAsBytes()).andReturn(mapper.writeValueAsBytes(g));

        replay(mockClient, mockGetRequestBuilder, mockGetResponse, mockFuture);
        this.credentialsService.retrieveGroup(g.getName());
        verify(mockClient, mockGetRequestBuilder, mockGetResponse, mockFuture);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRetrieveUsers() throws Exception {
        User u = Fixtures.createUser();
        SearchResponse mockSearchResponse = createMock(SearchResponse.class);
        SearchRequestBuilder mockSearchRequestBuilder = createMock(SearchRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        SearchHit[] hitArray = new SearchHit[1];
        SearchHit mockHit = createMock(SearchHit.class);
        hitArray[0] = mockHit;
        SearchHits mockHits = createMock(SearchHits.class);

        expect(mockClient.prepareSearch(ElasticSearchCredentialsService.INDEX_USERS)).andReturn(
                mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setQuery(anyObject(QueryBuilder.class))).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockSearchResponse);
        expect(mockSearchResponse.getHits()).andReturn(mockHits).times(2);
        expect(mockHits.getHits()).andReturn(hitArray);
        expect(mockHits.iterator()).andReturn(Arrays.asList(hitArray).iterator());
        expect(mockHit.getSourceAsString()).andReturn(mapper.writeValueAsString(u));

        replay(mockClient, mockSearchRequestBuilder, mockSearchResponse, mockFuture, mockHits, mockHit);
        List<User> users = this.credentialsService.retrieveUsers();
        verify(mockClient, mockSearchRequestBuilder, mockSearchResponse, mockFuture, mockHits, mockHit);
        assertEquals(1, users.size());
        assertEquals(u.getName(), users.get(0).getName());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRetrieveGroups() throws Exception {
        Group g = Fixtures.createGroup();
        SearchResponse mockSearchResponse = createMock(SearchResponse.class);
        SearchRequestBuilder mockSearchRequestBuilder = createMock(SearchRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        SearchHit[] hitArray = new SearchHit[1];
        SearchHit mockHit = createMock(SearchHit.class);
        hitArray[0] = mockHit;
        SearchHits mockHits = createMock(SearchHits.class);

        expect(mockClient.prepareSearch(ElasticSearchCredentialsService.INDEX_GROUPS)).andReturn(
                mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setQuery(anyObject(QueryBuilder.class))).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockSearchResponse);
        expect(mockSearchResponse.getHits()).andReturn(mockHits).times(2);
        expect(mockHits.getHits()).andReturn(hitArray);
        expect(mockHits.iterator()).andReturn(Arrays.asList(hitArray).iterator());
        expect(mockHit.getSourceAsString()).andReturn(mapper.writeValueAsString(g));

        replay(mockClient, mockSearchRequestBuilder, mockSearchResponse, mockFuture, mockHits, mockHit);
        List<Group> groups = this.credentialsService.retrieveGroups();
        verify(mockClient, mockSearchRequestBuilder, mockSearchResponse, mockFuture, mockHits, mockHit);
        assertEquals(1, groups.size());
        assertEquals(g.getName(), groups.get(0).getName());
    }
}
