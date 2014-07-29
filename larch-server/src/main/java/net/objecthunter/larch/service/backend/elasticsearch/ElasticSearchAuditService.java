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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.model.AuditRecord;
import net.objecthunter.larch.service.backend.BackendAuditService;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementation of an {@link net.objecthunter.larch.service.backend.BackendAuditService} built on top of
 * ElasticSearch
 */
public class ElasticSearchAuditService extends AbstractElasticSearchService implements BackendAuditService {

    public static final String INDEX_AUDIT = "audit";

    public static final String INDEX_AUDIT_TYPE = "audit";
    
    public static final String ENTITY_ID_FIELD = "entityId";

    private static final Logger log = Logger.getLogger(ElasticSearchAuditService.class);

    private int maxRecords = 50;

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    public void init() throws IOException {
        log.debug("initialising ElasticSearchAuditService");
        this.checkAndOrCreateIndex(INDEX_AUDIT);
        this.waitForIndex(INDEX_AUDIT);
    }

    @Override
    public List<AuditRecord> retrieve(String entityId, int offset, int numRecords) throws IOException {
        numRecords = numRecords > maxRecords ? maxRecords : numRecords;
        final SearchResponse resp;
        try {
            resp =
                    this.client
                            .prepareSearch(INDEX_AUDIT)
                            .setQuery(
                                    QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
                                            FilterBuilders
                                                    .termFilter(ENTITY_ID_FIELD, entityId)))
                            .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setFrom(offset).setSize(numRecords)
                            .addSort("timestamp", SortOrder.ASC).execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }

        final List<AuditRecord> records = new ArrayList<>(numRecords);
        for (final SearchHit hit : resp.getHits()) {
            records.add(mapper.readValue(hit.getSourceAsString(), AuditRecord.class));
        }

        return records;
    }

    @Override
    public String create(AuditRecord rec) throws IOException {
        String id;
        do {
            id = RandomStringUtils.randomAlphabetic(16);
        } while (this.exists(id));
        rec.setId(id);
        rec.setTimestamp(ZonedDateTime.now(ZoneOffset.UTC).toString());
        try {
            this.client
                    .prepareIndex(INDEX_AUDIT, INDEX_AUDIT_TYPE, id).setSource(mapper.writeValueAsBytes(rec)).execute()
                    .actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        refreshIndex(INDEX_AUDIT);
        return id;
    }

    @Override
    public void deleteAll(String entityId) throws IOException {
        log.debug("deleting all audit-records for entity " + entityId);
        try {
            client.prepareDeleteByQuery(INDEX_AUDIT).setQuery(
                    QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
                            FilterBuilders
                                    .termFilter(ENTITY_ID_FIELD, entityId))).execute().actionGet();
            refreshIndex(INDEX_AUDIT);
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
    }

    private boolean exists(String id) throws IOException {
        try {
            return this.client.prepareGet(INDEX_AUDIT, null, id).execute().actionGet().isExists();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
    }
}
