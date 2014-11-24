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

package net.objecthunter.larch.service.backend.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.exceptions.AlreadyExistsException;
import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.EntityHierarchy;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.model.state.IndexState;
import net.objecthunter.larch.service.backend.BackendEntityService;
import net.objecthunter.larch.service.backend.BackendMetadataService;

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
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
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

    private static final Logger log = LoggerFactory.getLogger(ElasticSearchEntityService.class);
    
    @Autowired
    private BackendMetadataService backendMetadataService;

    private int maxRecords;

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    public void init() throws IOException {
        log.debug("initialising ElasticSearchEntityService");
        this.maxRecords = Integer.parseInt(env.getProperty("search.maxRecords", "20"));
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
        Map<String, Object> entityData = mapper.readValue(mapper.writeValueAsString(e), Map.class);
        EntityHierarchy entityHierarchy = getHierarchy(e);
        entityData.put(EntitiesSearchField.LEVEL1.getFieldName(), entityHierarchy.getLevel1Id());
        entityData.put(EntitiesSearchField.LEVEL2.getFieldName(), entityHierarchy.getLevel2Id());

        try {
            client
                    .prepareIndex(INDEX_ENTITIES, INDEX_ENTITY_TYPE, e.getId()).setSource(
                            mapper.writeValueAsBytes(entityData))
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
        Map<String, Object> entityData = mapper.readValue(mapper.writeValueAsString(e), Map.class);
        // set hierarchy
        EntityHierarchy entityHierarchy = getHierarchy(e);
        entityData.put(EntitiesSearchField.LEVEL1.getFieldName(), entityHierarchy.getLevel1Id());
        entityData.put(EntitiesSearchField.LEVEL2.getFieldName(), entityHierarchy.getLevel2Id());
        try { 
            client
                    .prepareIndex(INDEX_ENTITIES, INDEX_ENTITY_TYPE, e.getId()).setSource(
                            mapper.writeValueAsBytes(entityData))
                    .execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        /* refresh the index before returning */
        refreshIndex(INDEX_ENTITIES);
    }

    @Override
    public Entity retrieve(String entityId) throws IOException {
        log.debug("fetching entity " + entityId);
        final GetResponse resp;
        try {
            resp = client.prepareGet(INDEX_ENTITIES, INDEX_ENTITY_TYPE, entityId).execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        if (resp.isSourceEmpty()) {
            throw new NotFoundException("Entity with id " + entityId + " not found");
        }
        return mapper.readValue(resp.getSourceAsBytes(), Entity.class);
    }

    @Override
    public List<String> fetchChildren(String id) throws IOException {
        final List<String> children = new ArrayList<>();
        SearchResponse search;
        int offset = 0;
        int max = 64;
        try {
            do {
                search = client.prepareSearch(INDEX_ENTITIES)
                        .setTypes(INDEX_ENTITY_TYPE)
                        .setQuery(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
                                FilterBuilders.termFilter(EntitiesSearchField.PARENT.getFieldName(), id)))
                        .setFrom(offset)
                        .setSize(max)
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
        final GetResponse resp;
        try {
            resp = client.prepareGet(INDEX_ENTITIES, INDEX_ENTITY_TYPE, id).execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        final Entity e = this.mapper.readValue(resp.getSourceAsBytes(), Entity.class);
        try {
            client.prepareDelete(INDEX_ENTITIES, INDEX_ENTITY_TYPE, id).execute().actionGet();
            refreshIndex(INDEX_ENTITIES);
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
    public SearchResult searchEntities(String query, int offset)
            throws IOException {
        return searchEntities(query, offset, maxRecords);
    }

    @Override
    public SearchResult searchEntities(String query, int offset, int maxRecords)
            throws IOException {
        final long time = System.currentTimeMillis();
        final ActionFuture<RefreshResponse> refresh =
                this.client.admin().indices().refresh(new RefreshRequest(ElasticSearchEntityService.INDEX_ENTITIES));
        final SearchResponse resp;

        if (StringUtils.isBlank(query)) {
            query = "*:*";
        }
        QueryStringQueryBuilder builder = QueryBuilders.queryString(query);

        try {
            refresh.actionGet();

            resp =
                    this.client
                            .prepareSearch(ElasticSearchEntityService.INDEX_ENTITIES).addFields(
                                    EntitiesSearchField.ID.getFieldName(), EntitiesSearchField.PARENT.getFieldName(),
                                    EntitiesSearchField.STATE.getFieldName(),
                                    EntitiesSearchField.LABEL.getFieldName(),
                                    EntitiesSearchField.CONTENT_MODEL.getFieldName(),
                                    EntitiesSearchField.TAG.getFieldName())
                            .setQuery(builder).setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setFrom(offset)
                            .setSize(maxRecords).execute()
                            .actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        log.debug("ES returned {} results for '{}'", resp.getHits().getHits().length, new String(builder
                .buildAsBytes().toBytes()));
        final SearchResult result = new SearchResult();

        final List<Entity> entities = new ArrayList<>();
        for (final SearchHit hit : resp.getHits()) {
            String parentId =
                    hit.field(EntitiesSearchField.PARENT.getFieldName()) != null ? hit.field(
                            EntitiesSearchField.PARENT.getFieldName()).getValue() : null;
            String label =
                    hit.field(EntitiesSearchField.LABEL.getFieldName()) != null ? hit.field(
                            EntitiesSearchField.LABEL.getFieldName()).getValue() : "";
            String contentModelId =
                    hit.field(EntitiesSearchField.CONTENT_MODEL.getFieldName()) != null ? hit.field(
                            EntitiesSearchField.CONTENT_MODEL.getFieldName()).getValue() : "";
            String state =
                    hit.field(EntitiesSearchField.STATE.getFieldName()) != null ? hit.field(
                            EntitiesSearchField.STATE.getFieldName()).getValue() : "";
            final Entity e = new Entity();
            e.setId(hit.field(EntitiesSearchField.ID.getFieldName()).getValue());
            e.setParentId(parentId);
            e.setContentModelId(contentModelId);
            e.setState(EntityState.valueOf(state));
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
        result.setHits(entities.size());
        result.setNumRecords(entities.size());
        result.setTerm(new String(builder.buildAsBytes().toBytes()));
        result.setOffset(offset);
        result.setNextOffset(offset + maxRecords);
        result.setPrevOffset(Math.max(offset - maxRecords, 0));
        result.setDuration(System.currentTimeMillis() - time);
        return result;
    }

    @Override
    public EntityHierarchy getHierarchy(String entityId) throws IOException {
        final GetResponse resp;
        try {
            resp =
                    client.prepareGet(INDEX_ENTITIES, INDEX_ENTITY_TYPE, entityId).setFields(
                            EntitiesSearchField.LEVEL1.getFieldName(),
                            EntitiesSearchField.LEVEL2.getFieldName()).execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        if (!resp.isExists()) {
            throw new NotFoundException("Entity with id " + entityId + " not found");
        }
        EntityHierarchy entityHierarchy = new EntityHierarchy();
        String level1Id =
                resp.getField(EntitiesSearchField.LEVEL1.getFieldName()) != null ? (String) resp.getField(
                        EntitiesSearchField.LEVEL1.getFieldName()).getValue() : null;
        String level2Id =
                resp.getField(EntitiesSearchField.LEVEL2.getFieldName()) != null ? (String) resp.getField(
                        EntitiesSearchField.LEVEL2.getFieldName()).getValue() : null;
        entityHierarchy.setLevel1Id(level1Id);
        entityHierarchy.setLevel2Id(level2Id);
        return entityHierarchy;
    }

    @Override
    public EntityHierarchy getHierarchy(Entity entity) throws IOException {
        if (entity == null) {
            throw new IOException("entity may not be null");
        }
        EntityHierarchy entityHierarchy = new EntityHierarchy();
        if (FixedContentModel.LEVEL1.getName().equals(entity.getContentModelId())) {
            entityHierarchy.setLevel1Id(entity.getId());
        } else if (FixedContentModel.LEVEL2.getName().equals(entity.getContentModelId())) {
            entityHierarchy.setLevel1Id(entity.getParentId());
            entityHierarchy.setLevel2Id(entity.getId());
        } else if (entity.getContentModelId() != null) {
            return getHierarchy(entity.getParentId());
        } else {
            throw new IOException("contentModel may not be null");
        }
        return entityHierarchy;
    }

    /**
     * Holds enabled search-fields in entities-index.
     * 
     * @author mih
     */
    public static enum EntitiesSearchField {
        ID("id"),
        LABEL("label"),
        CONTENT_MODEL("contentModelId"),
        PARENT("parentId"),
        TAG("tags"),
        STATE("state"),
        VERSION("version"),
        LEVEL1("level1"),
        LEVEL2("level2"),
        ALL("_all");

        private final String searchFieldName;

        EntitiesSearchField(final String searchFieldName) {
            this.searchFieldName = searchFieldName;
        }

        public String getFieldName() {
            return searchFieldName;
        }
    }

}
