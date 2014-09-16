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

package net.objecthunter.larch.service.backend.elasticsearch;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.exceptions.AlreadyExistsException;
import net.objecthunter.larch.exceptions.InvalidParameterException;
import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.Entity.EntityType;
import net.objecthunter.larch.model.security.Right;
import net.objecthunter.larch.model.security.Right.ObjectType;
import net.objecthunter.larch.model.security.Right.PermissionType;
import net.objecthunter.larch.model.security.Rights;
import net.objecthunter.larch.model.security.Role;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.UserRequest;
import net.objecthunter.larch.service.MailService;
import net.objecthunter.larch.service.backend.BackendCredentialsService;
import net.objecthunter.larch.service.backend.BackendEntityService;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementation of a spring-security {@link org.springframework.security.authentication.AuthenticationManager} which
 * uses ElasticSearch indices as a persistence layer
 */
public class ElasticSearchCredentialsService extends AbstractElasticSearchService
        implements AuthenticationManager, BackendCredentialsService {

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
                admin.addRole(Role.ADMIN, new Rights());
                client
                        .prepareIndex(INDEX_USERS, "user", admin.getName())
                        .setSource(mapper.writeValueAsBytes(admin))
                        .execute().actionGet();

                final User user = new User();
                user.setPwhash(DigestUtils.sha256Hex("user"));
                user.setName("user");
                user.setFirstName("Generic");
                user.setLastName("User");
                Rights rights = new Rights();
                Set<Right> rightSet = new HashSet<Right>();
                Right right = new Right(ObjectType.ENTITY, PermissionType.READ, EntityState.PENDING, false);
                rightSet.add(right);
                Right right1 = new Right(ObjectType.BINARY, PermissionType.WRITE, EntityState.SUBMITTED, true);
                rightSet.add(right1);
                rights.setRights("test", rightSet);
                user.addRole(Role.USER, rights);
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
                final User u = mapper.readValue(get.getSourceAsBytes(), User.class);
                if (u.getPwhash().equals(hash)) {
                    String[] roles = null;
                    if (u.getRoles() != null && u.getRoles().size() > 0) {
                        roles = u.getRoles().keySet().toArray(new String[u.getRoles().size()]);
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
            
            //Roles cannot be set with update user
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
    public void setRoles(String username, Map<Role, Rights> roles) throws IOException {
        //check parameters
        if (StringUtils.isBlank(username)) {
            throw new InvalidParameterException("User name can not be null");
        }
        for (Role role : roles.keySet()) {
            if (role == null) {
                throw new InvalidParameterException("role may not be null");
            }
            if (roles.get(role) != null && roles.get(role).getRights() != null && !roles.get(role).getRights().isEmpty()) {
                Map<String, Set<Right>> rights = roles.get(role).getRights();
                for(Entry<String, Set<Right>> right : rights.entrySet()) {
                    if (right.getKey() == null) {
                        throw new InvalidParameterException("id of right may not be null");
                    }
                    Entity entity = backendEntityService.retrieve(right.getKey());
                    if (!EntityType.AREA.equals(entity.getType()) && !EntityType.PERMISSION.equals(entity.getType())) {
                        throw new InvalidParameterException("object for objectId must be of type AREA or PERMISSION");
                    }
                    for (Right singleRight : right.getValue()) {
                        if (singleRight.getObjectType() == null) {
                            throw new InvalidParameterException("objectType of right may not be null");
                        }
                        if (singleRight.getPermissionType() == null) {
                            throw new InvalidParameterException("permissionType of right may not be null");
                        }
                    }
                }
            }
        }
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
    public void setRight(String username, Role role, String objectId, Right right) throws IOException {
        //check parameters
        if (StringUtils.isBlank(username)) {
            throw new InvalidParameterException("User name can not be null");
        }
        if (role == null) {
            throw new InvalidParameterException("name of role may not be null");
        }
        if (Role.ADMIN.equals(role)) {
            throw new InvalidParameterException("admin-role cannot be set with this method");
        }
        if (objectId == null) {
            throw new InvalidParameterException("objectId may not be null");
        }
        if (right == null) {
            throw new InvalidParameterException("right may not be null");
        }
        if (right.getObjectType() == null) {
            throw new InvalidParameterException("objectType of right may not be null");
        }
        if (right.getPermissionType() == null) {
            throw new InvalidParameterException("permissionType of right may not be null");
        }
        Entity entity = backendEntityService.retrieve(objectId);
        if (!EntityType.AREA.equals(entity.getType()) && !EntityType.PERMISSION.equals(entity.getType())) {
            throw new InvalidParameterException("object for objectId must be of type AREA or PERMISSION");
        }

        try {
            final GetResponse get =
                    this.client.prepareGet(INDEX_USERS, INDEX_USERS_TYPE, username).execute().actionGet();
            if (!get.isExists()) {
                throw new NotFoundException("The user " + username + " does not exist");
            }
            
            User user = mapper.readValue(get.getSourceAsBytes(), User.class);
            if (user.getRoles().get(role) == null) {
                Rights rights = new Rights();
                rights.addRights(objectId, right);
                user.addRole(role, rights);
            } else {
                user.getRoles().get(role).addRights(objectId, right);
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

    private boolean isLastAdminUser(String name) throws IOException {
        User user = this.retrieveUser(name);
        if (user.getRoles() == null || !user.getRoles().containsKey(Role.ADMIN.getName())) {
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
                                                    Role.ADMIN.getName()))).execute()
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
    public List<User> retrieveUsers() throws IOException {
        final SearchResponse resp;
        try {
            resp =
                    this.client.prepareSearch(INDEX_USERS).setQuery(QueryBuilders.matchAllQuery()).execute()
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
    public boolean isExistingUser(String name) throws IOException {
        try {
            return this.client.prepareGet(INDEX_USERS, INDEX_USERS_TYPE, name).execute().actionGet().isExists();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
    }

}
