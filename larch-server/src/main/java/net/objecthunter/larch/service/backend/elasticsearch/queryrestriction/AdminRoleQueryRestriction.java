/**
 * 
 */
package net.objecthunter.larch.service.backend.elasticsearch.queryrestriction;

import net.objecthunter.larch.model.security.role.Role;


/**
 * @author mih
 *
 */
public class AdminRoleQueryRestriction extends RoleQueryRestriction {
    
    public AdminRoleQueryRestriction(Role role) {
        super(role);
    }

    /**
     * Admin may see all entities
     */
    @Override
    public String getEntitiesRestrictionQuery() {
        return "(*:*)";
    }

    /**
     * Admin may see all users
     */
    @Override
    public String getUsersRestrictionQuery() {
        return "(*:*)";
    }

    /**
     * Admin may see all archives
     */
    @Override
    public String getArchivesRestrictionQuery() {
        return "(*:*)";
    }

}
