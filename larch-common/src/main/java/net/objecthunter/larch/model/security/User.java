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

package net.objecthunter.larch.model.security;

import java.util.HashMap;
import java.util.Map;

/**
 * A DTO for a larch User
 */
public class User {

    private String name;

    private String firstName;

    private String lastName;

    private String email;

    private String pwhash;

    private Map<Role, Rights> roles = new HashMap<Role, Rights>();

    /**
     * Get the user's name
     * 
     * @return the user's name
     */
    public String getName() {
        return name;
    }

    /**
     * set the user's name
     * 
     * @param name the user's name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get the user's first name
     * 
     * @return the user's first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Set the user's first name
     * 
     * @param firstName the first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Get the user's last name
     * 
     * @return the user's last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Set the user's last name
     * 
     * @param lastName the last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Get the user's email address
     * 
     * @return the email dress
     */
    public String getEmail() {
        return email;
    }

    /**
     * set the user's email address
     * 
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Get the user's password hash
     * 
     * @return the user's password hash
     */
    public String getPwhash() {
        return pwhash;
    }

    /**
     * set the user's password hash
     * 
     * @param pwhash the hash to set
     */
    public void setPwhash(String pwhash) {
        this.pwhash = pwhash;
    }

    /**
     * get the roles of the user
     * 
     * @return the roles
     */
    public Map<Role, Rights> getRoles() {
        return roles;
    }

    /**
     * Set the roles of the user
     * 
     * @param roles the roles to set
     */
    public void setRoles(Map<Role, Rights> roles) {
        if (roles == null) {
            this.roles = new HashMap<Role, Rights>();
        }
        this.roles = roles;
    }

    /**
     * add a role
     * 
     * @param group key of role
     * @param rights value of role
     */
    public void addRole(Role role, Rights rights) {
        if (roles == null) {
            this.roles = new HashMap<Role, Rights>();
        }
        roles.put(role, rights);
    }

    /**
     * add a role
     * 
     * @param group key of role
     * @param rights value of role
     */
    public void removeRole(Role role) {
        if (roles == null) {
            this.roles = new HashMap<Role, Rights>();
        }
        roles.remove(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (email != null ? !email.equals(user.email) : user.email != null) return false;
        if (firstName != null ? !firstName.equals(user.firstName) : user.firstName != null) return false;
        if (roles != null ? !roles.equals(user.roles) : user.roles != null) return false;
        if (lastName != null ? !lastName.equals(user.lastName) : user.lastName != null) return false;
        if (!name.equals(user.name)) return false;
        if (pwhash != null ? !pwhash.equals(user.pwhash) : user.pwhash != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (pwhash != null ? pwhash.hashCode() : 0);
        result = 31 * result + (roles != null ? roles.hashCode() : 0);
        return result;
    }
}
