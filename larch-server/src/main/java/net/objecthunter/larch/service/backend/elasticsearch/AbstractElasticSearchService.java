
package net.objecthunter.larch.service.backend.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import net.objecthunter.larch.service.AuthorizationService;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Abstract service for providing utility methods used by all the different ElasticSearch services
 */
public class AbstractElasticSearchService {

    @Autowired
    protected Environment env;

    @Autowired
    protected Client client;

    @Autowired
    protected ObjectMapper mapper;

    protected void refreshIndex(String... indices) throws IOException {
        try {
            client.admin().indices().refresh(new RefreshRequest(indices)).actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
    }

    protected void checkAndOrCreateIndex(String indexName) throws IOException {
        try {
            if (!indexExists(indexName)) {
                Map mappings = getMappings(indexName);
                if (mappings != null && !mappings.isEmpty()) {
                    CreateIndexRequestBuilder requestBuilder = client.admin().indices().prepareCreate(indexName);
                    for (String key : ((Set<String>) mappings.keySet())) {
                        requestBuilder.addMapping(key, mapper.writeValueAsString(mappings.get(key)));
                    }
                    requestBuilder.execute().actionGet();
                }
                else {
                    client.admin().indices().prepareCreate(indexName).execute().actionGet();
                }
            }
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
    }

    protected boolean indexExists(String indexName) throws IOException {
        try {
            return client.admin().indices().exists(new IndicesExistsRequest(indexName)).actionGet().isExists();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
    }

    protected void waitForIndex(String indexName) throws IOException {
        try {
            this.client.admin().cluster().prepareHealth(indexName).setWaitForYellowStatus().execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
    }

    private Map getMappings(String indexName) throws IOException {
        InputStream in =
                this.getClass().getResourceAsStream(
                        env.getProperty("elasticsearch.config.path") + indexName + "_mappings.json");
        if (in != null) {
            String mappings = IOUtils.toString(in);
            return mapper.readValue(mappings, Map.class);
        }
        return null;
    }
}
