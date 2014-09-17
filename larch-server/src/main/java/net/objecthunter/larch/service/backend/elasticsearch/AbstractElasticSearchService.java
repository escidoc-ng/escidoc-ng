
package net.objecthunter.larch.service.backend.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.Entity.EntityType;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.role.TestRole.RoleName;
import net.objecthunter.larch.model.security.role.TestUserRole;
import net.objecthunter.larch.model.security.role.TestUserRole.UserRoleRight;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

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
    protected QueryBuilder getEntitesUserRestrictionQuery() throws IOException {
        return getEntitiesUserRestrictionQuery(null);
    }

    /**
     * Get Query that restricts a search to entities the user may see.
     * 
     * @return QueryBuilder with user-restriction query
     */
    protected QueryBuilder getEntitiesUserRestrictionQuery(String permissionId) throws IOException {
        // get username and check for ADMIN-Role
        User currentUser = getCurrentUser();
        TestUserRole role = null;
        String username = null;
        if (currentUser != null) {
            username = currentUser.getName();
            if (currentUser.getRoles() != null && currentUser.hasRole(RoleName.ADMIN)) {
                return QueryBuilders.matchAllQuery();
            }
            // get user-permissions
            if (currentUser.getRoles() != null) {
                role = (TestUserRole)currentUser.getRole(RoleName.USER);
            }
        }

        BoolQueryBuilder restrictionQueryBuilder = QueryBuilders.boolQuery();
        //restrict to nothing
        restrictionQueryBuilder.should(QueryBuilders.termQuery(EntitiesSearchField.STATE.getFieldName(), "NONEXISTING"));

        // add restrictions
        if (StringUtils.isNotBlank(username) && role != null && role.getRights() != null) {
            for (Entry<String, List<UserRoleRight>> rightSet : role.getRights().entrySet()) {
                List<UserRoleRight> userRights = rightSet.getValue();
                for (UserRoleRight userRight : userRights) {
                    if (UserRoleRight.READ_PENDING_METADATA.equals(userRight)) {
                        restrictionQueryBuilder.should(getDataEntitiesRestrictionQuery(EntityState.PENDING,
                                rightSet.getKey()));
                    } else if (UserRoleRight.READ_PUBLISHED_METADATA.equals(userRight)) {
                        restrictionQueryBuilder.should(getDataEntitiesRestrictionQuery(EntityState.PUBLISHED,
                                rightSet.getKey()));
                    } else if (UserRoleRight.READ_SUBMITTED_METADATA.equals(userRight)) {
                        restrictionQueryBuilder.should(getDataEntitiesRestrictionQuery(EntityState.SUBMITTED,
                                rightSet.getKey()));
                    } else if (UserRoleRight.READ_WITHDRAWN_METADATA.equals(userRight)) {
                        restrictionQueryBuilder.should(getDataEntitiesRestrictionQuery(EntityState.WITHDRAWN,
                                rightSet.getKey()));
                    }
                }
            }
        }

        return restrictionQueryBuilder;
    }

    /**
     * Generate a subquery that restrict to a certain permission and entities with certain state
     * 
     * @param state
     * @param permissionId
     * @return BoolQueryBuilder subRestrictionQuery
     */
    private BoolQueryBuilder getDataEntitiesRestrictionQuery(EntityState state, String permissionId) {
        BoolQueryBuilder subRestrictionQueryBuilder = QueryBuilders.boolQuery();
        subRestrictionQueryBuilder.must(QueryBuilders.termQuery(EntitiesSearchField.STATE.getFieldName(), state
                .name()));
        subRestrictionQueryBuilder.mustNot(QueryBuilders.termQuery(EntitiesSearchField.TYPE.getFieldName(),
                EntityType.AREA.getName()));
        subRestrictionQueryBuilder.mustNot(QueryBuilders.termQuery(EntitiesSearchField.TYPE.getFieldName(),
                EntityType.PERMISSION.getName()));
        if (StringUtils.isNotBlank(permissionId)) {
            subRestrictionQueryBuilder.must(QueryBuilders.termQuery(EntitiesSearchField.PERMISSION_ID.getFieldName(),
                    permissionId));
        }
        return subRestrictionQueryBuilder;
    }

    /**
     * Get currently logged in User or null if no user is logged in.
     * 
     * @return User loggen id user
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
