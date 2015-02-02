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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.model.Archive;
import net.objecthunter.larch.model.EntityHierarchy;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.service.backend.BackendArchiveIndexService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ElasticSearchArchiveIndexService extends AbstractElasticSearchService implements
        BackendArchiveIndexService {

    private static final Logger log = LoggerFactory.getLogger(ElasticSearchArchiveIndexService.class);

    public static final String INDEX_ARCHIVES = "archives";

    public static final String INDEX_ARCHIVE_TYPE = "archive";

    @Autowired
    private Client client;

    @PostConstruct
    public void init() throws IOException {
        this.checkAndOrCreateIndex(INDEX_ARCHIVES);
        this.waitForIndex(INDEX_ARCHIVES);
    }

    @Override
    public Archive retrieve(final String id, final int version) throws IOException {
        final GetResponse resp = client.prepareGet(INDEX_ARCHIVES, INDEX_ARCHIVE_TYPE, id + "_v" + version)
                .execute()
                .actionGet();
        if (!resp.isExists()) {
            throw new FileNotFoundException("No archive for Entity " + id + " with version " + version);
        }
        return mapper.readValue(resp.getSourceAsBytes(), Archive.class);
    }

    @Override
    public void saveOrUpdate(final Archive a, final EntityHierarchy entityHierarchy) throws IOException {
        Map<String, Object> archiveData = mapper.readValue(mapper.writeValueAsString(a), Map.class);
        archiveData.put(EntitiesSearchField.LEVEL1.getFieldName(), entityHierarchy.getLevel1Id());
        archiveData.put(EntitiesSearchField.LEVEL2.getFieldName(), entityHierarchy.getLevel2Id());
        final IndexResponse index =
                this.client.prepareIndex(INDEX_ARCHIVES, INDEX_ARCHIVE_TYPE,
                        a.getEntityId() + "_v" + a.getEntityVersion())
                        .setSource(this.mapper.writeValueAsBytes(archiveData))
                        .execute()
                        .actionGet();
        this.refreshIndex(INDEX_ARCHIVES);
    }

    @Override
    public void delete(final String entityId, final int version) throws IOException {
        final DeleteResponse delete =
                this.client.prepareDelete(INDEX_ARCHIVES, INDEX_ARCHIVE_TYPE, entityId + "_v" + version)
                        .execute()
                        .actionGet();
        this.refreshIndex(INDEX_ARCHIVES);
    }

    @Override
    public boolean exists(final String id, final int version) throws IOException {
        return this.client.prepareGet(INDEX_ARCHIVES, INDEX_ARCHIVE_TYPE, id + "_v" + version)
                .execute()
                .actionGet()
                .isExists();
    }

    @Override
    public SearchResult searchArchives(String query, int offset, int maxRecords)
            throws IOException {
        final long time = System.currentTimeMillis();
        final SearchResponse resp;
        if (StringUtils.isBlank(query)) {
            query = "*:*";
        }
        QueryStringQueryBuilder builder = QueryBuilders.queryString(query);
        try {
            resp =
                    this.client.prepareSearch(INDEX_ARCHIVES).setQuery(builder).execute()
                            .actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }

        final SearchResult result = new SearchResult();

        final List<Archive> archives = new ArrayList<>();
        for (final SearchHit hit : resp.getHits()) {
            archives.add(mapper.readValue(hit.getSourceAsString(), Archive.class));
        }
        result.setData(archives);
        result.setTotalHits(resp.getHits().getTotalHits());
        result.setMaxRecords(maxRecords);
        result.setHits(archives.size());
        result.setNumRecords(archives.size());
        result.setTerm(new String(builder.buildAsBytes().toBytes()));
        result.setOffset(offset);
        result.setNextOffset(offset + maxRecords);
        result.setPrevOffset(Math.max(offset - maxRecords, 0));
        result.setDuration(System.currentTimeMillis() - time);
        return result;
    }

    /**
     * Holds enabled search-fields in archives-index.
     * 
     * @author mih
     */
    public static enum ArchivesSearchField {
        ID("entityId"),
        VERSION("entityVersion"),
        CONTENT_MODEL("contentModelId"),
        CREATOR("creator"),
        STATE("state"),
        LEVEL1("level1"),
        LEVEL2("level2"),
        ALL("_all");

        private final String searchFieldName;

        ArchivesSearchField(final String searchFieldName) {
            this.searchFieldName = searchFieldName;
        }

        public String getFieldName() {
            return searchFieldName;
        }
    }

}
