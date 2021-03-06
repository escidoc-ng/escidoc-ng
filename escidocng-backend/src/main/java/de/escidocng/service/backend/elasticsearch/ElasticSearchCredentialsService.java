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

package de.escidocng.service.backend.elasticsearch;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.escidocng.exceptions.AlreadyExistsException;
import de.escidocng.exceptions.InvalidParameterException;
import de.escidocng.exceptions.NotFoundException;
import de.escidocng.model.Entity;
import de.escidocng.model.SearchResult;
import de.escidocng.model.ContentModel.FixedContentModel;
import de.escidocng.model.security.PermissionAnchorType;
import de.escidocng.model.security.User;
import de.escidocng.model.security.UserRequest;
import de.escidocng.model.security.role.AdminRole;
import de.escidocng.model.security.role.Right;
import de.escidocng.model.security.role.Role;
import de.escidocng.model.security.role.UserAdminRole;
import de.escidocng.model.security.role.UserRole;
import de.escidocng.model.security.role.Role.RoleName;
import de.escidocng.model.security.role.Role.RoleRight;
import de.escidocng.service.MailService;
import de.escidocng.service.backend.BackendCredentialsService;
import de.escidocng.service.backend.BackendEntityService;

/**
 * Implementation of a spring-security {@link org.springframework.security.authentication.AuthenticationManager} which
 * uses ElasticSearch indices as a persistence layer
 */
