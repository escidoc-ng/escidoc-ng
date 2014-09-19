/**
 * 
 */
package net.objecthunter.larch.service.backend.elasticsearch.queryrestriction;

import net.objecthunter.larch.model.security.role.Role;

import org.elasticsearch.index.query.QueryBuilder;


/**
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
    
    public abstract QueryBuilder getEntitiesRestrictionQuery();
}
