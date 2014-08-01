
package net.objecthunter.larch.service.backend.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.model.WorkspacePermissions.Permission;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.AuthorizationService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
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
    protected AuthorizationService authorizationService;

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

    /**
     * Get Query that restricts a search to entities the user may see.
     * 
     * @return QueryBuilder with user-restriction query
     */
    protected QueryBuilder getUserRestrictionQuery() throws IOException {
        BoolQueryBuilder restrictionQueryBuilder = QueryBuilders.boolQuery();

        // add default-allowed states (published)
        restrictionQueryBuilder.should(QueryBuilders.termQuery("state", Entity.STATE_PUBLISHED));

        // get user-workspaces
        List<Workspace> userWorkspaces = authorizationService.retrieveUserWorkspaces();

        // get username
        User currentUser = authorizationService.getCurrentUser();
        String username = null;
        if (currentUser != null) {
            username = currentUser.getName();
        }

        if (StringUtils.isNotBlank(username)) {
            for (Workspace userWorkspace : userWorkspaces) {
                if (userWorkspace.getPermissions() != null &&
                        userWorkspace.getPermissions().getPermissions(username) != null) {
                    EnumSet<Permission> userPermissions = userWorkspace.getPermissions().getPermissions(username);
                    for (Permission userPermission : userPermissions) {
                        switch (userPermission) {
                        case READ_PENDING_METADATA:
                            restrictionQueryBuilder.should(getRestrictionQuery(Entity.STATE_PENDING, userWorkspace
                                    .getId()));
                            break;
                        case READ_SUBMITTED_METADATA:
                            restrictionQueryBuilder.should(getRestrictionQuery(Entity.STATE_SUBMITTED, userWorkspace
                                    .getId()));
                            break;
                        case READ_WITHDRAWN_METADATA:
                            restrictionQueryBuilder.should(getRestrictionQuery(Entity.STATE_WITHDRAWN, userWorkspace
                                    .getId()));
                            break;
                        default:
                            break;
                        }
                    }
                }
            }
        }

        return restrictionQueryBuilder;
    }

    /**
     * Generate a subquery that restrict to a certain workspace and entities with certain state
     * 
     * @param state
     * @param workspaceId
     * @return BoolQueryBuilder subRestrictionQuery
     */
    private BoolQueryBuilder getRestrictionQuery(String state, String workspaceId) {
        BoolQueryBuilder subRestrictionQueryBuilder = QueryBuilders.boolQuery();
        subRestrictionQueryBuilder.must(QueryBuilders.termQuery("state", state));
        subRestrictionQueryBuilder.must(QueryBuilders.termQuery("workspaceId", workspaceId));
        return subRestrictionQueryBuilder;
    }
}
