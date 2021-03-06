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

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import de.escidocng.exceptions.NotFoundException;
import de.escidocng.model.SearchResult;
import de.escidocng.model.security.User;
import de.escidocng.model.security.UserRequest;
import de.escidocng.model.security.role.Role;
import de.escidocng.model.security.role.Role.RoleName;
import de.escidocng.model.security.role.Role.RoleRight;
import de.escidocng.service.AuthorizationService;
import de.escidocng.service.CredentialsService;
import de.escidocng.service.backend.BackendCredentialsService;
import de.escidocng.service.backend.elasticsearch.ElasticSearchCredentialsService.UsersSearchField;
import de.escidocng.service.backend.elasticsearch.queryrestriction.QueryRestrictionFactory;
import de.escidocng.service.backend.elasticsearch.queryrestriction.RoleQueryRestriction;

/**
 * Default implementation of a {@link de.escidocng.service.CredentialsService}.
 */
public class DefaultCredentialsService implements CredentialsService {

    @Autowired
    private BackendCredentialsService backendCredentialsService;

    @Autowired
    private AuthorizationService defaultAuthorizationService;

    @Override
    public User createUser(User u) throws IOException {
        return backendCredentialsService.createUser(u);
    }

    @Override
    public UserRequest createNewUserRequest(User u) throws IOException {
        return backendCredentialsService.createNewUserRequest(u);
    }

    @Override
    public void updateUser(User u) throws IOException {
        backendCredentialsService.updateUser(u);
    }

    @Override
    public void setRoles(String username, List<Role> roles) throws IOException {
        backendCredentialsService.setRoles(username, roles);
    }

    @Override
    public void setRight(String username, RoleName roleName, String anchorId, List<RoleRight> rights) throws IOException {
        backendCredentialsService.setRight(username, roleName, anchorId, rights);
    }

    @Override
    public void deleteUser(String name) throws IOException {
        backendCredentialsService.deleteUser(name);
    }

    @Override
    public User retrieveUser(String name) throws IOException {
        return backendCredentialsService.retrieveUser(name);
    }

    @Override
    public User retrieveCurrentUser() throws IOException {
        User user = defaultAuthorizationService.getCurrentUser();
        if (user == null) {
            throw new NotFoundException("No user logged in");
        }
        return user;
    }

    @Override
    public SearchResult searchUsers(String query, int offset, int maxRecords) throws IOException {
        // add user restriction
        StringBuilder queryBuilder = new StringBuilder("");
        if (StringUtils.isNotBlank(query)) {
            queryBuilder.append("(").append(query).append(") AND ");
        }
        queryBuilder.append(getUsersUserRestrictionQuery());
        return backendCredentialsService.searchUsers(queryBuilder.toString(), offset, maxRecords);
    }

    @Override
    public UserRequest retrieveUserRequest(String token) throws IOException {
        return backendCredentialsService.retrieveUserRequest(token);
    }

    @Override
    public User createUser(String token, String password, String passwordRepeat) throws IOException {
        return backendCredentialsService.createUser(token, password, passwordRepeat);
    }

    @Override
    public void deleteUserRequest(String token) throws IOException {
        backendCredentialsService.deleteUserRequest(token);
    }

    /**
     * Get Query that restricts a search to users the user may see.
     * 
     * @return QueryBuilder with user-restriction query
     */
    protected String getUsersUserRestrictionQuery() throws IOException {
        User currentUser = defaultAuthorizationService.getCurrentUser();
        StringBuilder restrictionQueryBuilder = new StringBuilder("(");
        if (currentUser == null) {
            //restrict to nothing
            restrictionQueryBuilder.append(UsersSearchField.NAME.getFieldName()).append(":NONEXISTING");
            restrictionQueryBuilder.append(")");
            return restrictionQueryBuilder.toString();
        } else {
            // user may see himself
            restrictionQueryBuilder.append(UsersSearchField.NAME.getFieldName()).append(":").append(currentUser.getName());
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

}
