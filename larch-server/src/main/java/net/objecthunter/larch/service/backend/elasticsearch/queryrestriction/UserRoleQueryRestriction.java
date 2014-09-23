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
public class UserRoleQueryRestriction extends RoleQueryRestriction {

    public UserRoleQueryRestriction(Role role) {
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
                    if (RoleRight.READ_PENDING_METADATA.equals(userRight)) {
                        restrictionQueryBuilder.should(getDataEntitiesRestrictionQuery(EntityState.PENDING,
                                rightSet.getKey()));
                    } else if (RoleRight.READ_PUBLISHED_METADATA.equals(userRight)) {
                        restrictionQueryBuilder.should(getDataEntitiesRestrictionQuery(EntityState.PUBLISHED,
                                rightSet.getKey()));
                    } else if (RoleRight.READ_SUBMITTED_METADATA.equals(userRight)) {
                        restrictionQueryBuilder.should(getDataEntitiesRestrictionQuery(EntityState.SUBMITTED,
                                rightSet.getKey()));
                    } else if (RoleRight.READ_WITHDRAWN_METADATA.equals(userRight)) {
                        restrictionQueryBuilder.should(getDataEntitiesRestrictionQuery(EntityState.WITHDRAWN,
                                rightSet.getKey()));
                    } else if (RoleRight.READ_PERMISSION.equals(userRight)) {
                        restrictionQueryBuilder.should(getPermissionEntitiesRestrictionQuery(rightSet.getKey()));
                    }
                }
            }
        }

        return restrictionQueryBuilder;
    }

    @Override
    public QueryBuilder getUsersRestrictionQuery() {
        BoolQueryBuilder restrictionQueryBuilder = QueryBuilders.boolQuery();
        //restrict to nothing
        restrictionQueryBuilder.should(QueryBuilders.termQuery("name", "NONEXISTING"));
        return restrictionQueryBuilder;
    }

    /**
     * Generate a subquery that restrict to a certain permission and entities with certain state
     * 
     * @param state
     * @param permissionId
     * @return BoolQueryBuilder subRestrictionQuery
     */
    private BoolQueryBuilder getDataEntitiesRestrictionQuery(EntityState state, String permissionId) {
        BoolQueryBuilder subRestrictionQueryBuilder = QueryBuilders.boolQuery();
        subRestrictionQueryBuilder.must(QueryBuilders.termQuery(EntitiesSearchField.STATE.getFieldName(), state
                .name()));
        subRestrictionQueryBuilder.mustNot(QueryBuilders.termQuery(EntitiesSearchField.TYPE.getFieldName(),
                EntityType.AREA.getName()));
        subRestrictionQueryBuilder.mustNot(QueryBuilders.termQuery(EntitiesSearchField.TYPE.getFieldName(),
                EntityType.PERMISSION.getName()));
        if (StringUtils.isNotBlank(permissionId)) {
            subRestrictionQueryBuilder.must(QueryBuilders.termQuery(EntitiesSearchField.PERMISSION_ID.getFieldName(),
                    permissionId));
        }
        return subRestrictionQueryBuilder;
    }

    /**
     * Generate a subquery that restrict to a certain permission
     * 
     * @param permissionId
     * @return BoolQueryBuilder subRestrictionQuery
     */
    private BoolQueryBuilder getPermissionEntitiesRestrictionQuery(String permissionId) {
        BoolQueryBuilder subRestrictionQueryBuilder = QueryBuilders.boolQuery();
        subRestrictionQueryBuilder.must(QueryBuilders.termQuery(EntitiesSearchField.TYPE.getFieldName(), EntityType.PERMISSION.name()));
        if (StringUtils.isNotBlank(permissionId)) {
            subRestrictionQueryBuilder.must(QueryBuilders.termQuery(EntitiesSearchField.PERMISSION_ID.getFieldName(),
                    permissionId));
        }
        return subRestrictionQueryBuilder;
    }

}
