/**
 * 
 */
package net.objecthunter.larch.service.backend.elasticsearch.queryrestriction;

import net.objecthunter.larch.model.security.role.Role;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;


/**
 * @author mih
 *
 */
public class AdminRoleQueryRestriction extends RoleQueryRestriction {
    
    public AdminRoleQueryRestriction(Role role) {
        super(role);
    }

    @Override
    public QueryBuilder getEntitiesRestrictionQuery() {
        return QueryBuilders.matchAllQuery();
    }

    @Override
    public QueryBuilder getUsersRestrictionQuery() {
        return QueryBuilders.matchAllQuery();
    }

}
