/**
 * 
 */
package net.objecthunter.larch.service.backend.elasticsearch.queryrestriction;

import net.objecthunter.larch.model.security.role.Role;

import org.elasticsearch.index.query.QueryBuilder;


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
     * @return QueryBuilder QueryBuilder with restriction-Query
     */
    public abstract QueryBuilder getEntitiesRestrictionQuery();

    /**
     * Generate a Restriction-Query for a search for users
     * 
     * @return QueryBuilder QueryBuilder with restriction-Query
     */
    public abstract QueryBuilder getUsersRestrictionQuery();

}
