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

package de.escidocng.service.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequestBuilder;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsIndices;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsRequestBuilder;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.shard.DocsStats;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import de.escidocng.model.Describe;
import de.escidocng.model.state.EscidocngState;
import de.escidocng.service.backend.BackendBlobstoreService;
import de.escidocng.service.backend.BackendEntityService;
import de.escidocng.service.impl.DefaultRepositoryService;

public class DefaultRepositoryServiceTest {

    private DefaultRepositoryService repositoryService;

    private Client mockClient;

    private BackendBlobstoreService mockBlobstoreService;

    private BackendEntityService mockEntitiesService;

    private Environment mockEnv;

    private AdminClient mockAdminClient;

    private ClusterAdminClient mockClusterAdminClient;

    @Before
    public void setup() {
        repositoryService = new DefaultRepositoryService();
        mockClient = createMock(Client.class);
        mockBlobstoreService = createMock(BackendBlobstoreService.class);
        mockEntitiesService = createMock(BackendEntityService.class);
        mockEnv = createMock(Environment.class);
        mockAdminClient = createMock(AdminClient.class);
        mockClusterAdminClient = createMock(ClusterAdminClient.class);
        ReflectionTestUtils.setField(repositoryService, "client", mockClient);
        ReflectionTestUtils.setField(repositoryService, "backendBlobstoreService", mockBlobstoreService);
        ReflectionTestUtils.setField(repositoryService, "backendEntityService", mockEntitiesService);
        ReflectionTestUtils.setField(repositoryService, "env", mockEnv);
    }

    @Test
    public void testStatus() throws Exception {
        expect(mockEntitiesService.status()).andReturn(null);
        expect(mockBlobstoreService.status()).andReturn(null);

        replay(mockClient, mockEntitiesService, mockBlobstoreService);
        EscidocngState state = this.repositoryService.status();
        verify(mockClient, mockEntitiesService, mockBlobstoreService);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDescribe() throws Exception {
        ClusterStateRequestBuilder mockClusterStateRequest = createMock(ClusterStateRequestBuilder.class);
        ClusterStateResponse mockClusterStateResponse = createMock(ClusterStateResponse.class);
        ListenableActionFuture mockFuture = createMock(ListenableActionFuture.class);
        ClusterState mockClusterState = createMock(ClusterState.class);
        DiscoveryNodes mockDiscoveryNodes = createMock(DiscoveryNodes.class);
        DiscoveryNode mockDiscoveryNode = createMock(DiscoveryNode.class);
        TransportAddress mockAddress = createMock(TransportAddress.class);
        ClusterStatsRequestBuilder mockClusterStatsRequest = createMock(ClusterStatsRequestBuilder.class);
        ClusterStatsResponse mockClusterStatsResponse = createMock(ClusterStatsResponse.class);
        ClusterStatsIndices mockIndicesStats = createMock(ClusterStatsIndices.class);
        DocsStats mockDocStats = createMock(DocsStats.class);

        expect(mockEnv.getProperty("escidocng.version")).andReturn("0.0-TEST");
        expect(mockEnv.getProperty("server.port")).andReturn("8080");
        expect(mockEnv.getProperty("escidocng.cluster.name")).andReturn("escidocng-cluster");
        expect(mockClient.admin()).andReturn(mockAdminClient).times(2);
        expect(mockAdminClient.cluster()).andReturn(mockClusterAdminClient).times(2);
        expect(mockClusterAdminClient.prepareState()).andReturn(mockClusterStateRequest);
        expect(mockClusterStateRequest.setBlocks(false)).andReturn(mockClusterStateRequest);
        expect(mockClusterStateRequest.setMetaData(true)).andReturn(mockClusterStateRequest);
        expect(mockClusterStateRequest.setRoutingTable(false)).andReturn(mockClusterStateRequest);
        expect(mockClusterStateRequest.setNodes(true)).andReturn(mockClusterStateRequest);
        expect(mockClusterStateRequest.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockClusterStateResponse);
        expect(mockClusterStateResponse.getState()).andReturn(mockClusterState).times(4);
        expect(mockClusterState.getNodes()).andReturn(mockDiscoveryNodes).times(4);
        expect(mockDiscoveryNodes.getMasterNodeId()).andReturn("master-node-1");
        expect(mockDiscoveryNodes.getDataNodes()).andReturn(ImmutableOpenMap.<String, DiscoveryNode> of());
        expect(mockDiscoveryNodes.getMasterNode()).andReturn(mockDiscoveryNode);
        expect(mockDiscoveryNode.getAddress()).andReturn(mockAddress);
        expect(mockDiscoveryNodes.getLocalNodeId()).andReturn("local-node-1");
        expect(mockClusterAdminClient.prepareClusterStats()).andReturn(mockClusterStatsRequest);
        expect(mockClusterStatsRequest.execute()).andReturn(mockFuture);
        expect(mockFuture.actionGet()).andReturn(mockClusterStatsResponse);
        expect(mockClusterStatsResponse.getIndicesStats()).andReturn(mockIndicesStats);
        expect(mockIndicesStats.getDocs()).andReturn(mockDocStats);
        expect(mockDocStats.getCount()).andReturn(1l);

        replay(mockClient, mockEntitiesService, mockBlobstoreService, mockEnv, mockAdminClient,
                mockClusterAdminClient,
                mockFuture, mockClusterStateRequest, mockClusterStateResponse, mockClusterState, mockDiscoveryNodes,
                mockDiscoveryNode, mockAddress, mockClusterStatsRequest, mockClusterStatsResponse, mockIndicesStats,
                mockDocStats);
        Describe desc = this.repositoryService.describe();
        verify(mockClient, mockEntitiesService, mockBlobstoreService, mockEnv, mockAdminClient,
                mockClusterAdminClient,
                mockFuture, mockClusterStateRequest, mockClusterStateResponse, mockClusterState, mockDiscoveryNodes,
                mockDiscoveryNode, mockAddress, mockClusterStatsRequest, mockClusterStatsResponse, mockIndicesStats,
                mockDocStats);

    }
}
