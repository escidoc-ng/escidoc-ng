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

package net.objecthunter.larch.service.impl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.objecthunter.larch.annotations.PreAuth;
import net.objecthunter.larch.annotations.WorkspacePermission;
import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.model.WorkspacePermissions;
import net.objecthunter.larch.model.WorkspacePermissions.Permission;
import net.objecthunter.larch.model.security.Group;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.AuthorizationService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchWorkspaceService;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.util.MethodInvocationUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DefaultAuthorizationService implements AuthorizationService {

    @Autowired
    private Client client;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public boolean hasPermission(User user, Workspace ws,
            WorkspacePermissions.Permission... permissionsToCheck) {
        // check for role ADMIN
        if (user != null && user.getGroups() != null &&
                user.getGroups().contains(Group.ADMINS)) {
            return true;
        }

        // get Permissions for user and workspace
        EnumSet<WorkspacePermissions.Permission> currentPermissions =
                EnumSet.noneOf(WorkspacePermissions.Permission.class);
        if (user != null && ws != null && ws.getPermissions() != null &&
                ws.getPermissions().getPermissions() != null &&
                ws.getPermissions().getPermissions().get(user.getName()) != null) {
            currentPermissions = ws.getPermissions().getPermissions().get(user.getName());
        }

        // add default-permissions
        currentPermissions.addAll(getDefaultPermissions());

        return currentPermissions.containsAll(Arrays.asList(permissionsToCheck));
    }

    @Override
    public boolean hasCurrentUserPermission(Workspace ws, WorkspacePermissions.Permission... permissionsToCheck) {
        return hasPermission(this.getCurrentUser(), ws, permissionsToCheck);
    }

    @Override
    public void checkCurrentUserPermission(Workspace ws, WorkspacePermissions.Permission... permissionsToCheck) {
        final User u = this.getCurrentUser();
        if (u != null && u.getGroups() != null && u.getGroups().contains(Group.ADMINS)) {
            return;
        }
        if (!hasPermission(u, ws, permissionsToCheck)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    @Override
    public void checkCurrentUserPermission(String workspaceId, WorkspacePermissions.Permission... permissionsToCheck)
            throws IOException {
        final GetResponse get =
                this.client.prepareGet(ElasticSearchWorkspaceService.INDEX_WORKSPACES,
                        ElasticSearchWorkspaceService.INDEX_WORKSPACE_TYPE, workspaceId)
                        .execute()
                        .actionGet();

        /* check if the workspace does exist */
        if (!get.isExists()) {
            throw new NotFoundException("The workspace with id '" + workspaceId + "' does not exist");
        }
        final Workspace ws = this.mapper.readValue(get.getSourceAsString(), Workspace.class);
        this.checkCurrentUserPermission(ws, permissionsToCheck);
    }

    @Override
    public WorkspacePermissions.Permission metadataReadPermissions(Entity e) throws IOException {
        if (e.getState() != null) {
            if (e.getState().equals(Entity.STATE_PENDING)) {
                return WorkspacePermissions.Permission.READ_PENDING_METADATA;
            } else if (e.getState().equals(Entity.STATE_SUBMITTED)) {
                return WorkspacePermissions.Permission.READ_SUBMITTED_METADATA;
            } else if (e.getState().equals(Entity.STATE_PUBLISHED)) {
                return WorkspacePermissions.Permission.READ_PUBLISHED_METADATA;
            } else {
                throw new IOException("Entity has unknown state: " + e.getState());
            }
        } else {
            throw new IOException("State of Entity may not be null");
        }
    }

    @Override
    public WorkspacePermissions.Permission[] metadataReadWritePermissions(Entity e) throws IOException {
        return new WorkspacePermissions.Permission[] { this.metadataReadPermissions(e),
            this.metadataWritePermissions(e) };
    }

    @Override
    public WorkspacePermissions.Permission metadataWritePermissions(Entity e) throws IOException {
        if (e.getState() != null) {
            if (e.getState().equals(Entity.STATE_PENDING)) {
                return WorkspacePermissions.Permission.WRITE_PENDING_METADATA;
            } else if (e.getState().equals(Entity.STATE_SUBMITTED)) {
                return WorkspacePermissions.Permission.WRITE_SUBMITTED_METADATA;
            } else if (e.getState().equals(Entity.STATE_PUBLISHED)) {
                return WorkspacePermissions.Permission.WRITE_PUBLISHED_METADATA;
            } else {
                throw new IOException("Entity has unknown state: " + e.getState());
            }
        } else {
            throw new IOException("State of Entity may not be null");
        }
    }

    @Override
    public List<Workspace> retrieveUserWorkspaces(String workspaceId) throws IOException {
        final List<Workspace> userWorkspaces = new ArrayList<>();
        User currentUser = getCurrentUser();
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

    @Override
    public User getCurrentUser() {
        if (SecurityContextHolder.getContext() == null ||
                SecurityContextHolder.getContext().getAuthentication() == null ||
                SecurityContextHolder.getContext().getAuthentication().getPrincipal() == null ||
                !(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof User)) {
            return null;
        }
        return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Override
    public void preauthorize(Method method, String workspaceId) throws IOException {
        PreAuth preAuth = method
                .getAnnotation(PreAuth.class);
        if (preAuth != null) {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            // Anonymous user may not access protected methods
            if (a == null || a instanceof AnonymousAuthenticationToken) {
                throw new InsufficientAuthenticationException("No user logged in");
            }
            // handle securityExpression
            DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
            Expression accessExpression =
                    handler.getExpressionParser().parseExpression(preAuth.springSecurityExpression());
            if (!ExpressionUtils.evaluateAsBoolean(accessExpression, handler.createEvaluationContext(
                    a, MethodInvocationUtils.createFromClass(method.getDeclaringClass(), method
                            .getName())))) {
                throw new AccessDeniedException("Access denied");
            }

            // handle workspacePermission
            WorkspacePermission workspacePermission = preAuth.workspacePermission();
            if (workspacePermission.workspacePermissions().length > 0) {
                if (StringUtils.isBlank(workspaceId)) {
                    throw new AccessDeniedException("No WorkspaceId provided");
                }
                // convert Strings to Permissions
                Permission[] ps = new Permission[workspacePermission.workspacePermissions().length];
                for (int i = 0; i < workspacePermission.workspacePermissions().length; i++) {
                    ps[i] = Permission.valueOf(workspacePermission.workspacePermissions()[i]);
                }
                checkCurrentUserPermission(workspaceId, ps);
            }
        }
    }

    @Override
    public void postauthorize(Method method, Object result) throws IOException {
    }

    /**
     * Get the Permissions for everybody (also anonymous user).
     * 
     * @return Collection of default-permissions
     */
    private Collection<Permission> getDefaultPermissions() {
        Set<Permission> defaultPermissions = new HashSet<Permission>();
        defaultPermissions.add(Permission.READ_PUBLISHED_METADATA);
        defaultPermissions.add(Permission.READ_PUBLISHED_BINARY);
        return defaultPermissions;
    }
}
