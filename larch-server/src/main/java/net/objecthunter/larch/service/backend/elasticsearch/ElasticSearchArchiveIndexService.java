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

import net.objecthunter.larch.model.Archive;
import net.objecthunter.larch.service.backend.BackendArchiveIndexService;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;

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
    public Archive retrieve(String id, int version) throws IOException {
        final GetResponse resp = client.prepareGet(INDEX_ARCHIVES, INDEX_ARCHIVE_TYPE, id + "_v" + version)
                .execute()
                .actionGet();
        if (!resp.isExists()) {
            throw new FileNotFoundException("No archive for Entity " + id + " with version " + version);
        }
        return mapper.readValue(resp.getSourceAsBytes(), Archive.class);
    }

    @Override
    public void saveOrUpdate(Archive a) throws IOException {
        final IndexResponse index = this.client.prepareIndex(INDEX_ARCHIVES, INDEX_ARCHIVE_TYPE)
                .setSource(this.mapper.writeValueAsBytes(a))
                .setId(a.getEntityId() + "_v" + a.getEntityVersion())
                .execute()
                .actionGet();
        this.refreshIndex(INDEX_ARCHIVES);
    }

    @Override
    public void delete(String entityId, int version) throws IOException {
        final DeleteResponse delete = this.client.prepareDelete(INDEX_ARCHIVES, INDEX_ARCHIVE_TYPE, entityId + "_v" + version)
                .execute()
                .actionGet();
        this.refreshIndex(INDEX_ARCHIVES);
    }

    @Override
    public boolean exists(String id, int version) throws IOException {
        return this.client.prepareGet(INDEX_ARCHIVES, INDEX_ARCHIVE_TYPE, id + "_v" + version)
                .execute()
                .actionGet()
                .isExists();
    }

}
