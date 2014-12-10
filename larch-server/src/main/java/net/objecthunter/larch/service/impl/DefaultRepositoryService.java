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

package net.objecthunter.larch.service.impl;

import java.io.IOException;

import net.objecthunter.larch.model.Describe;
import net.objecthunter.larch.model.state.LarchState;
import net.objecthunter.larch.service.RepositoryService;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;
import net.objecthunter.larch.service.backend.BackendEntityService;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.annotation.Secured;

/**
 * Default implementation of a {@link net.objecthunter.larch.service.RepositoryService} which is able to fetch state
 * information from the underlying {@link net.objecthunter.larch.service.backend.BackendEntityService} and
 * {@link net.objecthunter.larch.service.backend.BackendBlobstoreService} implementations
 */
public class DefaultRepositoryService implements RepositoryService {

    @Autowired
    private BackendEntityService backendEntityService;

    @Autowired
    private BackendBlobstoreService backendBlobstoreService;

    @Autowired
    private Environment env;

    @Autowired
    private Client client;

    @Override
    public LarchState status() throws IOException {
        final LarchState state = new LarchState();
        state.setBlobstoreState(backendBlobstoreService.status());
        state.setIndexState(backendEntityService.status());
        return state;
    }

    @Override
    public Describe describe() throws IOException {
        final Describe desc = new Describe();
        desc.setLarchVersion(env.getProperty("version"));
        desc.setLarchHost("localhost:" + env.getProperty("server.port"));
        desc.setLarchClusterName(env.getProperty("cluster.name"));
        try {
            final ClusterStateResponse state = client.admin().cluster().prepareState()
                    .setBlocks(false)
                    .setMetaData(true)
                    .setRoutingTable(false)
                    .setNodes(true)
                    .execute()
                    .actionGet();
            desc.setEsMasterNodeName(state.getState().getNodes().getMasterNodeId());
            desc.setEsNumDataNodes(state.getState().getNodes().getDataNodes().size());
            desc.setEsMasterNodeAddress(state.getState().getNodes().getMasterNode().getAddress().toString());
            desc.setEsNodeName(state.getState().getNodes().getLocalNodeId());
            final ClusterStatsResponse stats = client.admin().cluster().prepareClusterStats()
                    .execute()
                    .actionGet();
            desc.setEsNumIndexedRecords(stats.getIndicesStats().getDocs().getCount());
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        return desc;
    }
}
