/**
 * 
 */
package de.escidocng.service.backend.elasticsearch.queryrestriction;

import de.escidocng.model.security.role.Role;


/**
 * Abstract Class that defines Methods extending classes have to implement.
 * Responsible to deliver Restriction-Queries for Searches, dependent on Roles the searching user has.
 * 
 * @author mih
 *
 */
public abstract class RoleQueryRestriction {
    
    private Role role;
    
    public RoleQueryRestriction(Role role) {
        this.role = role;
    }
    
    protected Role getRole() {
        return this.role;
    }
    
    /**
     * Generate a Restriction-Query for a search for entities
     * 
     * @return String with restriction-Query
     */
    public abstract String getEntitiesRestrictionQuery();

    /**
     * Generate a Restriction-Query for a search for users
     * 
     * @return String with restriction-Query
     */
    public abstract String getUsersRestrictionQuery();

    /**
     * Generate a Restriction-Query for a search for archives
     * 
     * @return String with restriction-Query
     */
    public abstract String getArchivesRestrictionQuery();

}
