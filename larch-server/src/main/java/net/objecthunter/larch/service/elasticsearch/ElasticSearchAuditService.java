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

import com.fasterxml.jackson.databind.ObjectMapper;
import net.objecthunter.larch.model.AuditRecord;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.service.AuditService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of an {@link net.objecthunter.larch.service.AuditService} built on top of ElasticSearch
 */
public class ElasticSearchAuditService implements AuditService {
    public static final String INDEX_AUDIT = "audit";
    private static final Logger log = Logger.getLogger(ElasticSearchAuditService.class);

    private int maxRecords = 50;

    @Autowired
    private Client client;

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    public void init() {
        log.debug("initialising ElasticSearchIndexService");
        boolean indexExists = client.admin()
                .indices()
                .exists(new IndicesExistsRequest(INDEX_AUDIT))
                .actionGet()
                .isExists();
        if (!indexExists) {
            client.admin()
                    .indices()
                    .create(new CreateIndexRequest(INDEX_AUDIT))
                    .actionGet();
        }
        this.client.admin().cluster()
                .prepareHealth(INDEX_AUDIT)
                .setWaitForYellowStatus()
                .execute()
                .actionGet();
    }

    @Override
    public List<AuditRecord> retrieve(String entityId, int offset, int numRecords) throws IOException {
        numRecords = numRecords > maxRecords ? maxRecords : numRecords;
        final SearchResponse resp = this.client.prepareSearch(INDEX_AUDIT)
                .setQuery(QueryBuilders.matchQuery("entityId", entityId))
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFrom(offset)
                .setSize(numRecords)
                .execute()
                .actionGet();


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
        final IndexResponse resp = this.client.prepareIndex(INDEX_AUDIT, "audit", id)
                .setSource(mapper.writeValueAsBytes(rec))
                .execute()
                .actionGet();
        return id;
    }

    private boolean exists(String id) {
        return this.client.prepareGet(INDEX_AUDIT, null, id)
                .execute()
                .actionGet()
                .isExists();
    }

    private void refreshIndex(String... indices) {
        this.client.admin()
                .indices()
                .refresh(new RefreshRequest(indices))
                .actionGet();
    }

}