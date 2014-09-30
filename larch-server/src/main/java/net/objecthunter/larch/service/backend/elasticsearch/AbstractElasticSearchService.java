
package net.objecthunter.larch.service.backend.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;
import net.objecthunter.larch.service.backend.elasticsearch.queryrestriction.QueryRestrictionFactory;
import net.objecthunter.larch.service.backend.elasticsearch.queryrestriction.RoleQueryRestriction;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;

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

    /**
     * Get Query that restricts a search to entities the user may see.
     * 
     * @return QueryBuilder with user-restriction query
     */
    protected String getEntitesUserRestrictionQuery() throws IOException {
        User currentUser = getCurrentUser();
        StringBuilder restrictionQueryBuilder = new StringBuilder("(");
        if (currentUser == null || currentUser.getRoles() == null || currentUser.getRoles().isEmpty()) {
            // restrict to nothing
            restrictionQueryBuilder.append(EntitiesSearchField.STATE.getFieldName()).append(":NONEXISTING");
            restrictionQueryBuilder.append(")");
            return restrictionQueryBuilder.toString();
        } else {
            int counter = 0;
            for (Role role : currentUser.getRoles()) {
                if (counter > 0) {
                    restrictionQueryBuilder.append(" OR ");
                }
                RoleQueryRestriction roleQueryRestriction = QueryRestrictionFactory.getRoleQueryRestriction(role);
                restrictionQueryBuilder.append(roleQueryRestriction.getEntitiesRestrictionQuery());
                counter++;
            }
        }
        restrictionQueryBuilder.append(")");
        return restrictionQueryBuilder.toString();
    }

    /**
     * Get Query that restricts a search to users the user may see.
     * 
     * @return QueryBuilder with user-restriction query
     */
    protected String getUsersUserRestrictionQuery() throws IOException {
        User currentUser = getCurrentUser();
        StringBuilder restrictionQueryBuilder = new StringBuilder("(");
        if (currentUser == null) {
            //restrict to nothing
            restrictionQueryBuilder.append("name:NONEXISTING");
            restrictionQueryBuilder.append(")");
            return restrictionQueryBuilder.toString();
        } else {
            // user may see himself
            restrictionQueryBuilder.append("name:").append(currentUser.getName());
            if (currentUser.getRoles() != null) {
                for (Role role : currentUser.getRoles()) {
                    RoleQueryRestriction roleQueryRestriction = QueryRestrictionFactory.getRoleQueryRestriction(role);
                    restrictionQueryBuilder.append(" OR ");
                    restrictionQueryBuilder.append(roleQueryRestriction.getUsersRestrictionQuery());
                }
            }
        }
        restrictionQueryBuilder.append(")");
        return restrictionQueryBuilder.toString();
    }

    /**
     * Get currently logged in User or null if no user is logged in.
     * 
     * @return User logged in user
     */
    private User getCurrentUser() {
        if (SecurityContextHolder.getContext() == null ||
                SecurityContextHolder.getContext().getAuthentication() == null ||
                SecurityContextHolder.getContext().getAuthentication().getPrincipal() == null ||
                !(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof User)) {
            return null;
        }
        return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

}
