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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.model.WorkspacePermissions;
import net.objecthunter.larch.service.backend.BackendWorkspaceService;

import org.apache.commons.lang3.RandomStringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchWorkspaceService extends AbstractElasticSearchService implements BackendWorkspaceService {

    public static final String INDEX_WORKSPACES = "workspaces";

    public static final String INDEX_WORKSPACE_TYPE = "workspace";

    private static final Logger log = LoggerFactory.getLogger(ElasticSearchWorkspaceService.class);

    private int maxRecords = 50;

    @PostConstruct
    public void init() throws IOException {
        log.debug("initialising ElasticSearchWorkspaceService");
        this.checkAndOrCreateIndex(INDEX_WORKSPACES);
        this.waitForIndex(INDEX_WORKSPACES);
    }

    @Override
    public String create(Workspace workspace) throws IOException {
        if (workspace.getId() == null || workspace.getId().isEmpty()) {
            // generate a workspace id
            workspace.setId(RandomStringUtils.randomAlphabetic(16));
        }
        if (workspace.getName() == null || workspace.getName().isEmpty()) {
            workspace.setName("Unnamed Workspace");
        }
        if (workspace.getPermissions() == null) {
            workspace.setPermissions(WorkspacePermissions.getDefaultPermissions(workspace.getOwner()));
        }
        /* check if the workspace exists already */
        final GetResponse get = this.client.prepareGet(INDEX_WORKSPACES, INDEX_WORKSPACE_TYPE, workspace.getId())
                .execute()
                .actionGet();
        if (get.isExists()) {
            throw new IOException("Workspace with id " + workspace.getId() + " does already exist");
        }
        /* create a new Index record for the workspace */
        final IndexResponse index =
                this.client.prepareIndex(INDEX_WORKSPACES, INDEX_WORKSPACE_TYPE, workspace.getId())
                        .setSource(mapper.writeValueAsBytes(workspace))
                        .execute()
                        .actionGet();
        this.refreshIndex(INDEX_WORKSPACES);
        return index.getId();
    }

    @Override
    public Workspace retrieve(final String id) throws IOException {
        final GetResponse get = this.client.prepareGet(INDEX_WORKSPACES, INDEX_WORKSPACE_TYPE, id)
                .execute()
                .actionGet();

        /* check if the workspace does exist */
        if (!get.isExists()) {
            throw new FileNotFoundException("The workspace with id '" + id + "' does not exist");
        }
        final Workspace ws = this.mapper.readValue(get.getSourceAsBytes(), Workspace.class);
        return ws;
    }

    @Override
    public void update(final Workspace workspace) throws IOException {
        /* check if the workspace does exist */
        final GetResponse get = this.client.prepareGet(INDEX_WORKSPACES, INDEX_WORKSPACE_TYPE, workspace.getId())
                .execute()
                .actionGet();
        if (!get.isExists()) {
            throw new FileNotFoundException("The workspace with id '" + workspace.getId() + "' does not exist");
        }

        this.client.prepareIndex(INDEX_WORKSPACES, INDEX_WORKSPACE_TYPE, workspace.getId())
                .setSource(mapper.writeValueAsBytes(workspace))
                .execute()
                .actionGet();
        this.refreshIndex(INDEX_WORKSPACES);
    }

    @Override
    public void patch(final Workspace workspace) throws IOException {
        /* check if the workspace does exist */
        final GetResponse get = this.client.prepareGet(INDEX_WORKSPACES, INDEX_WORKSPACE_TYPE, workspace.getId())
                .execute()
                .actionGet();
        if (!get.isExists()) {
            throw new FileNotFoundException("The workspace with id '" + workspace.getId() + "' does not exist");
        }
        /* only update the fields given in the patch request */
        final Workspace orig = mapper.readValue(get.getSourceAsBytes(), Workspace.class);
        if (!workspace.getName().isEmpty()) {
            orig.setName(workspace.getName());
        }
        if (!workspace.getOwner().isEmpty()) {
            orig.setOwner(workspace.getOwner());
        }
        this.client.prepareIndex(INDEX_WORKSPACES, INDEX_WORKSPACE_TYPE, orig.getId())
                .setSource(mapper.writeValueAsBytes(orig))
                .execute()
                .actionGet();

        this.refreshIndex(INDEX_WORKSPACES);
    }

    @Override
    public List<Workspace> scanIndex(final String owner, final int offset, int numRecords) throws IOException {
        if (numRecords < 1) {
            numRecords = maxRecords;
        }
        numRecords = numRecords > maxRecords ? maxRecords : numRecords;
        final SearchResponse resp;
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        if (owner == null) {
            queryBuilder.must(QueryBuilders.matchAllQuery());
        } else {
            queryBuilder.must(QueryBuilders.matchQuery("owner", owner));
        }
        queryBuilder.must(getWorkspacesUserRestrictionQuery());

        try {
            resp = this.client.prepareSearch(ElasticSearchWorkspaceService.INDEX_WORKSPACES)
                    .setQuery(queryBuilder)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setFrom(offset)
                    .setSize(numRecords)
                    .addFields("_source")
                    .execute()
                    .actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }

        final List<Workspace> workspaces = new ArrayList<>(numRecords);
        for (final SearchHit hit : resp.getHits()) {
            // TODO: check if JSON document is prefetched or laziliy initialised
            workspaces.add(mapper.readValue(hit.getSourceAsString(), Workspace.class));
        }
        return workspaces;
    }

}
