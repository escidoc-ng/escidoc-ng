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

import net.objecthunter.larch.model.SearchResult;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.easymock.EasyMock.*;

public class ElasticSearchSearchServiceTest {
    private ElasticSearchSearchService searchService;

    private Client mockClient;
    private AdminClient mockAdminClient;
    private IndicesAdminClient mockIndicesAdminClient;


    @Before
    public void setup() {
        searchService = new ElasticSearchSearchService();
        mockClient = createMock(Client.class);
        mockAdminClient = createMock(AdminClient.class);
        mockIndicesAdminClient = createMock(IndicesAdminClient.class);
        ReflectionTestUtils.setField(searchService, "client", mockClient);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testScanIndexWithNumRecords() throws Exception {
        SearchResponse mockSearchResponse = createMock(SearchResponse.class);
        SearchRequestBuilder mockSearchRequestBuilder = createMock(SearchRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        SearchHit[] hitArray = new SearchHit[1];
        SearchHit mockHit = createMock(SearchHit.class);
        hitArray[0] = mockHit;
        SearchHitField mockField = createMock(SearchHitField.class);
        SearchHits mockHits = createMock(SearchHits.class);

        expect(mockClient.prepareSearch(ElasticSearchIndexService.INDEX_ENTITIES)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setQuery(anyObject(QueryBuilder.class))).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setFrom(0)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setSize(10)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.addFields("id", "label", "type", "tags")).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockSearchResponse);
        expect(mockSearchResponse.getHits()).andReturn(mockHits).times(3);
        expect(mockHits.getHits()).andReturn(hitArray);
        expect(mockHits.getTotalHits()).andReturn((long) hitArray.length);
        expect(mockHits.iterator()).andReturn(Arrays.asList(hitArray).iterator());
        expect(mockHit.field("label")).andReturn(mockField).times(2);
        expect(mockField.getValue()).andReturn("test label");
        expect(mockHit.field("type")).andReturn(mockField).times(2);
        expect(mockField.getValue()).andReturn("test type");
        expect(mockHit.field("id")).andReturn(mockField);
        expect(mockField.getValue()).andReturn("testid");
        expect(mockHit.field("tags")).andReturn(mockField).times(2);
        expect(mockField.values()).andReturn(Arrays.asList("testtag1", "testtag2"));

        replay(mockClient, mockSearchRequestBuilder, mockFuture, mockSearchResponse, mockHits, mockHit, mockField);
        SearchResult result = searchService.scanIndex(0, 10);
        verify(mockClient, mockSearchRequestBuilder, mockFuture, mockSearchResponse, mockHits, mockHit, mockField);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSearchEntities() throws Exception {
        SearchResponse mockSearchResponse = createMock(SearchResponse.class);
        SearchRequestBuilder mockSearchRequestBuilder = createMock(SearchRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        SearchHit[] hitArray = new SearchHit[1];
        SearchHit mockHit = createMock(SearchHit.class);
        hitArray[0] = mockHit;
        SearchHitField mockField = createMock(SearchHitField.class);
        SearchHits mockHits = createMock(SearchHits.class);

        /* index refresh */
        expect(mockClient.admin()).andReturn(mockAdminClient);
        expect(mockAdminClient.indices()).andReturn(mockIndicesAdminClient);
        expect(mockIndicesAdminClient.refresh(anyObject())).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        expect(mockClient.prepareSearch(ElasticSearchIndexService.INDEX_ENTITIES)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setQuery(anyObject(QueryBuilder.class))).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.addFields("id", "label", "type", "tags")).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockSearchResponse);
        expect(mockSearchResponse.getHits()).andReturn(mockHits).times(5);
        expect(mockHits.getHits()).andReturn(hitArray).times(2);
        expect(mockHits.iterator()).andReturn(Arrays.asList(hitArray).iterator());
        expect(mockHit.field("label")).andReturn(mockField).times(2);
        expect(mockField.getValue()).andReturn("test label");
        expect(mockHit.field("type")).andReturn(mockField).times(2);
        expect(mockField.getValue()).andReturn("test type");
        expect(mockHit.field("id")).andReturn(mockField);
        expect(mockField.getValue()).andReturn("testid");
        expect(mockHit.field("tags")).andReturn(mockField).times(2);
        expect(mockField.values()).andReturn(Arrays.asList("testtag1", "testtag2"));
        expect(mockHits.getTotalHits()).andReturn(1l).times(2);

        replay(mockClient, mockAdminClient, mockIndicesAdminClient, mockSearchRequestBuilder, mockFuture,
                mockSearchResponse, mockHits, mockHit, mockField);
        SearchResult result = searchService.searchEntities("*");
        verify(mockClient, mockAdminClient, mockIndicesAdminClient, mockSearchRequestBuilder, mockFuture,
                mockSearchResponse, mockHits, mockHit, mockField);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testScanIndexDefaultNumRecords() throws Exception {
        SearchResponse mockSearchResponse = createMock(SearchResponse.class);
        SearchRequestBuilder mockSearchRequestBuilder = createMock(SearchRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        SearchHit[] hitArray = new SearchHit[1];
        SearchHit mockHit = createMock(SearchHit.class);
        hitArray[0] = mockHit;
        SearchHitField mockField = createMock(SearchHitField.class);
        SearchHits mockHits = createMock(SearchHits.class);

        expect(mockClient.prepareSearch(ElasticSearchIndexService.INDEX_ENTITIES)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setQuery(anyObject(QueryBuilder.class))).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setFrom(0)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setSize(50)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.addFields("id", "label", "type", "tags")).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockSearchResponse);
        expect(mockSearchResponse.getHits()).andReturn(mockHits).times(3);
        expect(mockHits.getHits()).andReturn(hitArray);
        expect(mockHits.getTotalHits()).andReturn((long) hitArray.length);
        expect(mockHits.iterator()).andReturn(Arrays.asList(hitArray).iterator());
        expect(mockHit.field("label")).andReturn(mockField).times(2);
        expect(mockField.getValue()).andReturn("test label");
        expect(mockHit.field("type")).andReturn(mockField).times(2);
        expect(mockField.getValue()).andReturn("test type");
        expect(mockHit.field("id")).andReturn(mockField);
        expect(mockField.getValue()).andReturn("testid");
        expect(mockHit.field("tags")).andReturn(mockField).times(2);
        expect(mockField.values()).andReturn(Arrays.asList("testtag1", "testtag2"));

        replay(mockClient, mockSearchRequestBuilder, mockFuture, mockSearchResponse, mockHits, mockHit, mockField);
        SearchResult result = searchService.scanIndex(0);
        verify(mockClient, mockSearchRequestBuilder, mockFuture, mockSearchResponse, mockHits, mockHit, mockField);
    }
}
