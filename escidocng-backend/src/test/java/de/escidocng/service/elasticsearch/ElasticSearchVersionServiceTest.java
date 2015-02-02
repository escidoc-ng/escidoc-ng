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
package de.escidocng.service.elasticsearch;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.ByteArrayInputStream;

import de.escidocng.test.util.Fixtures;

import org.elasticsearch.action.ListenableActionFuture;
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
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.escidocng.model.Entity;
import de.escidocng.model.Version;
import de.escidocng.service.backend.BackendBlobstoreService;
import de.escidocng.service.backend.elasticsearch.ElasticSearchVersionService;

public class ElasticSearchVersionServiceTest {

    private Client mockClient = createMock(Client.class);

    private AdminClient mockAdminClient = createMock(AdminClient.class);

    private IndicesAdminClient mockIndicesAdminClient = createMock(IndicesAdminClient.class);

    private BackendBlobstoreService mockBlobstoreService = createMock(BackendBlobstoreService.class);

    private ElasticSearchVersionService versionService = new ElasticSearchVersionService();

    private ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        ReflectionTestUtils.setField(versionService, "client", mockClient);
        ReflectionTestUtils.setField(versionService, "backendBlobstoreService", mockBlobstoreService);
        ReflectionTestUtils.setField(versionService, "mapper", mapper);
    }

    @Test
    public void testAddOldVersion() throws Exception {
        IndexRequestBuilder mockIndexRequestBuilder = createMock(IndexRequestBuilder.class);

        /* blob creation */
        expect(mockBlobstoreService.createOldVersionBlob(anyObject(Entity.class))).andReturn("bar");

        /* index */
        expect(
                mockClient.prepareIndex(ElasticSearchVersionService.INDEX_VERSIONS,
                        ElasticSearchVersionService.TYPE_VERSIONS)).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.setSource((byte[]) anyObject())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        /* index refresh */
        expect(mockClient.admin()).andReturn(mockAdminClient);
        expect(mockAdminClient.indices()).andReturn(mockIndicesAdminClient);
        expect(mockIndicesAdminClient.refresh(anyObject())).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        replay(mockIndexRequestBuilder, mockClient, mockAdminClient, mockIndicesAdminClient, mockBlobstoreService,
                mockFuture);
        this.versionService.addOldVersion(Fixtures.createEntity());
        verify(mockIndexRequestBuilder, mockClient, mockAdminClient, mockIndicesAdminClient, mockBlobstoreService,
                mockFuture);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetOldVersion() throws Exception {
        SearchRequestBuilder mockSearchRequestBuilder = createMock(SearchRequestBuilder.class);
        SearchResponse mockSearchResponse = createMock(SearchResponse.class);
        SearchHits mockSearchHits = createMock(SearchHits.class);
        SearchHit mockHit = createMock(SearchHit.class);
        SearchHit[] hitArray = new SearchHit[1];
        Version v = new Version();
        v.setEntityId("foo");
        v.setVersionNumber(1);
        v.setPath("bar");

        expect(mockClient.prepareSearch(ElasticSearchVersionService.INDEX_VERSIONS))
                .andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setQuery(anyObject(QueryBuilder.class))).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setFrom(0)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setSize(1)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockSearchResponse);
        expect(mockSearchResponse.getHits()).andReturn(mockSearchHits).times(2);
        expect(mockSearchHits.getTotalHits()).andReturn(1l);
        expect(mockSearchHits.getAt(0)).andReturn(mockHit);
        expect(mockHit.getSourceAsString()).andReturn(mapper.writeValueAsString(v));
        expect(mockBlobstoreService.retrieveOldVersionBlob(v.getPath())).andReturn(
                new ByteArrayInputStream("{}".getBytes()));

        replay(mockClient, mockSearchHits, mockSearchRequestBuilder, mockSearchResponse, mockAdminClient,
                mockIndicesAdminClient, mockBlobstoreService, mockHit, mockFuture);
        this.versionService.getOldVersion("foo", 1);
        verify(mockClient, mockSearchHits, mockSearchRequestBuilder, mockSearchResponse, mockAdminClient,
                mockIndicesAdminClient, mockBlobstoreService, mockHit, mockFuture);
    }
}
