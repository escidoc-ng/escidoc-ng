/**
 * 
 */
package net.objecthunter.larch.service.backend.elasticsearch.queryrestriction;

import java.util.List;
import java.util.Map.Entry;

import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.Entity.EntityType;
import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.model.security.role.Role.RoleRight;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;


/**
 * @author mih
 *
 */
public class AreaAdminRoleQueryRestriction extends RoleQueryRestriction {

    public AreaAdminRoleQueryRestriction(Role role) {
        super(role);
    }

    @Override
    public QueryBuilder getEntitiesRestrictionQuery() {
        BoolQueryBuilder restrictionQueryBuilder = QueryBuilders.boolQuery();
        //restrict to nothing
        restrictionQueryBuilder.should(QueryBuilders.termQuery(EntitiesSearchField.STATE.getFieldName(), "NONEXISTING"));

        // add restrictions
        if (getRole() != null && getRole().getRights() != null) {
            for (Entry<String, List<RoleRight>> rightSet : getRole().getRights().entrySet()) {
                List<RoleRight> userRights = rightSet.getValue();
                for (RoleRight userRight : userRights) {
                    if (RoleRight.READ.equals(userRight)) {
                        restrictionQueryBuilder.should(getAreaAndPermissionEntitiesRestrictionQuery(rightSet.getKey()));
                    }
                }
            }
        }
        return restrictionQueryBuilder;
    }

    @Override
    public QueryBuilder getUsersRestrictionQuery() {
        return QueryBuilders.matchAllQuery();
    }

    /**
     * Generate a subquery that restrict to a certain permission and entities with certain state
     * 
     * @param state
     * @param areaId
     * @return BoolQueryBuilder subRestrictionQuery
     */
    private BoolQueryBuilder getAreaAndPermissionEntitiesRestrictionQuery(String areaId) {
        BoolQueryBuilder subRestrictionQueryBuilder = QueryBuilders.boolQuery();
        BoolQueryBuilder subSubRestrictionQueryBuilder = QueryBuilders.boolQuery();
        subSubRestrictionQueryBuilder.should(QueryBuilders.termQuery(EntitiesSearchField.TYPE.getFieldName(),
                EntityType.AREA.getName()));
        subSubRestrictionQueryBuilder.should(QueryBuilders.termQuery(EntitiesSearchField.TYPE.getFieldName(),
                EntityType.PERMISSION.getName()));
        subRestrictionQueryBuilder.must(subSubRestrictionQueryBuilder);
        if (StringUtils.isNotBlank(areaId)) {
            subRestrictionQueryBuilder.must(QueryBuilders.termQuery(EntitiesSearchField.AREA_ID.getFieldName(),
                    areaId));
        }
        return subRestrictionQueryBuilder;
    }

}