public class ElasticSearchCredentialsService extends AbstractElasticSearchService
        implements AuthenticationManager, BackendCredentialsService {

    private static final Logger log = LoggerFactory.getLogger(ElasticSearchCredentialsService.class);

    public static final String INDEX_USERS = "users";

    public static final String INDEX_USERS_TYPE = "user";

    public static final String INDEX_USERS_REQUEST = "user_requests";

    public static final String INDEX_USERS_REQUEST_TYPE = "user_request";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MailService mailService;

    @Autowired
    private BackendEntityService backendEntityService;

    @PostConstruct
    public void setup() throws IOException {
        this.checkAndOrCreateIndex(INDEX_USERS);
        this.checkAndOrCreateIndex(INDEX_USERS_REQUEST);
        checkAndOrCreateDefaultUsers();
    }

    private void checkAndOrCreateDefaultUsers() throws IOException {
        long count = client.prepareCount(INDEX_USERS).execute().actionGet().getCount();
        if (count == 0) {
            try {
                final User admin = new User();
                admin.setPwhash(DigestUtils.sha256Hex("admin"));
                admin.setName("admin");
                admin.setFirstName("Generic");
                admin.setLastName("Superuser");
                admin.setRole(new AdminRole());
                client
                        .prepareIndex(INDEX_USERS, "user", admin.getName())
                        .setSource(mapper.writeValueAsBytes(admin))
                        .execute().actionGet();

                final User user = new User();
                user.setPwhash(DigestUtils.sha256Hex("user"));
                user.setName("user");
                user.setFirstName("Generic");
                user.setLastName("User");
                client
                        .prepareIndex(INDEX_USERS, "user", user.getName()).setSource(mapper.writeValueAsBytes(user))
                        .execute()
                        .actionGet();
            } catch (ElasticsearchException ex) {
                throw new IOException(ex.getMostSpecificCause().getMessage());
            }
        }
    }

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        final String name = (String) auth.getPrincipal();
        final String hash = DigestUtils.sha256Hex((String) auth.getCredentials());
        final GetResponse get;
        try {
            get = client.prepareGet(INDEX_USERS, INDEX_USERS_TYPE, name).execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new AuthenticationServiceException(ex.getMostSpecificCause().getMessage());
        }
        if (get.isExists()) {
            try {
                User u = mapper.readValue(get.getSourceAsBytes(), User.class);
                u = addDefaultRights(u);
                if (u.getPwhash().equals(hash)) {
                    String[] roles = null;
                    if (u.getRoles() != null && u.getRoles().size() > 0) {
                        roles = new String[u.getRoles().size()];
                        int i = 0;
                        for (Role role : u.getRoles()) {
                            roles[i] = role.getRoleName().name();
                            i++;
                        }
                    } else {
                        roles = new String[] { "ROLE_IDENTIFIED" };
                    }
                    return new UsernamePasswordAuthenticationToken(u, auth.getCredentials(),
                            AuthorityUtils.createAuthorityList(roles));
                }
            } catch (IOException e) {
                throw new BadCredentialsException("Unable to authenticate");
            }
        }
        throw new BadCredentialsException("Unable to authenticate");
    }

    @Override
    public User createUser(User u) throws IOException {
        if (u.getName() == null || u.getName().isEmpty()) {
            throw new InvalidParameterException("User name can not be null");
        }
        if (u.getEmail() == null || u.getEmail().isEmpty()) {
            throw new InvalidParameterException("Email can not be empty");
        }
        try {
            final GetResponse get =
                    this.client.prepareGet(INDEX_USERS, INDEX_USERS_TYPE, u.getName()).execute().actionGet();
            if (get.isExists()) {
                throw new AlreadyExistsException("The user " + u.getName() + " does already exist");
            }
            this.client
                    .prepareIndex(INDEX_USERS, INDEX_USERS_TYPE, u.getName()).setSource(
                            mapper.writeValueAsBytes(u))
                    .execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        this.refreshIndex(INDEX_USERS);
        return u;
    }

    @Override
    public UserRequest createNewUserRequest(User u) throws IOException {
        if (u.getName() == null || u.getName().isEmpty()) {
            throw new InvalidParameterException("User name must be set");
        }
        if (u.getEmail() == null || u.getEmail().isEmpty()) {
            throw new InvalidParameterException("User's email can not be empty");
        }
        if (this.isExistingUser(u.getName())) {
            throw new AlreadyExistsException("The user " + u.getName() + " does already exist");
        }
        final UserRequest request = new UserRequest();
        request.setUser(u);
        request.setValidUntil(ZonedDateTime.now().plusWeeks(1));
        request.setToken(RandomStringUtils.randomAlphanumeric(128));
        if (mailService.isEnabled()) {
            this.mailService.sendUserRequest(request);
        }
        try {
            this.client
                    .prepareIndex(INDEX_USERS_REQUEST, INDEX_USERS_REQUEST_TYPE)
                    .setSource(this.mapper.writeValueAsBytes(request)).setId(request.getToken()).execute()
                    .actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        this.refreshIndex(INDEX_USERS_REQUEST);
        return request;
    }

    @Override
    public void updateUser(User u) throws IOException {
        if (u.getName() == null || u.getName().isEmpty()) {
            throw new InvalidParameterException("User name can not be null");
        }
        if (u.getEmail() == null || u.getEmail().isEmpty()) {
            throw new InvalidParameterException("Email can not be empty");
        }
        try {
            final GetResponse get =
                    this.client.prepareGet(INDEX_USERS, INDEX_USERS_TYPE, u.getName()).execute().actionGet();
            if (!get.isExists()) {
                throw new NotFoundException("The user " + u.getName() + " does not exist");
            }

            // Roles cannot be set with update user
            User oldUser = mapper.readValue(get.getSourceAsBytes(), User.class);
            u.setRoles(oldUser.getRoles());

            this.client
                    .prepareIndex(INDEX_USERS, INDEX_USERS_TYPE, u.getName()).setSource(
                            mapper.writeValueAsBytes(u))
                    .execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        this.refreshIndex(INDEX_USERS);
    }

    @Override
    public void setRoles(String username, List<Role> roles) throws IOException {
        // check parameters
        if (StringUtils.isBlank(username)) {
            throw new InvalidParameterException("User name can not be null");
        }
        validateRoles(roles);
        try {
            final GetResponse get =
                    this.client.prepareGet(INDEX_USERS, INDEX_USERS_TYPE, username).execute().actionGet();
            if (!get.isExists()) {
                throw new NotFoundException("The user " + username + " does not exist");
            }

            User user = mapper.readValue(get.getSourceAsBytes(), User.class);
            user.setRoles(roles);

            this.client
                    .prepareIndex(INDEX_USERS, INDEX_USERS_TYPE, user.getName()).setSource(
                            mapper.writeValueAsBytes(user))
                    .execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        this.refreshIndex(INDEX_USERS);
    }

    @Override
    public void setRight(String username, RoleName roleName, String anchorId, List<RoleRight> rights)
            throws IOException {
        // check parameters
        if (StringUtils.isBlank(username)) {
            throw new InvalidParameterException("User name can not be null");
        }
        if (roleName == null) {
            throw new InvalidParameterException("name of role may not be null");
        }
        if (anchorId == null) {
            throw new InvalidParameterException("anchorId may not be null");
        }
        
        if (!RoleName.ROLE_ADMIN.equals(roleName) && rights != null && !rights.isEmpty()) {
            Role roleToSet = Role.getRoleObject(roleName);
            roleToSet.setRights(Arrays.asList(new Right(anchorId, rights)));
            validateRoles(Arrays.asList(roleToSet));
        }

        try {
            final GetResponse get =
                    this.client.prepareGet(INDEX_USERS, INDEX_USERS_TYPE, username).execute().actionGet();
            if (!get.isExists()) {
                throw new NotFoundException("The user " + username + " does not exist");
            }

            User user = mapper.readValue(get.getSourceAsBytes(), User.class);

            Role existingRole = user.getRole(roleName);
            if (RoleName.ROLE_ADMIN.equals(roleName)) {
                // handle admin role
                if (existingRole == null) {
                    user.setRole(Role.getRoleObject(roleName));
                } else {
                    user.removeRole(roleName);
                }
            } else {
                // handle other roles
                if (existingRole != null && rights != null && !rights.isEmpty()) {
                    List<Right> expandedRights = existingRole.getRights();
                    if (expandedRights == null) {
                        expandedRights = new ArrayList<Right>();
                    }
                    expandedRights.add(new Right(anchorId, rights));
                    existingRole.setRights(expandedRights);
                } else if (existingRole != null && (rights == null || rights.isEmpty())) {
                    List<Right> expandedRights = existingRole.getRights();
                    if (expandedRights == null) {
                        expandedRights = new ArrayList<Right>();
                    }
                    //remove right
                    int index = -1;
                    for (int i = 0; i < expandedRights.size(); i++) {
                        if (anchorId.equals(expandedRights.get(i).getAnchorId())) {
                            index = i;
                            break;
                        }
                    }
                    if (index > -1) {
                        expandedRights.remove(index);
                    }
                    if (expandedRights.isEmpty()) {
                        user.removeRole(existingRole.getRoleName());
                    }
                } else if (existingRole == null && rights != null && !rights.isEmpty()) {
                    Role newRole = Role.getRoleObject(roleName);
                    List<Right> newRights = new ArrayList<Right>();
                    newRights.add(new Right(anchorId, rights));
                    newRole.setRights(newRights);
                    user.setRole(newRole);
                }
            }

            this.client
                    .prepareIndex(INDEX_USERS, INDEX_USERS_TYPE, user.getName()).setSource(
                            mapper.writeValueAsBytes(user))
                    .execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        this.refreshIndex(INDEX_USERS);
    }

    @Override
    public void deleteUser(String name) throws IOException {
        if (name == null) {
            throw new InvalidParameterException("User name can not be null");
        }
        if (this.isLastAdminUser(name)) {
            throw new InvalidParameterException("Unable to delete last remaining Administrator");
        }
        try {
            final GetResponse get = this.client.prepareGet(INDEX_USERS, INDEX_USERS_TYPE, name).execute().actionGet();
            if (!get.isExists()) {
                throw new NotFoundException("The user " + name + " does not exist");
            }
            this.client.prepareDelete(INDEX_USERS, INDEX_USERS_TYPE, name).execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        this.refreshIndex(INDEX_USERS);
    }

    /**
     * Check if the User with the given username is the last user in the system that has the Role ROLE_ADMIN.
     * 
     * @param name
     * @return boolean.
     * @throws IOException
     */
    private boolean isLastAdminUser(String name) throws IOException {
        User user = this.retrieveUser(name);
        if (user.getRoles() == null || !user.hasRole(RoleName.ROLE_ADMIN)) {
            return false;
        }
        final CountResponse resp;
        try {
            resp =
                    this.client
                            .prepareCount(INDEX_USERS)
                            .setQuery(
                                    QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(),
                                            FilterBuilders.termFilter("groups.name",
                                                    RoleName.ROLE_ADMIN))).execute()
                            .actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        return resp.getCount() < 2; // at least one other admin must exist
    }

    @Override
    public User retrieveUser(String name) throws IOException {
        final GetResponse get;
        try {
            get = this.client.prepareGet(INDEX_USERS, INDEX_USERS_TYPE, name).execute().actionGet();
            if (!get.isExists()) {
                throw new NotFoundException("User '" + name + "' does not exist");
            }
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        return mapper.readValue(get.getSourceAsBytes(), User.class);
    }

    @Override
    public SearchResult searchUsers(String query, int offset, int maxRecords) throws IOException {
        final long time = System.currentTimeMillis();
        final SearchResponse resp;
        if (StringUtils.isBlank(query)) {
            query = "*:*";
        }
        QueryStringQueryBuilder builder = QueryBuilders.queryString(query);
        try {
            resp =
                    this.client.prepareSearch(INDEX_USERS).setQuery(builder).execute()
                            .actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }

        final SearchResult result = new SearchResult();

        final List<User> users = new ArrayList<>();
        for (final SearchHit hit : resp.getHits()) {
            users.add(mapper.readValue(hit.getSourceAsString(), User.class));
        }
        result.setData(users);
        result.setTotalHits(resp.getHits().getTotalHits());
        result.setMaxRecords(maxRecords);
        result.setHits(users.size());
        result.setNumRecords(users.size());
        result.setTerm(new String(builder.buildAsBytes().toBytes()));
        result.setOffset(offset);
        result.setNextOffset(offset + maxRecords);
        result.setPrevOffset(Math.max(offset - maxRecords, 0));
        result.setDuration(System.currentTimeMillis() - time);
        return result;
    }

    @Override
    public UserRequest retrieveUserRequest(String token) throws IOException {
        final GetResponse resp;
        try {
            resp =
                    this.client.prepareGet(INDEX_USERS_REQUEST, INDEX_USERS_REQUEST_TYPE, token).execute()
                            .actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        if (!resp.isExists()) {
            throw new NotFoundException("User-Request with token " + token + " not found");
        }
        return mapper.readValue(resp.getSourceAsBytes(), UserRequest.class);
    }

    @Override
    public User createUser(final String token, final String password, final String passwordRepeat) throws IOException {
        if (password == null || password.isEmpty()) {
            throw new InvalidParameterException("Password can not be empty");
        }
        if (password.length() < 6) {
            throw new InvalidParameterException("Password must have six characters at least");
        }
        if (!password.equals(passwordRepeat)) {
            throw new InvalidParameterException("Passwords do not match");
        }
        final UserRequest req = this.retrieveUserRequest(token);
        if (req.getValidUntil().isBefore(ZonedDateTime.now())) {
            this.deleteUserRequest(token);
            throw new InvalidParameterException("The User request is not valid anymore");
        }
        req.getUser().setPwhash(DigestUtils.sha256Hex(password));
        final User u = this.createUser(req.getUser());
        this.deleteUserRequest(token);
        return u;
    }

    @Override
    public void deleteUserRequest(String token) throws IOException {
        try {
            this.client.prepareDelete(INDEX_USERS_REQUEST, INDEX_USERS_REQUEST_TYPE, token).execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        refreshIndex(INDEX_USERS_REQUEST);
    }

    @Override
    public User addDefaultRights(User user) {
        // Right to read and write self.
        UserAdminRole userAdminRole = (UserAdminRole) user.getRole(RoleName.ROLE_USER_ADMIN);
        if (userAdminRole == null) {
            userAdminRole = new UserAdminRole();
        }
        List<Right> rights = userAdminRole.getRights();
        if (rights == null) {
            rights = new ArrayList<Right>();
        }
        rights.add(new Right(user.getName(), new ArrayList<RoleRight>() {

            {
                add(RoleRight.READ);
                add(RoleRight.WRITE);
            }
        }));
        try {
            userAdminRole.setRights(rights);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        user.setRole(userAdminRole);
        return user;
    }

    @Override
    public boolean isExistingUser(String name) throws IOException {
        try {
            return this.client.prepareGet(INDEX_USERS, INDEX_USERS_TYPE, name).execute().actionGet().isExists();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public void deleteRights(String anchorId) throws IOException {
        List<User> users = retrieveUsersWithRight(anchorId);
        for (User user : users) {
            if (user.getRoles() != null) {
                List<Role> rolesToRemove = new ArrayList<Role>();
                List<Role> userRoles = user.getRoles();
                for (Role role : userRoles) {
                    if (role.getRights() != null && role.hasRight(anchorId)) {
                        role.removeRight(anchorId);
                        if (role.getRights().isEmpty()) {
                            rolesToRemove.add(role);
                        }
                    }
                }
                if (!rolesToRemove.isEmpty()) {
                    for (Role roleToRemove : rolesToRemove) {
                        userRoles.remove(roleToRemove);
                    }
                }
                setRoles(user.getName(), userRoles);
            }
        }
    }

    private List<User> retrieveUsersWithRight(String anchorId) throws IOException {
        final SearchResponse resp;
        QueryStringQueryBuilder builder = QueryBuilders.queryString("roles.rights.anchorId:" + anchorId);
        try {
            resp =
                    this.client.prepareSearch(INDEX_USERS).setQuery(builder).execute()
                            .actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        final List<User> users = new ArrayList<>(resp.getHits().getHits().length);
        for (SearchHit hit : resp.getHits()) {
            users.add(mapper.readValue(hit.getSourceAsString(), User.class));
        }
        return users;
    }

    private void validateRoles(List<Role> roles) throws IOException {
        List<RoleName> existingRoleNames = new ArrayList<RoleName>();
        for (Role role : roles) {
            if (role == null) {
                throw new IOException("role may not be null");
            }
            if (existingRoleNames.contains(role.getRoleName())) {
                throw new InvalidParameterException("duplicate role:" + role.getRoleName());
            }

            if (role.getRights() != null) {
                for (Right right : role.getRights()) {
                    List<RoleRight> existingRights = new ArrayList<RoleRight>();
                    if (right.getRoleRights() != null) {
                        for (RoleRight roleRight : right.getRoleRights()) {
                            if (existingRights.contains(roleRight)) {
                                throw new InvalidParameterException("duplicate role:" + role.getRoleName() +
                                        " anchorId:" + right.getAnchorId() + " right:" + roleRight);
                            }
                            if (role.allowedRights() != null && !role.allowedRights().isEmpty() &&
                                    !role.allowedRights().contains(roleRight)) {
                                throw new InvalidParameterException("not allowed role:" + role.getRoleName() +
                                        " anchorId:" + right.getAnchorId() + " right:" +
                                        roleRight);
                            }
                            existingRights.add(roleRight);
                        }
                    }
                }
                checkAnchorTypes(role);
            }
            existingRoleNames.add(role.getRoleName());
        }
    }

    /**
     * Checks if the given anchorIds all are of one of the types in anchorTypes. Throw IOException if one of the
     * anchorIds does not belong to one of the anchorTypes.
     * 
     * @param anchorIds
     * @param anchorTypes
     * @throws IOException
     */
    private void checkAnchorTypes(Role role) throws IOException {
        List<String> anchorIds = role.retrieveAnchorIds();
        List<PermissionAnchorType> anchorTypes = role.anchorTypes();
        List<String> usernames = new ArrayList<String>();
        List<String> contentModelIds = new ArrayList<String>();
        if (anchorTypes == null && anchorIds != null && !anchorIds.isEmpty()) {
            throw new InvalidParameterException("Not supported role:" + role.getRoleName() + " anchorId:all");
        }
        if (anchorTypes == null || anchorIds == null) {
            return;
        }
        if (anchorTypes.contains(PermissionAnchorType.USER)) {
            for (String anchorId : anchorIds) {
                try {
                    if (StringUtils.isNotBlank(anchorId)) {
                        retrieveUser(anchorId);
                    }
                    usernames.add(anchorId);
                } catch (IOException e) {
                }
            }
        }
        if (anchorTypes.contains(PermissionAnchorType.LEVEL1_ENTITY) ||
                anchorTypes.contains(PermissionAnchorType.LEVEL2_ENTITY)) {
            for (String anchorId : anchorIds) {
                if (!usernames.contains(anchorId) && StringUtils.isNotBlank(anchorId)) {
                    try {
                        Entity e = backendEntityService.retrieve(anchorId);
                        if (e.getContentModelId() == null) {
                            throw new InvalidParameterException("Wrong anchorType role:" + role.getRoleName() +
                                    " anchorId:" + anchorId);
                        }
                        if ((FixedContentModel.LEVEL1.getName().equals(e.getContentModelId()) && !anchorTypes
                                .contains(PermissionAnchorType.LEVEL1_ENTITY)) ||
                                (FixedContentModel.LEVEL2.getName().equals(e.getContentModelId()) && !anchorTypes
                                        .contains(PermissionAnchorType.LEVEL2_ENTITY)) ||
                                (!FixedContentModel.LEVEL1.getName().equals(e.getContentModelId()) && !FixedContentModel.LEVEL2
                                        .getName().equals(e.getContentModelId()))) {
                            throw new InvalidParameterException("Wrong anchorType role:" + role.getRoleName() +
                                    " anchorId:" + anchorId);
                        }
                        contentModelIds.add(e.getContentModelId());
                    } catch (IOException e) {
                    }
                }
            }
        }
        if (anchorIds.size() != (usernames.size() + contentModelIds.size())) {
            throw new IOException("At least one Object has wrong type for permission-anchor");
        }
    }

    /**
     * Holds enabled search-fields in users-index.
     * 
     * @author mih
     */
    public static enum UsersSearchField {
        NAME("name"),
        FIRSTNAME("firstName"),
        LASTNAME("lastName"),
        EMAIL("email"),
        ALL("_all");

        private final String searchFieldName;

        UsersSearchField(final String searchFieldName) {
            this.searchFieldName = searchFieldName;
        }

        public String getFieldName() {
            return searchFieldName;
        }
    }

}
