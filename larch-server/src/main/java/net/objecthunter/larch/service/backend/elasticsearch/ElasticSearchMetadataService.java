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
 * WITHOUT WARRANTIES OR CONDITIONS OF ROLE_ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package net.objecthunter.larch.service.backend.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.EntityHierarchy;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.service.backend.BackendMetadataService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
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
public class ElasticSearchMetadataService extends AbstractElasticSearchService implements BackendMetadataService {

    public static final String INDEX_METADATAS = "metadatas";

    public static final String INDEX_METADATA_TYPE = "metadata";

    private static final Logger log = LoggerFactory.getLogger(ElasticSearchMetadataService.class);

    private int maxRecords;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private XMLSerializer serializer;

    @PostConstruct
    public void init() throws IOException {
        log.debug("initialising ElasticSearchMetadataService");
        this.maxRecords = Integer.parseInt(env.getProperty("search.maxRecords", "20"));
        this.checkAndOrCreateIndex(INDEX_METADATAS);
        this.waitForIndex(INDEX_METADATAS);
    }

    @Override
    public void index(Entity entity, EntityHierarchy entityHierarchy) throws IOException {
        if (entity == null || entityHierarchy == null) {
            throw new IOException("entitydata may not be null");
        }
        Map<String, Object> metadataMap = new HashMap<String, Object>();
        // for authorization
        metadataMap.put(EntitiesSearchField.ID.getFieldName(), entity.getId());
        metadataMap.put(EntitiesSearchField.LABEL.getFieldName(), entity.getLabel());
        metadataMap.put(EntitiesSearchField.STATE.getFieldName(), entity.getState());
        metadataMap.put(EntitiesSearchField.CONTENT_MODEL.getFieldName(), entity.getContentModelId());
        metadataMap.put(EntitiesSearchField.LEVEL1.getFieldName(), entityHierarchy.getLevel1Id());
        metadataMap.put(EntitiesSearchField.LEVEL2.getFieldName(), entityHierarchy.getLevel2Id());
        // write metadata as json
        if (entity.getMetadata() != null) {
            for (Metadata md : entity.getMetadata().values()) {
                if (metadataMap.get("md_" + md.getType()) == null) {
                    metadataMap.put("md_" + md.getType(), new ArrayList<Map>());
                }
                ((List) metadataMap.get("md_" + md.getType())).add(mapper.readValue(serializer.read(md.getData())
                        .toString(), Map.class));
            }
        }
        if (entity.getBinaries() != null) {
            for (Binary binary : entity.getBinaries().values()) {
                if (binary.getMetadata() != null) {
                    for (Metadata md : binary.getMetadata().values()) {
                        if (metadataMap.get("binarymd_" + md.getType()) == null) {
                            metadataMap.put("binarymd_" + md.getType(), new ArrayList<Map>());
                        }
                        ((List) metadataMap.get("binarymd_" + md.getType())).add(mapper.readValue(serializer.read(
                                md.getData())
                                .toString(), Map.class));
                    }
                }
            }
        }
        try {
            client
                    .prepareIndex(INDEX_METADATAS, INDEX_METADATA_TYPE, entity.getId()).setSource(
                            mapper.writeValueAsBytes(metadataMap))
                    .execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        refreshIndex(INDEX_METADATAS);
    }

    @Override
    public void delete(String entityId) throws IOException {
        log.debug("deleting metadata for entity " + entityId);
        try {
            client.prepareDelete(INDEX_METADATAS, INDEX_METADATA_TYPE, entityId).execute().actionGet();
            refreshIndex(INDEX_METADATAS);
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public SearchResult searchMetadata(String query, int offset)
            throws IOException {
        return searchMetadata(query, offset, maxRecords);
    }

    @Override
    public SearchResult searchMetadata(String query, int offset, int maxRecords)
            throws IOException {
        final long time = System.currentTimeMillis();
        final ActionFuture<RefreshResponse> refresh =
                this.client.admin().indices().refresh(
                        new RefreshRequest(ElasticSearchMetadataService.INDEX_METADATAS));
        final SearchResponse resp;

        if (StringUtils.isBlank(query)) {
            query = "*:*";
        }
        QueryStringQueryBuilder builder = QueryBuilders.queryString(query);

        try {
            refresh.actionGet();

            resp =
                    this.client
                            .prepareSearch(ElasticSearchMetadataService.INDEX_METADATAS).addFields(
                                    EntitiesSearchField.ID.getFieldName(), EntitiesSearchField.LABEL.getFieldName())
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
            String id =
                    hit.field(EntitiesSearchField.ID.getFieldName()) != null ? hit.field(
                            EntitiesSearchField.ID.getFieldName()).getValue() : null;
            String label =
                    hit.field(EntitiesSearchField.LABEL.getFieldName()) != null ? hit.field(
                            EntitiesSearchField.LABEL.getFieldName()).getValue() : "";
            final Entity e = new Entity();
            e.setId(id);
            e.setLabel(label);
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

}
