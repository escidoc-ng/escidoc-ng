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
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import net.objecthunter.larch.helpers.AuditRecords;
import net.objecthunter.larch.model.AuditRecord;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchAuditService;

import org.easymock.EasyMock;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticSearchAuditServiceTest {

    private ElasticSearchAuditService auditService;

    private Client mockClient;

    private AdminClient mockAdminClient;

    private IndicesAdminClient mockIndicesAdminClient;

    @Before
    public void setup() {
        mockClient = createMock(Client.class);
        mockAdminClient = createMock(AdminClient.class);
        mockIndicesAdminClient = createMock(IndicesAdminClient.class);
        auditService = new ElasticSearchAuditService();
        ReflectionTestUtils.setField(auditService, "client", mockClient);
        ReflectionTestUtils.setField(auditService, "mapper", new ObjectMapper());
        User u = new User();
        u.setName("test");
        u.setPwhash("test");
        Authentication auth = new UsernamePasswordAuthenticationToken(u, "test");

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRetrieve() throws Exception {
        SearchRequestBuilder mockSearchRequestBuilder = createMock(SearchRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        SearchResponse mockResponse = createMock(SearchResponse.class);
        SearchHit[] hitArray = new SearchHit[1];
        SearchHit hitMock = createMock(SearchHit.class);
        hitArray[0] = hitMock;
        SearchHits mockHits = createMock(SearchHits.class);

        expect(mockClient.prepareSearch(ElasticSearchAuditService.INDEX_AUDIT)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setQuery((MatchQueryBuilder) EasyMock.anyObject())).andReturn(
                mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)).andReturn(
                mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setFrom(0)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setSize(0)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.addSort("timestamp", SortOrder.ASC)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockResponse);
        expect(mockResponse.getHits()).andReturn(mockHits);
        expect(mockHits.iterator()).andReturn(Arrays.asList(hitArray).iterator());
        String json = new ObjectMapper().writeValueAsString(AuditRecords.createEntityRecord("id"));
        expect(hitMock.getSourceAsString()).andReturn(json);

        replay(mockClient, mockSearchRequestBuilder, mockFuture, mockHits, mockResponse, hitMock);
        List<AuditRecord> records = this.auditService.retrieve("id", 0, 0);
        verify(mockClient, mockSearchRequestBuilder, mockFuture, mockHits, mockResponse, hitMock);

        assertEquals(1, records.size());
        assertEquals("id", records.get(0).getEntityId());
        assertEquals(AuditRecord.EVENT_CREATE_ENTITY, records.get(0).getAction());
        assertEquals("test", records.get(0).getAgentName());
        assertNotNull(records.get(0).getTimestamp());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreate() throws Exception {
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        GetResponse mockResponse = createMock(GetResponse.class);
        IndexRequestBuilder mockIndexRequestBuilder = createMock(IndexRequestBuilder.class);

        expect(mockClient.prepareGet(eq(ElasticSearchAuditService.INDEX_AUDIT), isNull(), anyString())).andReturn(
                mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockResponse);
        expect(mockResponse.isExists()).andReturn(false);

        expect(mockClient.prepareIndex(eq(ElasticSearchAuditService.INDEX_AUDIT), eq("audit"), anyString()))
                .andReturn(
                    mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.setSource((byte[]) anyObject())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        /* index refresh */
        expect(mockClient.admin()).andReturn(mockAdminClient);
        expect(mockAdminClient.indices()).andReturn(mockIndicesAdminClient);
        expect(mockIndicesAdminClient.refresh(anyObject())).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        replay(mockIndicesAdminClient, mockAdminClient, mockClient, mockGetRequestBuilder, mockFuture, mockResponse, mockIndexRequestBuilder);
        auditService.create(AuditRecords.createEntityRecord("id"));
        verify(mockIndicesAdminClient, mockAdminClient, mockClient, mockGetRequestBuilder, mockFuture, mockResponse, mockIndexRequestBuilder);
    }
}
