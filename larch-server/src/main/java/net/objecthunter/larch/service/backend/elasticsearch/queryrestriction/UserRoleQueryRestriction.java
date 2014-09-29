/**
 * 
 */
package net.objecthunter.larch.service.backend.elasticsearch.queryrestriction;

import java.util.List;
import java.util.Map.Entry;

import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity.EntityState;
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
                    } else if (RoleRight.READ_LEVEL2.equals(userRight)) {
                        restrictionQueryBuilder.should(getLevel2EntitiesRestrictionQuery(rightSet.getKey()));
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
     * Generate a subquery that restrict to a certain level2 and entities with certain state
     * 
     * @param state
     * @param level2Id
     * @return BoolQueryBuilder subRestrictionQuery
     */
    private BoolQueryBuilder getDataEntitiesRestrictionQuery(EntityState state, String level2Id) {
        BoolQueryBuilder subRestrictionQueryBuilder = QueryBuilders.boolQuery();
        subRestrictionQueryBuilder.must(QueryBuilders.termQuery(EntitiesSearchField.STATE.getFieldName(), state
                .name()));
        subRestrictionQueryBuilder.mustNot(QueryBuilders.termQuery(EntitiesSearchField.CONTENT_MODEL.getFieldName(),
                FixedContentModel.LEVEL1.getName()));
        subRestrictionQueryBuilder.mustNot(QueryBuilders.termQuery(EntitiesSearchField.CONTENT_MODEL.getFieldName(),
                FixedContentModel.LEVEL2.getName()));
        if (StringUtils.isNotBlank(level2Id)) {
            subRestrictionQueryBuilder.must(QueryBuilders.termQuery(EntitiesSearchField.LEVEL2.getFieldName(),
                    level2Id));
        }
        return subRestrictionQueryBuilder;
    }

    /**
     * Generate a subquery that restrict to a certain level2
     * 
     * @param level2Id
     * @return BoolQueryBuilder subRestrictionQuery
     */
    private BoolQueryBuilder getLevel2EntitiesRestrictionQuery(String level2Id) {
        BoolQueryBuilder subRestrictionQueryBuilder = QueryBuilders.boolQuery();
        subRestrictionQueryBuilder.must(QueryBuilders.termQuery(EntitiesSearchField.CONTENT_MODEL.getFieldName(), FixedContentModel.LEVEL2.getName()));
        if (StringUtils.isNotBlank(level2Id)) {
            subRestrictionQueryBuilder.must(QueryBuilders.termQuery(EntitiesSearchField.LEVEL2.getFieldName(),
                    level2Id));
        }
        return subRestrictionQueryBuilder;
    }

}
