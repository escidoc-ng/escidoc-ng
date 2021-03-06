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

package de.escidocng.service.backend;

import java.io.IOException;
import java.util.List;

import de.escidocng.model.SearchResult;
import de.escidocng.model.security.User;
import de.escidocng.model.security.UserRequest;
import de.escidocng.model.security.role.Role;
import de.escidocng.model.security.role.Role.RoleName;
import de.escidocng.model.security.role.Role.RoleRight;

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
    UserRequest createNewUserRequest(User u) throws IOException;

    /**
     * Update a {@link de.escidocng.model.security.User} in the Repository
     * 
     * @param u the user to update
     * @throws IOException
     */
    void updateUser(User u) throws IOException;

    /**
     * Set roles for a {@link de.escidocng.model.security.User} in the Repository
     * 
     * @param username the name of the user
     * @param roles the roles to set
     * @throws IOException
     */
    void setRoles(String username, List<Role> roles) throws IOException;

    /**
     * Set a right for a {@link de.escidocng.model.security.User} and an objectId in the Repository
     * 
     * @param username the name of the user
     * @param roleName the rolename
     * @param anchorId the anchorId to set the right for
     * @param rights the rights to set
     * @throws IOException
     */
    void setRight(String username, RoleName roleName, String anchorId, List<RoleRight> rights) throws IOException;

    /**
     * Delete a User from the repository
     * 
     * @param name The name of the user to delete
     * @throws IOException
     */
    void deleteUser(String name) throws IOException;

    /**
     * Retrieve a {@link de.escidocng.model.security.User} object from the repository
     * 
     * @param name the name of the user to retrieve
     * @return The {@link de.escidocng.model.security.User} object of the corresponding user
     * @throws IOException
     */
    User retrieveUser(String name) throws IOException;

    /**
     * Retrieve a SearchResult containing a list of {@link de.escidocng.model.security.User}s existing in the repository
     * 
     * @return a SearchResult containing list of {@link de.escidocng.model.security.User} objects
     * @throws IOException
     */
    SearchResult searchUsers(String query, int offset, int maxRecords) throws IOException;

    /**
     * Retrieve an existing {@link de.escidocng.model.security.UserRequest}
     * 
     * @param token the token of the user request
     * @return the existing {@link de.escidocng.model.security.UserRequest}
     */
    UserRequest retrieveUserRequest(String token) throws IOException;

    /**
     * Create a new {@link de.escidocng.model.security.User} from an existing
     * {@link de.escidocng .model.security.UserRequest} with a given token value
     * 
     * @param token the token value
     * @param password the password to use
     * @param passwordRepeat the password repetition to check
     */
    User createUser(String token, String password, String passwordRepeat) throws IOException;

    /**
     * Delete a {@link de.escidocng.model.security.UserRequest} record
     * 
     * @param token the token value of the {@link de.escidocng.model.security.UserRequest}
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
     * Add default rights to the users roles
     * 
     * @param user the user
     * @return User the user with added default roles
     */
    User addDefaultRights(User user);

    /**
     * Delete all rights for given anchorId
     * 
     * @throws IOException
     */
    void deleteRights(String anchorId) throws IOException;

}
