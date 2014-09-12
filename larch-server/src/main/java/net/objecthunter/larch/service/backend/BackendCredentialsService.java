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

package net.objecthunter.larch.service.backend;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.objecthunter.larch.model.security.Group;
import net.objecthunter.larch.model.security.Rights;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.UserRequest;

/**
 * Service definition for the AuthZ/AuthN service
 */
public interface BackendCredentialsService {

    /**
     * Create a new User in the repository
     * 
     * @param u The user to create
     * @throws IOException
     */
    User createUser(User u) throws IOException;

    /**
     * Create a new request to add a user. the user's email address should be employed to send a confirmation link to
     * 
     * @param u the user for which a new confirmation request should be created
     * @throws IOException
     */
    net.objecthunter.larch.model.security.UserRequest createNewUserRequest(User u) throws IOException;

    /**
     * Create a new Group in the repository
     * 
     * @param g the group to create
     * @throws IOException
     */
    void createGroup(Group g) throws IOException;

    /**
     * Update a {@link net.objecthunter.larch.model.security.User} in the Repository
     * 
     * @param u the user to update
     * @throws IOException
     */
    void updateUser(User u) throws IOException;

    /**
     * Set roles for a {@link net.objecthunter.larch.model.security.User} in the Repository
     * 
     * @param username the name of the user
     * @param roles the roles to set
     * @throws IOException
     */
    void setRoles(String username, Map<String, Rights> roles) throws IOException;

    /**
     * Update a {@link net.objecthunter.larch.model.security.Group} in the repository
     * 
     * @param g The group to update
     * @throws IOException
     */
    void updateGroup(Group g) throws IOException;

    /**
     * Delete a User from the repository
     * 
     * @param name The name of the user to delete
     * @throws IOException
     */
    void deleteUser(String name) throws IOException;

    /**
     * Delete a {@link net.objecthunter.larch.model.security.Group} from the repository
     * 
     * @param name The name of the {@link net.objecthunter.larch.model.security.Group} to delete
     * @throws IOException
     */
    void deleteGroup(String name) throws IOException;

    /**
     * Retrieve a {@link net.objecthunter.larch.model.security.User} object from the repository
     * 
     * @param name the name of the user to retrieve
     * @return The {@link net.objecthunter.larch.model.security.User} object of the corresponding user
     * @throws IOException
     */
    User retrieveUser(String name) throws IOException;

    /**
     * Retrieve a Group from the repository
     * 
     * @param name the name of the{@link net.objecthunter.larch.model.security.Group} to retrieve
     * @return The {@link net.objecthunter.larch.model.security.Group} object with the corresponding name
     * @throws IOException
     */
    Group retrieveGroup(String name) throws IOException;

    /**
     * Retrieve a list of {@link net.objecthunter.larch.model.security.User}s existing in the repository
     * 
     * @return a list of {@link net.objecthunter.larch.model.security.User} objects
     * @throws IOException
     */
    List<User> retrieveUsers() throws IOException;

    /**
     * retrieve a list of {@link net.objecthunter.larch.model.security.Group}s existing in the repository
     * 
     * @return The list of {@link net.objecthunter.larch.model.security.Group} that are available
     * @throws IOException
     */
    List<Group> retrieveGroups() throws IOException;

    /**
     * Retrieve an existing {@link net.objecthunter.larch.model.security.UserRequest}
     * 
     * @param token the token of the user request
     * @return the existing {@link net.objecthunter.larch.model.security.UserRequest}
     */
    UserRequest retrieveUserRequest(String token) throws IOException;

    /**
     * Create a new {@link net.objecthunter.larch.model.security.User} from an existing
     * {@link net.objecthunter.larch .model.security.UserRequest} with a given token value
     * 
     * @param token the token value
     * @param password the password to use
     * @param passwordRepeat the password repetition to check
     */
    User createUser(String token, String password, String passwordRepeat) throws IOException;

    /**
     * Delete a {@link net.objecthunter.larch.model.security.UserRequest} record
     * 
     * @param token the token value of the {@link net.objecthunter.larch.model.security.UserRequest}
     */
    void deleteUserRequest(String token) throws IOException;

    /**
     * Check if a user name is already existing in the index
     * 
     * @param name the name to check
     * @return true if the user does exist, otherwise false
     */
    boolean isExistingUser(String name) throws IOException;

    /**
     * Retrieve a list of groups from the repo
     * 
     * @param groupNames the names of the groups to retrieve
     * @return a List containing the corresponding {@link net.objecthunter.larch.model.security.Group} instances
     */
    List<Group> retrieveGroups(List<String> groupNames) throws IOException;
}
