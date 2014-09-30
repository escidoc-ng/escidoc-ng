/**
 * 
 */

package net.objecthunter.larch.service.backend.elasticsearch.queryrestriction;

import java.util.List;
import java.util.Map.Entry;

import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.model.security.role.Role.RoleRight;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

import org.apache.commons.lang3.StringUtils;

/**
 * @author mih
 */
public class Level1AdminRoleQueryRestriction extends RoleQueryRestriction {

    public Level1AdminRoleQueryRestriction(Role role) {
        super(role);
    }

    @Override
    public String getEntitiesRestrictionQuery() {
        StringBuilder restrictionQueryBuilder = new StringBuilder("(");
        // restrict to nothing
        restrictionQueryBuilder.append(EntitiesSearchField.STATE.getFieldName()).append(":NONEXISTING");

        // add restrictions
        if (getRole() != null && getRole().getRights() != null) {
            for (Entry<String, List<RoleRight>> rightSet : getRole().getRights().entrySet()) {
                List<RoleRight> userRights = rightSet.getValue();
                for (RoleRight userRight : userRights) {
                    if (RoleRight.READ.equals(userRight)) {
                        restrictionQueryBuilder.append(" OR ").append(
                                getLevel1AndLevel2EntitiesRestrictionQuery(rightSet.getKey()));
                    }
                }
            }
        }
        restrictionQueryBuilder.append(")");
        return restrictionQueryBuilder.toString();
    }

    @Override
    public String getUsersRestrictionQuery() {
        return "(*:*)";
    }

    /**
     * Generate a subquery that restrict to level2s and level1s belonging to a distinct level1Id.
     * 
     * @param level1Id
     * @return BoolQueryBuilder subRestrictionQuery
     */
    private StringBuilder getLevel1AndLevel2EntitiesRestrictionQuery(String level1Id) {
        StringBuilder subRestrictionQueryBuilder = new StringBuilder("(");
        subRestrictionQueryBuilder.append("(");
        subRestrictionQueryBuilder.append(EntitiesSearchField.CONTENT_MODEL.getFieldName()).append(":").append(
                FixedContentModel.LEVEL1.getName());
        subRestrictionQueryBuilder.append(" OR ").append(EntitiesSearchField.CONTENT_MODEL.getFieldName())
                .append(":").append(FixedContentModel.LEVEL2.getName());
        subRestrictionQueryBuilder.append(")");
        if (StringUtils.isNotBlank(level1Id)) {
            subRestrictionQueryBuilder.append(" AND ").append(EntitiesSearchField.LEVEL1.getFieldName()).append(":").append(level1Id);
        }
        subRestrictionQueryBuilder.append(")");
        return subRestrictionQueryBuilder;
    }

}
