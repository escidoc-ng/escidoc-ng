
package net.objecthunter.larch.service.backend.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.Entity.EntityType;
import net.objecthunter.larch.model.Rights;
import net.objecthunter.larch.model.Rights.Right;
import net.objecthunter.larch.model.security.Group;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
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
        String username = null;
        if (currentUser != null) {
            username = currentUser.getName();
            if (currentUser.getGroups() != null && currentUser.getGroups().contains(Group.ADMINS)) {
                return QueryBuilders.matchAllQuery();
            }
        }

        // get user-permissions
        List<Entity> userPermissions = retrieveUserPermissions(currentUser, permissionId);

        BoolQueryBuilder restrictionQueryBuilder = QueryBuilders.boolQuery();

        // add restrictions for entities of type DATA
        // add default-allowed states (published)
        restrictionQueryBuilder.should(getDataEntitiesRestrictionQuery(EntityState.PUBLISHED, null));

        if (StringUtils.isNotBlank(username)) {
            for (Entity userPermission : userPermissions) {
                if (userPermission.getRights() != null &&
                        userPermission.getRights().getRights(username) != null) {
                    EnumSet<Right> userRights = userPermission.getRights().getRights(username);
                    for (Right userRight : userRights) {
                        switch (userRight) {
                        case READ_PENDING_METADATA:
                            restrictionQueryBuilder.should(getDataEntitiesRestrictionQuery(EntityState.PENDING,
                                    userPermission
                                            .getId()));
                            break;
                        case READ_SUBMITTED_METADATA:
                            restrictionQueryBuilder.should(getDataEntitiesRestrictionQuery(EntityState.SUBMITTED,
                                    userPermission
                                            .getId()));
                            break;
                        case READ_WITHDRAWN_METADATA:
                            restrictionQueryBuilder.should(getDataEntitiesRestrictionQuery(EntityState.WITHDRAWN,
                                    userPermission
                                            .getId()));
                            break;
                        default:
                            break;
                        }
                    }
                }
            }
        }

        // add restrictions for entities of type PERMISSION
        if (StringUtils.isNotBlank(username)) {
            for (Entity userPermission : userPermissions) {
                if (userPermission.getRights() != null &&
                        userPermission.getRights().getRights(username) != null) {
                    EnumSet<Right> userRights = userPermission.getRights().getRights(username);
                    for (Right userRight : userRights) {
                        switch (userRight) {
                        case READ_PERMISSION:
                            restrictionQueryBuilder.should(getPermissionEntitiesRestrictionQuery(userPermission
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
     * Retrieve all Workspaces where given user has rights for.
     * 
     * @param permissionId
     * @return List<Workspace>
     * @throws IOException
     */
    private List<Entity> retrieveUserPermissions(User currentUser, String permissionId) throws IOException {
        final List<Entity> userPermissions = new ArrayList<>();
        if (currentUser == null) {
            return userPermissions;
        }
        SearchResponse search;
        try {
            FilterBuilder filterBuilder = null;
            if (StringUtils.isEmpty(permissionId)) {
                filterBuilder = FilterBuilders.existsFilter("rights.rights." + currentUser.getName());
            } else {
                filterBuilder = FilterBuilders.andFilter(FilterBuilders.existsFilter("rights.rights." +
                        currentUser.getName()), FilterBuilders.idsFilter().addIds(permissionId), FilterBuilders.termFilter(EntitiesSearchField.TYPE.getFieldName(), EntityType.PERMISSION.name()));
            }
            search = client.prepareSearch(ElasticSearchEntityService.INDEX_ENTITIES)
                    .setTypes(ElasticSearchEntityService.INDEX_ENTITY_TYPE)
                    .setQuery(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
                            filterBuilder))
                    .addFields("id", "owner", "rights.rights." + currentUser.getName())
                    .execute()
                    .actionGet();
            if (search.getHits().getHits().length > 0) {
                for (SearchHit hit : search.getHits().getHits()) {
                    final Entity entity = new Entity();
                    entity.setId(hit.field("id").getValue());
                    if (hit.field("owner") != null) {
                        entity.setOwner(hit.field("owner").getValue());
                    }

                    Rights rights = new Rights();
                    if (hit.field("rights.rights." + currentUser.getName()) != null) {
                        for (Object o : hit.field("rights.rights." + currentUser.getName()).values()) {
                            String permissionName = (String) o;
                            rights.addRights(currentUser.getName(), Right
                                    .valueOf(permissionName));
                        }
                    }
                    entity.setRights(rights);
                    userPermissions.add(entity);
                }
            }
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        return userPermissions;
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
        subRestrictionQueryBuilder.must(QueryBuilders.termQuery(EntitiesSearchField.STATE.getFieldName(), state.name()));
        subRestrictionQueryBuilder.must(QueryBuilders.termQuery(EntitiesSearchField.TYPE.getFieldName(), EntityType.DATA.getName()));
        if (StringUtils.isNotBlank(permissionId)) {
            subRestrictionQueryBuilder.must(QueryBuilders.termQuery(EntitiesSearchField.PERMISSION_ID.getFieldName(), permissionId));
        }
        return subRestrictionQueryBuilder;
    }

    /**
     * Generate a subquery that restrict to a certain permission
     * 
     * @param state
     * @param permissionId
     * @return BoolQueryBuilder subRestrictionQuery
     */
    private BoolQueryBuilder getPermissionEntitiesRestrictionQuery(String permissionId) {
        BoolQueryBuilder subRestrictionQueryBuilder = QueryBuilders.boolQuery();
        subRestrictionQueryBuilder.must(QueryBuilders.termQuery(EntitiesSearchField.TYPE.getFieldName(), EntityType.PERMISSION.getName()));
        if (StringUtils.isNotBlank(permissionId)) {
            subRestrictionQueryBuilder.must(QueryBuilders.termQuery(EntitiesSearchField.PERMISSION_ID.getFieldName(), permissionId));
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
