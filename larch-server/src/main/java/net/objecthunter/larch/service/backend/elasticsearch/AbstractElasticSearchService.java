
package net.objecthunter.larch.service.backend.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.model.WorkspacePermissions;
import net.objecthunter.larch.model.WorkspacePermissions.Permission;
import net.objecthunter.larch.model.security.Group;
import net.objecthunter.larch.model.security.User;

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

    private static final String STATE_FIELD = "state";

    private static final String WORKSPACE_ID_FIELD = "workspaceId";

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
    protected QueryBuilder getEntitiesUserRestrictionQuery(String workspaceId) throws IOException {
        // get username and check for ADMIN-Role
        User currentUser = getCurrentUser();
        String username = null;
        if (currentUser != null) {
            username = currentUser.getName();
            if (currentUser.getGroups() != null && currentUser.getGroups().contains(Group.ADMINS)) {
                return QueryBuilders.matchAllQuery();
            }
        }

        BoolQueryBuilder restrictionQueryBuilder = QueryBuilders.boolQuery();

        // add default-allowed states (published)
        restrictionQueryBuilder.should(QueryBuilders.termQuery(STATE_FIELD, Entity.STATE_PUBLISHED));

        // get user-workspaces
        List<Workspace> userWorkspaces = retrieveUserWorkspaces(currentUser, workspaceId);

        if (StringUtils.isNotBlank(username)) {
            for (Workspace userWorkspace : userWorkspaces) {
                if (userWorkspace.getPermissions() != null &&
                        userWorkspace.getPermissions().getPermissions(username) != null) {
                    EnumSet<Permission> userPermissions = userWorkspace.getPermissions().getPermissions(username);
                    for (Permission userPermission : userPermissions) {
                        switch (userPermission) {
                        case READ_PENDING_METADATA:
                            restrictionQueryBuilder.should(getEntitiesRestrictionQuery(Entity.STATE_PENDING,
                                    userWorkspace
                                            .getId()));
                            break;
                        case READ_SUBMITTED_METADATA:
                            restrictionQueryBuilder.should(getEntitiesRestrictionQuery(Entity.STATE_SUBMITTED,
                                    userWorkspace
                                            .getId()));
                            break;
                        case READ_WITHDRAWN_METADATA:
                            restrictionQueryBuilder.should(getEntitiesRestrictionQuery(Entity.STATE_WITHDRAWN,
                                    userWorkspace
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
     * Get Query that restricts a search to workspaces the user may see.
     * 
     * @return QueryBuilder with user-restriction query
     */
    protected QueryBuilder getWorkspacesUserRestrictionQuery() throws IOException {
        // get username and check for ADMIN-Role
        User currentUser = getCurrentUser();
        String username = null;
        if (currentUser != null) {
            username = currentUser.getName();
            if (currentUser.getGroups() != null && currentUser.getGroups().contains(Group.ADMINS)) {
                return QueryBuilders.matchAllQuery();
            }
        }

        BoolQueryBuilder restrictionQueryBuilder = QueryBuilders.boolQuery();

        // get user-workspaces
        List<Workspace> userWorkspaces = retrieveUserWorkspaces(currentUser, null);

        if (StringUtils.isNotBlank(username)) {
            for (Workspace userWorkspace : userWorkspaces) {
                if (userWorkspace.getPermissions() != null &&
                        userWorkspace.getPermissions().getPermissions(username) != null) {
                    EnumSet<Permission> userPermissions = userWorkspace.getPermissions().getPermissions(username);
                    if (userPermissions.contains(Permission.READ_WORKSPACE)) {
                        restrictionQueryBuilder.should(QueryBuilders.termQuery(WORKSPACE_ID_FIELD, userWorkspace
                                .getId()));
                    }
                }
            }
        }

        if (!restrictionQueryBuilder.hasClauses()) {
            // add default-restriction to restrict to 0
            restrictionQueryBuilder.should(QueryBuilders.termQuery(WORKSPACE_ID_FIELD, "notprovided"));
        }

        return restrictionQueryBuilder;
    }

    /**
     * Retrieve all Workspaces where given user has rights for.
     * 
     * @param workspaceId
     * @return List<Workspace>
     * @throws IOException
     */
    private List<Workspace> retrieveUserWorkspaces(User currentUser, String workspaceId) throws IOException {
        final List<Workspace> userWorkspaces = new ArrayList<>();
        if (currentUser == null) {
            return userWorkspaces;
        }
        SearchResponse search;
        try {
            FilterBuilder filterBuilder = null;
            if (StringUtils.isEmpty(workspaceId)) {
                filterBuilder = FilterBuilders.existsFilter("permissions.permissions." + currentUser.getName());
            } else {
                filterBuilder = FilterBuilders.andFilter(FilterBuilders.existsFilter("permissions.permissions." +
                        currentUser.getName()), FilterBuilders.idsFilter().addIds(workspaceId));
            }
            search = client.prepareSearch(ElasticSearchWorkspaceService.INDEX_WORKSPACES)
                    .setTypes(ElasticSearchWorkspaceService.INDEX_WORKSPACE_TYPE)
                    .setQuery(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
                            filterBuilder))
                    .addFields("id", "name", "owner", "permissions.permissions." + currentUser.getName())
                    .execute()
                    .actionGet();
            if (search.getHits().getHits().length > 0) {
                for (SearchHit hit : search.getHits().getHits()) {
                    final Workspace workspace = new Workspace();
                    workspace.setId(hit.field("id").getValue());
                    workspace.setName(hit.field("name").getValue());
                    workspace.setOwner(hit.field("owner").getValue());

                    WorkspacePermissions workspacePermissions = new WorkspacePermissions();
                    if (hit.field("permissions.permissions." + currentUser.getName()) != null) {
                        for (Object o : hit.field("permissions.permissions." + currentUser.getName()).values()) {
                            String permissionName = (String) o;
                            workspacePermissions.addPermissions(currentUser.getName(), Permission
                                    .valueOf(permissionName));
                        }
                    }
                    workspace.setPermissions(workspacePermissions);
                    userWorkspaces.add(workspace);
                }
            }
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        return userWorkspaces;
    }

    /**
     * Generate a subquery that restrict to a certain workspace and entities with certain state
     * 
     * @param state
     * @param workspaceId
     * @return BoolQueryBuilder subRestrictionQuery
     */
    private BoolQueryBuilder getEntitiesRestrictionQuery(String state, String workspaceId) {
        BoolQueryBuilder subRestrictionQueryBuilder = QueryBuilders.boolQuery();
        subRestrictionQueryBuilder.must(QueryBuilders.termQuery(STATE_FIELD, state));
        subRestrictionQueryBuilder.must(QueryBuilders.termQuery(WORKSPACE_ID_FIELD, workspaceId));
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
