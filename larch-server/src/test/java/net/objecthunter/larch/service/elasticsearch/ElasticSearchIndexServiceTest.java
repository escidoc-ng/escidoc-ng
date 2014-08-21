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
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.state.IndexState;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService;
import net.objecthunter.larch.test.util.Fixtures;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.admin.indices.status.DocsStatus;
import org.elasticsearch.action.admin.indices.status.IndexShardStatus;
import org.elasticsearch.action.admin.indices.status.IndexStatus;
import org.elasticsearch.action.admin.indices.status.IndicesStatusRequest;
import org.elasticsearch.action.admin.indices.status.IndicesStatusResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.index.flush.FlushStats;
import org.elasticsearch.index.merge.MergeStats;
import org.elasticsearch.index.refresh.RefreshStats;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticSearchIndexServiceTest {

    private ElasticSearchEntityService indexService;

    private Client mockClient;

    private AdminClient mockAdminClient;

    private IndicesAdminClient mockIndicesAdminClient;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        mockClient = createMock(Client.class);
        mockAdminClient = createMock(AdminClient.class);
        mockIndicesAdminClient = createMock(IndicesAdminClient.class);
        indexService = new ElasticSearchEntityService();
        ReflectionTestUtils.setField(indexService, "mapper", mapper);
        ReflectionTestUtils.setField(indexService, "client", mockClient);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreate() throws Exception {
        Entity e = Fixtures.createEntity();
        GetResponse mockGetResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        IndexRequestBuilder mockIndexRequestBuilder = createMock(IndexRequestBuilder.class);

        /* existence check */
        expect(mockClient.prepareGet(ElasticSearchEntityService.INDEX_ENTITIES,
                ElasticSearchEntityService.INDEX_ENTITY_TYPE, e.getId())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.isExists()).andReturn(false);

        /* user indexing */
        expect(mockClient.prepareIndex(ElasticSearchEntityService.INDEX_ENTITIES,
                ElasticSearchEntityService.INDEX_ENTITY_TYPE,
                e.getId())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.setSource((byte[]) anyObject())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        /* index refresh */
        expect(mockClient.admin()).andReturn(mockAdminClient);
        expect(mockAdminClient.indices()).andReturn(mockIndicesAdminClient);
        expect(mockIndicesAdminClient.refresh(anyObject())).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

//        replay(mockIndicesAdminClient, mockAdminClient, mockClient, mockGetRequestBuilder, mockFuture,
//                mockGetResponse,
//                mockIndexRequestBuilder);
//        this.indexService.create(e);
//        verify(mockIndicesAdminClient, mockAdminClient, mockClient, mockGetRequestBuilder, mockFuture,
//                mockGetResponse,
//                mockIndexRequestBuilder);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdate() throws Exception {
        Entity e = Fixtures.createEntity();
        GetResponse mockGetResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        IndexRequestBuilder mockIndexRequestBuilder = createMock(IndexRequestBuilder.class);

        expect(mockClient.prepareGet(ElasticSearchEntityService.INDEX_ENTITIES,
                ElasticSearchEntityService.INDEX_ENTITY_TYPE, e.getId())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expectLastCall();

        /* indexing */
        expect(mockClient.prepareIndex(ElasticSearchEntityService.INDEX_ENTITIES,
                ElasticSearchEntityService.INDEX_ENTITY_TYPE,
                e.getId())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.setSource((byte[]) anyObject())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        /* index refresh */
        expect(mockClient.admin()).andReturn(mockAdminClient);
        expect(mockAdminClient.indices()).andReturn(mockIndicesAdminClient);
        expect(mockIndicesAdminClient.refresh(anyObject())).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

//        replay(mockIndicesAdminClient, mockAdminClient, mockClient, mockGetRequestBuilder,
//                mockFuture, mockGetResponse, mockIndexRequestBuilder);
//        this.indexService.update(e);
//        verify(mockIndicesAdminClient, mockAdminClient, mockClient, mockGetRequestBuilder, mockFuture,
//                mockGetResponse, mockIndexRequestBuilder);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRetrieve() throws Exception {
        Entity e = Fixtures.createEntity();
        GetResponse mockGetResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        SearchResponse mockSearchResponse = createMock(SearchResponse.class);
        SearchRequestBuilder mockSearchRequestBuilder = createMock(SearchRequestBuilder.class);
        SearchHits mockHits = createMock(SearchHits.class);

        /* retrieve */
        expect(mockClient.prepareGet(ElasticSearchEntityService.INDEX_ENTITIES,
                ElasticSearchEntityService.INDEX_ENTITY_TYPE, e.getId())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.isSourceEmpty()).andReturn(false);
        expect(mockGetResponse.getSourceAsBytes()).andReturn(mapper.writeValueAsBytes(e));

        expectLastCall();

        replay(mockClient, mockHits, mockSearchRequestBuilder, mockSearchResponse,
                mockGetResponse,
                mockGetRequestBuilder, mockFuture);
        this.indexService.retrieve(e.getId());
        verify(mockClient, mockHits, mockSearchRequestBuilder, mockSearchResponse, mockGetRequestBuilder,
                mockGetResponse, mockFuture);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDelete() throws Exception {
        Entity e = Fixtures.createEntity();
        DeleteResponse mockDeleteResponse = createMock(DeleteResponse.class);
        DeleteRequestBuilder mockDeleteRequest = createMock(DeleteRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        GetResponse mockGetResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);

        // get
        expect(mockClient.prepareGet(ElasticSearchEntityService.INDEX_ENTITIES,
                ElasticSearchEntityService.INDEX_ENTITY_TYPE, e.getId())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.isSourceEmpty()).andReturn(false);
        expect(mockGetResponse.getSourceAsBytes()).andReturn(mapper.writeValueAsBytes(e));

        expectLastCall();

        expect(mockClient.prepareDelete(ElasticSearchEntityService.INDEX_ENTITIES,
                ElasticSearchEntityService.INDEX_ENTITY_TYPE, e.getId())).andReturn(mockDeleteRequest);
        expect(mockDeleteRequest.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockDeleteResponse);

        /* index refresh */
        expect(mockClient.admin()).andReturn(mockAdminClient);
        expect(mockAdminClient.indices()).andReturn(mockIndicesAdminClient);
        expect(mockIndicesAdminClient.refresh(anyObject())).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        replay(mockClient, mockAdminClient, mockIndicesAdminClient, mockDeleteRequest,
                mockDeleteResponse, mockFuture, mockGetRequestBuilder, mockGetResponse);
        this.indexService.delete(e.getId());
        verify(mockClient, mockAdminClient, mockIndicesAdminClient, mockDeleteRequest, mockDeleteResponse, mockFuture);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStatus() throws Exception {
        IndicesStatsResponse mockStats = createMock(IndicesStatsResponse.class);
        IndicesStatusResponse mockStatusResponse = createMock(IndicesStatusResponse.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        IndexStatus mockIndexStatus = createMock(IndexStatus.class);
        ByteSizeValue mockByteSize = createMock(ByteSizeValue.class);
        IndexShardStatus mockShardStatus = createMock(IndexShardStatus.class);
        DocsStatus mockDocStatus = createMock(DocsStatus.class);
        FlushStats mockFlushStats = createMock(FlushStats.class);
        MergeStats mockMergeStats = createMock(MergeStats.class);
        RefreshStats mockRefreshStats = createMock(RefreshStats.class);

        Map<String, IndexStatus> indexStates = new HashMap<>();
        indexStates.put(ElasticSearchEntityService.INDEX_ENTITIES, mockIndexStatus);
        Map<Integer, IndexShardStatus> shardStates = new HashMap<>();
        shardStates.put(0, mockShardStatus);

        expect(mockClient.admin()).andReturn(mockAdminClient);
        expect(mockAdminClient.indices()).andReturn(mockIndicesAdminClient);
        expect(mockIndicesAdminClient.status(anyObject(IndicesStatusRequest.class))).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockStatusResponse);
        expect(mockStatusResponse.getIndices()).andReturn(indexStates);
        expect(mockIndexStatus.getStoreSize()).andReturn(mockByteSize);
        expect(mockByteSize.getBytes()).andReturn(0l);
        expect(mockIndexStatus.getShards()).andReturn(shardStates);
        expect(mockIndexStatus.getDocs()).andReturn(mockDocStatus).times(2);
        expect(mockDocStatus.getNumDocs()).andReturn(0l);
        expect(mockDocStatus.getMaxDoc()).andReturn(0l);
        expect(mockIndexStatus.getFlushStats()).andReturn(mockFlushStats);
        expect(mockFlushStats.getTotalTimeInMillis()).andReturn(0l);
        expect(mockIndexStatus.getMergeStats()).andReturn(mockMergeStats).times(3);
        expect(mockMergeStats.getTotalTimeInMillis()).andReturn(0l);
        expect(mockMergeStats.getCurrentNumDocs()).andReturn(0l);
        expect(mockMergeStats.getTotalSizeInBytes()).andReturn(0l);
        expect(mockIndexStatus.getRefreshStats()).andReturn(mockRefreshStats);
        expect(mockRefreshStats.getTotalTimeInMillis()).andReturn(0l);

        replay(mockClient, mockAdminClient, mockIndicesAdminClient, mockStats, mockFuture, mockIndexStatus,
                mockStatusResponse, mockByteSize, mockDocStatus, mockFlushStats, mockMergeStats, mockRefreshStats);
        IndexState state = this.indexService.status();
        verify(mockClient, mockAdminClient, mockIndicesAdminClient, mockStats, mockFuture, mockIndexStatus,
                mockStatusResponse, mockByteSize, mockDocStatus, mockFlushStats, mockMergeStats, mockRefreshStats);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExists() throws Exception {
        Entity e = Fixtures.createEntity();
        GetResponse mockGetResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);

        /* existence check */
        expect(mockClient.prepareGet(ElasticSearchEntityService.INDEX_ENTITIES,
                ElasticSearchEntityService.INDEX_ENTITY_TYPE, e.getId())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.isExists()).andReturn(true);

        replay(mockClient, mockGetResponse, mockGetRequestBuilder, mockFuture);
        assertTrue(this.indexService.exists(e.getId()));
        verify(mockClient, mockGetResponse, mockGetRequestBuilder, mockFuture);
    }
}
