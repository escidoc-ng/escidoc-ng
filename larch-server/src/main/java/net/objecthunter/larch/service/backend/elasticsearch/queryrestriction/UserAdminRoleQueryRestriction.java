/**
 * 
 */
package net.objecthunter.larch.service.backend.elasticsearch.queryrestriction;

import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;


/**
 * @author mih
 *
 */
public class UserAdminRoleQueryRestriction extends RoleQueryRestriction {

    public UserAdminRoleQueryRestriction(Role role) {
        super(role);
    }

    @Override
    public QueryBuilder getEntitiesRestrictionQuery() {
        BoolQueryBuilder restrictionQueryBuilder = QueryBuilders.boolQuery();
        //restrict to nothing
        restrictionQueryBuilder.should(QueryBuilders.termQuery(EntitiesSearchField.STATE.getFieldName(), "NONEXISTING"));
        return restrictionQueryBuilder;
    }

}
