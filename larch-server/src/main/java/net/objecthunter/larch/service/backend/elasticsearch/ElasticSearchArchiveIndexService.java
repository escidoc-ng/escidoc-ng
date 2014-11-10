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

import net.objecthunter.larch.model.Archive;
import net.objecthunter.larch.service.backend.BackendArchiveIndexService;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ElasticSearchArchiveIndexService extends AbstractElasticSearchService implements BackendArchiveIndexService {

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
    public void saveOrUpdate(final Archive a) throws IOException {
        final IndexResponse index = this.client.prepareIndex(INDEX_ARCHIVES, INDEX_ARCHIVE_TYPE)
                .setSource(this.mapper.writeValueAsBytes(a))
                .setId(a.getEntityId() + "_v" + a.getEntityVersion())
                .execute()
                .actionGet();
        this.refreshIndex(INDEX_ARCHIVES);
    }

    @Override
    public void delete(final String entityId, final int version) throws IOException {
        final DeleteResponse delete = this.client.prepareDelete(INDEX_ARCHIVES, INDEX_ARCHIVE_TYPE, entityId + "_v" + version)
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
    public List<Archive> list(final int offset, final int count) throws IOException{
        final List<Archive> archives = new ArrayList<>(count);
        final SearchResponse resp = this.client.prepareSearch(INDEX_ARCHIVES)
                .setTypes(INDEX_ARCHIVE_TYPE)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.matchAllQuery())
                .setFrom(offset)
                .setSize(count)
                .execute()
                .actionGet();
        for (SearchHit hit: resp.getHits().getHits()) {
            archives.add(this.mapper.readValue(hit.getSourceAsString(), Archive.class));
        }
        return archives;
    }

}
