package net.objecthunter.larch.service.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Version;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchVersionService;
import net.objecthunter.larch.test.util.Fixtures;

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

import javax.annotation.PostConstruct;

import java.io.ByteArrayInputStream;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

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
        ReflectionTestUtils.setField(versionService, "blobstoreService", mockBlobstoreService);
        ReflectionTestUtils.setField(versionService, "mapper", mapper);
    }

    @Test
    public void testAddOldVersion() throws Exception {
        IndexRequestBuilder mockIndexRequestBuilder = createMock(IndexRequestBuilder.class);

        /* blob creation */
        expect(mockBlobstoreService.createOldVersionBlob(anyObject(Entity.class))).andReturn("bar");

        /* index */
        expect(mockClient.prepareIndex(ElasticSearchVersionService.INDEX_VERSIONS, ElasticSearchVersionService.TYPE_VERSIONS))
                .andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.setSource((byte[]) anyObject())).andReturn(mockIndexRequestBuilder);
        expect(mockIndexRequestBuilder.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        /* index refresh */
        expect(mockClient.admin()).andReturn(mockAdminClient);
        expect(mockAdminClient.indices()).andReturn(mockIndicesAdminClient);
        expect(mockIndicesAdminClient.refresh(anyObject())).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(null);

        replay(mockIndexRequestBuilder, mockClient, mockAdminClient, mockIndicesAdminClient, mockBlobstoreService, mockFuture);
        this.versionService.addOldVersion(Fixtures.createEntity());
        verify(mockIndexRequestBuilder, mockClient, mockAdminClient, mockIndicesAdminClient, mockBlobstoreService, mockFuture);
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
        expect(mockBlobstoreService.retrieveOldVersionBlob(v.getPath())).andReturn(new ByteArrayInputStream("{}".getBytes()));

        replay(mockClient, mockSearchHits, mockSearchRequestBuilder, mockSearchResponse, mockAdminClient, mockIndicesAdminClient, mockBlobstoreService, mockHit, mockFuture);
        this.versionService.getOldVersion("foo", 1);
        verify(mockClient, mockSearchHits, mockSearchRequestBuilder, mockSearchResponse, mockAdminClient, mockIndicesAdminClient, mockBlobstoreService, mockHit, mockFuture);
    }
}