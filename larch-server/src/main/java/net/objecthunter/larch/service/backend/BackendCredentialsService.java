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

import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.UserRequest;
import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.model.security.role.Role.RoleName;
import net.objecthunter.larch.model.security.role.Role.RoleRight;

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
    void setRoles(String username, List<Role> roles) throws IOException;

    /**
     * Set a right for a {@link net.objecthunter.larch.model.security.User} and an objectId in the Repository
     * 
     * @param username the name of the user
     * @param roleName the rolename
     * @param objectId the objectId to set the right for
     * @param rights the rights to set
     * @throws IOException
     */
    void setRight(String username, RoleName roleName, String objectId, List<RoleRight> rights) throws IOException;

    /**
     * Delete a User from the repository
     * 
     * @param name The name of the user to delete
     * @throws IOException
     */
    void deleteUser(String name) throws IOException;

    /**
     * Retrieve a {@link net.objecthunter.larch.model.security.User} object from the repository
     * 
     * @param name the name of the user to retrieve
     * @return The {@link net.objecthunter.larch.model.security.User} object of the corresponding user
     * @throws IOException
     */
    User retrieveUser(String name) throws IOException;

    /**
     * Retrieve a list of {@link net.objecthunter.larch.model.security.User}s existing in the repository
     * 
     * @return a list of {@link net.objecthunter.larch.model.security.User} objects
     * @throws IOException
     */
    List<User> retrieveUsers() throws IOException;

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

}
