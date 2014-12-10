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

package net.objecthunter.larch.service.elasticsearch;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import net.objecthunter.larch.model.MetadataType;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchSchemaService;
import net.objecthunter.larch.test.util.Fixtures;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticSearchSchemaServiceTest {

    private ElasticSearchSchemaService schemaService;

    private ObjectMapper mapper = new ObjectMapper();

    private Client mockClient;

    private AdminClient mockAdminClient;

    private IndicesAdminClient mockIndicesAdminClient;

    @Before
    public void setup() {
        mockClient = createMock(Client.class);
        mockAdminClient = createMock(AdminClient.class);
        mockIndicesAdminClient = createMock(IndicesAdminClient.class);
        schemaService = new ElasticSearchSchemaService();
        ReflectionTestUtils.setField(schemaService, "mapper", mapper);
        ReflectionTestUtils.setField(schemaService, "client", mockClient);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetSchemUrlForType() throws Exception {
        MetadataType type = Fixtures.createMetadataType();
        GetRequestBuilder mockGetRequest = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        GetResponse mockGetResponse = createMock(GetResponse.class);

        expect(mockClient.prepareGet(ElasticSearchSchemaService.INDEX_MD_SCHEMATA,
                ElasticSearchSchemaService.INDEX_MD_SCHEMATA_TYPE, type.getName())).andReturn(mockGetRequest);
        expect(mockGetRequest.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.isExists()).andReturn(true);
        expect(mockGetResponse.getSourceAsBytes()).andReturn(mapper.writeValueAsBytes(type));

        replay(mockClient, mockFuture, mockGetRequest, mockGetResponse);
        String url = schemaService.getSchemUrlForType(type.getName());
        verify(mockClient, mockFuture, mockGetRequest, mockGetResponse);

        assertEquals(type.getSchemaUrl(), url);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetSchemaTypes() throws Exception {
        MetadataType type = Fixtures.createMetadataType();
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        SearchResponse mockSearchResponse = createMock(SearchResponse.class);
        SearchRequestBuilder mockSearchRequestBuilder = createMock(SearchRequestBuilder.class);
        SearchHit[] hitArray = new SearchHit[1];
        SearchHit mockHit = createMock(SearchHit.class);
        hitArray[0] = mockHit;
        SearchHits mockHits = createMock(SearchHits.class);

        expect(mockClient.prepareSearch(ElasticSearchSchemaService.INDEX_MD_SCHEMATA)).andReturn(
                mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setSearchType(anyObject(SearchType.class))).andReturn(
                mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setQuery(anyObject(QueryBuilder.class))).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.setSize(128)).andReturn(mockSearchRequestBuilder);
        expect(mockSearchRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockSearchResponse);
        expect(mockSearchResponse.getHits()).andReturn(mockHits);
        expect(mockHits.getHits()).andReturn(hitArray);
        expect(mockSearchResponse.getHits()).andReturn(mockHits);
        expect(mockHits.iterator()).andReturn(Arrays.asList(hitArray).iterator());
        expect(hitArray[0].getSourceAsString()).andReturn(mapper.writeValueAsString(type));

        replay(mockClient, mockFuture, mockSearchRequestBuilder, mockSearchResponse, mockHits, mockHit);
        List<MetadataType> types = this.schemaService.getSchemaTypes();
        verify(mockClient, mockFuture, mockSearchRequestBuilder, mockSearchResponse, mockHits, mockHit);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateSchemaType() throws Exception {
        MetadataType type = Fixtures.createMetadataType();
        GetResponse mockGetResponse = createMock(GetResponse.class);
        GetRequestBuilder mockGetRequestBuilder = createMock(GetRequestBuilder.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        IndexRequestBuilder mockIndexRequestBuilder = createMock(IndexRequestBuilder.class);
        IndexResponse mockIndexResponse = createMock(IndexResponse.class);

        /* existence check */
        expect(mockClient.prepareGet(ElasticSearchSchemaService.INDEX_MD_SCHEMATA,
                ElasticSearchSchemaService.INDEX_MD_SCHEMATA_TYPE, type.getName())).andReturn(mockGetRequestBuilder);
        expect(mockGetRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockGetResponse);
        expect(mockGetResponse.isExists()).andReturn(false);

        /* indexing */
        expect(mockClient.prepareIndex(ElasticSearchSchemaService.INDEX_MD_SCHEMATA,
                ElasticSearchSchemaService.INDEX_MD_SCHEMATA_TYPE,
                type.getName())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.setSource((byte[]) anyObject())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockIndexResponse);
        expect(mockIndexResponse.getId()).andReturn(type.getName());

        /* index refresh */
        expect(mockClient.admin()).andReturn(mockAdminClient);
        expect(mockAdminClient.indices()).andReturn(mockIndicesAdminClient);
        expect(mockIndicesAdminClient.refresh(anyObject())).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        replay(mockClient, mockAdminClient, mockIndicesAdminClient, mockFuture, mockGetRequestBuilder,
                mockGetResponse,
                mockIndexRequestBuilder, mockIndexResponse);
        assertNotNull(this.schemaService.createSchemaType(type));
        verify(mockClient, mockAdminClient, mockIndicesAdminClient, mockFuture, mockGetRequestBuilder,
                mockGetResponse, mockIndexRequestBuilder, mockIndexResponse);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteMetadataType() throws Exception {
        MetadataType type = Fixtures.createMetadataType();
        CountRequestBuilder mockCountRequest = createMock(CountRequestBuilder.class);
        CountResponse mockCountResponse = createMock(CountResponse.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        DeleteResponse mockDeleteResponse = createMock(DeleteResponse.class);
        DeleteRequestBuilder mockDeleteRequest = createMock(DeleteRequestBuilder.class);

        expect(mockClient.prepareCount(ElasticSearchEntityService.INDEX_ENTITIES)).andReturn(mockCountRequest);
        expect(mockCountRequest.setQuery(anyObject(QueryBuilder.class))).andReturn(mockCountRequest);
        expect(mockCountRequest.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockCountResponse);
        expect(mockCountResponse.getCount()).andReturn(0l);

        expect(mockClient.prepareDelete(ElasticSearchSchemaService.INDEX_MD_SCHEMATA,
                ElasticSearchSchemaService.INDEX_MD_SCHEMATA_TYPE, type.getName())).andReturn(mockDeleteRequest);
        expect(mockDeleteRequest.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockDeleteResponse);

        replay(mockClient, mockDeleteRequest, mockDeleteResponse, mockCountRequest, mockCountResponse, mockFuture);
        this.schemaService.deleteSchemaType(type.getName());
        verify(mockClient, mockDeleteRequest, mockDeleteResponse, mockCountRequest, mockCountResponse, mockFuture);
    }

    @Test
    @Ignore
    public void testValidate() throws Exception {
        // TODO implement validation test
    }
}
