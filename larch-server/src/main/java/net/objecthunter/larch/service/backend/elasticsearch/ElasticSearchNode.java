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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 * A Spring bean for starting a ElasticSearch node as an internal process in the same JVM
 */
public class ElasticSearchNode {

    private Node node;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void startNode() {
        node =
                NodeBuilder.nodeBuilder()
                        .clusterName(environment.getProperty("elasticsearch.cluster.name"))
                        .settings(
                            ImmutableSettings.settingsBuilder()
                                .put("path.logs",
                                     environment.getProperty("elasticsearch.path.logs"))
                                .put("path.data",
                                     environment.getProperty("elasticsearch.path.data"))
                                .put("bootstrap.mlockall",
                                     environment.getProperty("elasticsearch.bootstrap.mlockall"))
                                .put("network.bind.host",
                                     environment.getProperty("elasticsearch.network.bind.host"))
                                .put("network.host",
                                     environment.getProperty("elasticsearch.network.host", ""))
                                .put("gateway.expected_nodes",
                                     environment
                                         .getProperty("elasticsearch.gateway.expected_nodes"))
                                .put("http.port",
                                     environment.getProperty("elasticsearch.http.port"))
                                .put("http.enabled",
                                     environment.getProperty("elasticsearch.http.enabled"))
                                .put("transport.tcp.port",
                                     environment
                                         .getProperty("elasticsearch.transport.tcp.port", ""))
                                .put("network.publish_host",
                                     environment
                                         .getProperty("elasticsearch.network.publish_host", ""))
                                .put("gateway.type",
                                     environment.getProperty("elasticsearch.gateway.type"))
                        )
                        .node();
    }

    @PreDestroy
    public void stopNode() {
        if (node != null) {
            node.stop();
        }
    }

    public Client getClient() {
        return node.client();
    }

    public boolean isAlive() {
        return node != null && !node.isClosed();
    }
}
