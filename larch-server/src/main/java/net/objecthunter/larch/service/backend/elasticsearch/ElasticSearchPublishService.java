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

package net.objecthunter.larch.service.backend.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.model.Entities;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.service.backend.BackendPublishService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticSearchPublishService extends AbstractElasticSearchService implements BackendPublishService {

    private static final Logger log = LoggerFactory.getLogger(ElasticSearchPublishService.class);

    public static final String INDEX_PUBLISHED = "publish";

    public static final String TYPE_PUBLISHED = "publishedentity";

    private int maxRecords = 50;

    @Autowired
    private Client client;

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    public void init() throws IOException {
        this.checkAndOrCreateIndex(INDEX_PUBLISHED);
    }

    @Override
    public String publish(Entity e) throws IOException {
        String publishId = new StringBuilder(e.getId()).append(":").append(e.getVersion()).toString();
        e.setPublishId(publishId);
        try {
            this.client
                    .prepareIndex(INDEX_PUBLISHED, TYPE_PUBLISHED, publishId).setSource(
                            this.mapper.writeValueAsBytes(e))
                    .execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        this.refreshIndex(INDEX_PUBLISHED);
        return publishId;
    }

    @Override
    public Entity retrievePublishedEntity(String publishId) throws IOException {
        final GetResponse resp;
        try {
            resp =
                    this.client.prepareGet(INDEX_PUBLISHED, TYPE_PUBLISHED, publishId).execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        if (!resp.isExists()) {
            throw new NotFoundException("The entity with the publishId " + publishId
                    + " can not be found in the publish index");
        }
        return this.mapper.readValue(resp.getSourceAsBytes(), Entity.class);
    }

    @Override
    public Entities retrievePublishedEntities(String entityId) throws IOException {
        final SearchResponse search;
        try {
            search =
                    this.client
                            .prepareSearch(INDEX_PUBLISHED)
                            .setTypes(TYPE_PUBLISHED)
                            .setQuery(
                                    QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
                                            FilterBuilders.termFilter("id", entityId))).addSort(
                                    SortBuilders.fieldSort("publishId").ignoreUnmapped(true).order(SortOrder.ASC))
                            .execute()
                            .actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        if (search.getHits().getTotalHits() == 0) {
            throw new NotFoundException("There are no published versions of the entity " + entityId);
        }
        final List<Entity> result = new ArrayList<>();
        for (SearchHit hit : search.getHits().getHits()) {
            result.add(this.mapper.readValue(hit.getSourceAsString(), Entity.class));
        }
        Entities entities = new Entities();
        entities.setEntities(result);
        return entities;
    }

    @Override
    public SearchResult scanIndex(int offset) throws IOException {
        return scanIndex(offset, maxRecords);
    }

    @Override
    public SearchResult scanIndex(int offset, int numRecords) throws IOException {
        final long time = System.currentTimeMillis();
        numRecords = numRecords > maxRecords ? maxRecords : numRecords;
        final SearchResponse resp;
        try {
            resp =
                    this.client
                            .prepareSearch(INDEX_PUBLISHED).setQuery(QueryBuilders.matchAllQuery())
                            .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setFrom(offset).setSize(numRecords)
                            .addFields("id", "workspaceId", "publishId", "version", "label", "type", "tags")
                            .execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }

        final SearchResult result = new SearchResult();
        result.setOffset(offset);
        result.setNumRecords(numRecords);
        result.setHits(resp.getHits().getHits().length);
        result.setTotalHits(resp.getHits().getTotalHits());
        result.setOffset(offset);
        result.setNextOffset(offset + numRecords);
        result.setPrevOffset(Math.max(offset - numRecords, 0));
        result.setMaxRecords(maxRecords);

        final List<Entity> entites = new ArrayList<>(numRecords);
        for (final SearchHit hit : resp.getHits()) {
            // TODO: check if JSON document is prefetched or laziliy initialised
            int version = hit.field("version") != null ? hit.field("version").getValue() : 0;
            String workspaceId = hit.field("workspaceId") != null ? hit.field("workspaceId").getValue() : null;
            String label = hit.field("label") != null ? hit.field("label").getValue() : "";
            String type = hit.field("type") != null ? hit.field("type").getValue() : "";
            final Entity e = new Entity();
            e.setId(hit.field("id").getValue());
            e.setWorkspaceId(workspaceId);
            e.setPublishId(hit.field("publishId").getValue());
            e.setVersion(version);
            e.setLabel(label);
            e.setType(type);
            List<String> tags = new ArrayList<>();
            if (hit.field("tags") != null) {
                for (Object o : hit.field("tags").values()) {
                    tags.add((String) o);
                }
            }
            e.setTags(tags);
            entites.add(e);
        }

        result.setData(entites);
        result.setDuration(System.currentTimeMillis() - time);
        return result;
    }

    @Override
    public SearchResult scanWorkspace(String workspaceId, int offset) throws IOException {
        return scanWorkspace(workspaceId, offset, maxRecords);
    }

    @Override
    public SearchResult scanWorkspace(String workspaceId, int offset, int numRecords) throws IOException {
        final long time = System.currentTimeMillis();
        numRecords = numRecords > maxRecords || numRecords < 1 ? maxRecords : numRecords;
        final SearchResponse resp;
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.matchQuery("workspaceId", workspaceId));
        queryBuilder.must(getEntitiesUserRestrictionQuery(workspaceId));
        try {
            resp =
                    this.client
                            .prepareSearch(INDEX_PUBLISHED).setQuery(
                                    queryBuilder)
                            .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setFrom(offset).setSize(numRecords)
                            .addFields("id", "publishId", "version", "label", "type", "tags", "state").execute()
                            .actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }

        final SearchResult result = new SearchResult();
        result.setOffset(offset);
        result.setNumRecords(numRecords);
        result.setHits(resp.getHits().getHits().length);
        result.setTotalHits(resp.getHits().getTotalHits());
        result.setOffset(offset);
        result.setNextOffset(offset + numRecords);
        result.setPrevOffset(Math.max(offset - numRecords, 0));
        result.setMaxRecords(maxRecords);

        final List<Entity> entites = new ArrayList<>(numRecords);
        for (final SearchHit hit : resp.getHits()) {
            // TODO: check if JSON docuemnt is prefetched or laziliy initialised
            int version = hit.field("version") != null ? hit.field("version").getValue() : 0;
            String label = hit.field("label") != null ? hit.field("label").getValue() : "";
            String type = hit.field("type") != null ? hit.field("type").getValue() : "";
            String state = hit.field("state") != null ? hit.field("state").getValue() : "";
            final Entity e = new Entity();
            e.setId(hit.field("id").getValue());
            e.setWorkspaceId(workspaceId);
            e.setPublishId(hit.field("publishId").getValue());
            e.setVersion(version);
            e.setLabel(label);
            e.setType(type);
            e.setState(state);
            List<String> tags = new ArrayList<>();
            if (hit.field("tags") != null) {
                for (Object o : hit.field("tags").values()) {
                    tags.add((String) o);
                }
            }
            e.setTags(tags);
            entites.add(e);
        }

        result.setData(entites);
        result.setDuration(System.currentTimeMillis() - time);
        return result;
    }

    @Override
    public SearchResult searchEntities(Map<EntitiesSearchField, String[]> searchFields) throws IOException {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        for (Entry<EntitiesSearchField, String[]> searchField : searchFields.entrySet()) {
            if (searchField.getValue() != null && searchField.getValue().length > 0) {
                BoolQueryBuilder childQueryBuilder = QueryBuilders.boolQuery();
                for (int i = 0; i < searchField.getValue().length; i++) {
                    if (StringUtils.isNotBlank(searchField.getValue()[i])) {
                        String value = searchField.getValue()[i].toLowerCase();
                        if (searchField.getKey().getFieldName().equals("workspaceId") ||
                                searchField.getKey().getFieldName().equals("parentId")) {
                            value = searchField.getValue()[i];
                        }
                        childQueryBuilder.should(QueryBuilders.wildcardQuery(searchField.getKey().getFieldName(),
                                value));
                    }
                }
                queryBuilder.must(childQueryBuilder);
            }
        }

        int numRecords = 20;
        final long time = System.currentTimeMillis();
        final ActionFuture<RefreshResponse> refresh =
                this.client.admin().indices().refresh(new RefreshRequest(INDEX_PUBLISHED));
        final SearchResponse resp;
        try {
            refresh.actionGet();
            resp =
                    this.client
                            .prepareSearch(INDEX_PUBLISHED).addFields("id", "publishId", "version", "label", "type",
                                    "tags")
                            .setQuery(queryBuilder).setSearchType(SearchType.DFS_QUERY_THEN_FETCH).execute()
                            .actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        log.debug("ES returned {} results for '{}'", resp.getHits().getHits().length, new String(queryBuilder
                .buildAsBytes().toBytes()));
        final SearchResult result = new SearchResult();

        final List<Entity> entities = new ArrayList<>();
        for (final SearchHit hit : resp.getHits()) {
            int version = hit.field("version") != null ? hit.field("version").getValue() : 0;
            String label = hit.field("label") != null ? hit.field("label").getValue() : "";
            String type = hit.field("type") != null ? hit.field("type").getValue() : "";
            final Entity e = new Entity();
            e.setId(hit.field("id").getValue());
            e.setPublishId(hit.field("publishId").getValue());
            e.setVersion(version);
            e.setType(type);
            e.setLabel(label);

            final List<String> tags = new ArrayList<>();
            if (hit.field("tags") != null) {
                for (Object o : hit.field("tags").values()) {
                    tags.add((String) o);
                }
            }
            e.setTags(tags);
            entities.add(e);
        }
        result.setData(entities);
        result.setTotalHits(resp.getHits().getTotalHits());
        result.setMaxRecords(maxRecords);
        result.setHits(resp.getHits().getHits().length);
        result.setNumRecords(numRecords);
        result.setOffset(0);
        result.setTerm(new String(queryBuilder.buildAsBytes().toBytes()));
        result.setPrevOffset(0);
        result.setNextOffset(0);
        result.setTotalHits(resp.getHits().getTotalHits());
        result.setDuration(System.currentTimeMillis() - time);
        return result;
    }

}
