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

import net.objecthunter.larch.exceptions.AlreadyExistsException;
import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.model.state.IndexState;
import net.objecthunter.larch.service.backend.BackendEntityService;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.admin.indices.status.IndexStatus;
import org.elasticsearch.action.admin.indices.status.IndicesStatusRequest;
import org.elasticsearch.action.admin.indices.status.IndicesStatusResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An {@link net.objecthunter.larch.service.backend.BackendEntityService} implementation built on top of
 * ElasticSearch.
 */
public class ElasticSearchEntityService extends AbstractElasticSearchService implements BackendEntityService {

    public static final String INDEX_ENTITIES = "entities";

    public static final String INDEX_ENTITY_TYPE = "entity";

    private int maxRecords = 50;

    private static final Logger log = LoggerFactory.getLogger(ElasticSearchEntityService.class);

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    public void init() throws IOException {
        log.debug("initialising ElasticSearchEntityService");
        this.checkAndOrCreateIndex(INDEX_ENTITIES);
        this.waitForIndex(INDEX_ENTITIES);
    }

    @Override
    public String create(Entity e) throws IOException {
        log.debug("creating new entity");
        if (e.getId() != null) {
            final GetResponse resp =
                    client.prepareGet(INDEX_ENTITIES, INDEX_ENTITY_TYPE, e.getId()).execute().actionGet();
            if (resp.isExists()) {
                throw new AlreadyExistsException("Entity with id " + e.getId() + " already exists");
            }
        }
        try {
            client
                    .prepareIndex(INDEX_ENTITIES, INDEX_ENTITY_TYPE, e.getId()).setSource(
                            mapper.writeValueAsBytes(e))
                    .execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        refreshIndex(INDEX_ENTITIES);
        return e.getId();
    }

    @Override
    public void update(Entity e) throws IOException {
        log.debug("updating entity " + e.getId());
        /* and create the updated document */
        try {
            client
                    .prepareIndex(INDEX_ENTITIES, INDEX_ENTITY_TYPE, e.getId()).setSource(
                            mapper.writeValueAsBytes(e))
                    .execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        /* refresh the index before returning */
        refreshIndex(INDEX_ENTITIES);
    }

    @Override
    public Entity retrieve(String id) throws IOException {
        log.debug("fetching entity " + id);
        final GetResponse resp;
        try {
            resp = client.prepareGet(INDEX_ENTITIES, INDEX_ENTITY_TYPE, id).execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        if (resp.isSourceEmpty()) {
            throw new NotFoundException("entity with id " + id + " not found");
        }
        final Entity parent = mapper.readValue(resp.getSourceAsBytes(), Entity.class);
        parent.setChildren(fetchChildren(id));
        return parent;
    }

    private List<String> fetchChildren(String id) throws IOException {
        final List<String> children = new ArrayList<>();
        SearchResponse search;
        int offset = 0;
        int max = 64;
        try {
            do {
                search =
                        client
                                .prepareSearch(INDEX_ENTITIES)
                                .setTypes(INDEX_ENTITY_TYPE)
                                .setQuery(
                                        QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
                                                FilterBuilders
                                                        .termFilter("parentId", id))).setFrom(offset).setSize(max)
                                .execute()
                                .actionGet();
                if (search.getHits().getHits().length > 0) {
                    for (SearchHit hit : search.getHits().getHits()) {
                        children.add(hit.getId());
                    }
                }
                offset = offset + max;
            } while (offset < search.getHits().getTotalHits());
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        return children;
    }

    @Override
    public void delete(String id) throws IOException {
        log.debug("deleting entity " + id);
        try {
            client.prepareDelete(INDEX_ENTITIES, INDEX_ENTITY_TYPE, id).execute().actionGet();
            this.client.admin().indices().refresh(new RefreshRequest(("groven"))).actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public IndexState status() throws IOException {
        final IndicesStatusResponse resp;
        final IndexStatus esState;
        try {
            resp =
                    client.admin().indices().status(new IndicesStatusRequest(INDEX_ENTITIES)).actionGet();
            esState = resp.getIndices().get(INDEX_ENTITIES);
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }

        final IndexState state = new IndexState();
        state.setName(INDEX_ENTITIES);
        state.setStoreSize(esState.getStoreSize().getBytes());
        state.setShardsSize(esState.getShards().size());
        state.setNumDocs(esState.getDocs().getNumDocs());
        state.setMaxDocs(esState.getDocs().getMaxDoc());
        state.setTotalFlushTime(esState.getFlushStats().getTotalTimeInMillis());
        state.setTotalMergeTime(esState.getMergeStats().getTotalTimeInMillis());
        state.setNumDocsToMerge(esState.getMergeStats().getCurrentNumDocs());
        state.setSizeToMerge(esState.getMergeStats().getTotalSizeInBytes());
        state.setTotalRefreshTime(esState.getRefreshStats().getTotalTimeInMillis());
        return state;
    }

    @Override
    public boolean exists(String id) throws IOException {
        try {
            return client.prepareGet(INDEX_ENTITIES, INDEX_ENTITY_TYPE, id).execute().actionGet().isExists();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public SearchResult scanIndex(int offset, int numRecords) throws IOException {
        final long time = System.currentTimeMillis();
        numRecords = numRecords > maxRecords ? maxRecords : numRecords;
        final SearchResponse resp;
        try {
            resp =
                    this.client
                            .prepareSearch(ElasticSearchEntityService.INDEX_ENTITIES).setQuery(
                                    QueryBuilders.matchAllQuery())
                            .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setFrom(offset).setSize(numRecords)
                            .addFields("id", "label", "type", "tags", "state").execute().actionGet();
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
            String label = hit.field("label") != null ? hit.field("label").getValue() : "";
            String type = hit.field("type") != null ? hit.field("type").getValue() : "";
            String state = hit.field("state") != null ? hit.field("state").getValue() : "";
            final Entity e = new Entity();
            e.setId(hit.field("id").getValue());
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
                        childQueryBuilder.should(QueryBuilders.wildcardQuery(searchField.getKey().getFieldName(),
                                searchField.getValue()[i].toLowerCase()));
                    }
                }
                queryBuilder.must(childQueryBuilder);
            }
        }

        int numRecords = 20;
        final long time = System.currentTimeMillis();
        final ActionFuture<RefreshResponse> refresh =
                this.client.admin().indices().refresh(new RefreshRequest(ElasticSearchEntityService.INDEX_ENTITIES));
        final SearchResponse resp;
        try {
            refresh.actionGet();

            resp =
                    this.client
                            .prepareSearch(ElasticSearchEntityService.INDEX_ENTITIES).addFields("id", "label",
                                    "type",
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
            String label = hit.field("label") != null ? hit.field("label").getValue() : "";
            String type = hit.field("type") != null ? hit.field("type").getValue() : "";
            final Entity e = new Entity();
            e.setId(hit.field("id").getValue());
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

    @Override
    public SearchResult scanIndex(int offset) throws IOException {
        return scanIndex(offset, maxRecords);
    }

    @Override
    public String createWorkspace(Workspace workspace) {
        return null;
    }

    @Override
    public Workspace retrieveWorkspace(String id) {
        return null;
    }

    @Override
    public void updateWorkspace(Workspace workspace) {

    }

    @Override
    public void patchWorkSpace(Workspace workspace) {

    }

    /**
     * Holds enabled search-fields in entities-index. Differentiate between name of GET/POST-Parameter and name of
     * Search-Field in index.
     * 
     * @author mih
     */
    public static enum EntitiesSearchField {
        ID("id", "id"), LABEL("label", "label"), TYPE("type", "type"), PARENT("parent", "parentId"), TAG("tag",
                "tags"), STATE(
                "state", "state"), VERSION("version", "version"), ALL("term", "_all");

        private final String requestParameterName;

        private final String searchFieldName;

        EntitiesSearchField(final String requestParameterName, final String searchFieldName) {
            this.requestParameterName = requestParameterName;
            this.searchFieldName = searchFieldName;
        }

        public String getRequestParameterName() {
            return requestParameterName;
        }

        public String getFieldName() {
            return searchFieldName;
        }

        /**
         * EntitiesSearchField anhand requestParameterName ermitteln.
         * 
         * @param requestParameterName requestParameterName
         * @return EntitiesSearchField oder null, falls nicht gefunden.
         */
        public static EntitiesSearchField getWithRequestParameter(String requestParameterName) {
            for (EntitiesSearchField entitiesSearchField : EntitiesSearchField.values()) {
                if (entitiesSearchField.getRequestParameterName().equals(requestParameterName)) {
                    return entitiesSearchField;
                }
            }
            return null;
        }
    }

}
