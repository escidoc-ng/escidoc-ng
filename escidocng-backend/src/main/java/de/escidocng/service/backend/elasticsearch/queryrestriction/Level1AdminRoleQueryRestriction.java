/**
 * 
 */

package de.escidocng.service.backend.elasticsearch.queryrestriction;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.escidocng.model.ContentModel.FixedContentModel;
import de.escidocng.model.security.role.Right;
import de.escidocng.model.security.role.Role;
import de.escidocng.model.security.role.Role.RoleRight;
import de.escidocng.service.backend.elasticsearch.ElasticSearchArchiveIndexService.ArchivesSearchField;
import de.escidocng.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

/**
 * @author mih
 */
public class Level1AdminRoleQueryRestriction extends RoleQueryRestriction {

    public Level1AdminRoleQueryRestriction(Role role) {
        super(role);
    }

    /**
     * Level1-Admin may see all level1 + level2-entities belonging to the level1 he has rights for.
     */
    @Override
    public String getEntitiesRestrictionQuery() {
        StringBuilder restrictionQueryBuilder = new StringBuilder("(");
        // restrict to nothing
        restrictionQueryBuilder.append(EntitiesSearchField.STATE.getFieldName()).append(":NONEXISTING");

        // add restrictions
        if (getRole() != null && getRole().getRights() != null) {
            for (Right right : getRole().getRights()) {
                List<RoleRight> userRights = right.getRoleRights();
                for (RoleRight userRight : userRights) {
                    if (RoleRight.READ.equals(userRight)) {
                        restrictionQueryBuilder.append(" OR ").append(
                                getLevel1AndLevel2EntitiesRestrictionQuery(right.getAnchorId()));
                    }
                }
            }
        }
        restrictionQueryBuilder.append(")");
        return restrictionQueryBuilder.toString();
    }

    /**
     * Level1-Admin may see all users
     */
    @Override
    public String getUsersRestrictionQuery() {
        return "(*:*)";
    }

    /**
     * Level1-Admin may see archives of all level1 + level2-entities belonging to the level1 he has rights for.
     */
    @Override
    public String getArchivesRestrictionQuery() {
        StringBuilder restrictionQueryBuilder = new StringBuilder("(");
        // restrict to nothing
        restrictionQueryBuilder.append(ArchivesSearchField.STATE.getFieldName()).append(":NONEXISTING");

        // add restrictions
        if (getRole() != null && getRole().getRights() != null) {
            for (Right right : getRole().getRights()) {
                List<RoleRight> userRights = right.getRoleRights();
                for (RoleRight userRight : userRights) {
                    if (RoleRight.READ.equals(userRight)) {
                        restrictionQueryBuilder.append(" OR ").append(
                                getLevel1AndLevel2ArchivesRestrictionQuery(right.getAnchorId()));
                    }
                }
            }
        }
        restrictionQueryBuilder.append(")");
        return restrictionQueryBuilder.toString();
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

    /**
     * Generate a subquery that restrict to level2s and level1s belonging to a distinct level1Id.
     * 
     * @param level1Id
     * @return BoolQueryBuilder subRestrictionQuery
     */
    private StringBuilder getLevel1AndLevel2ArchivesRestrictionQuery(String level1Id) {
        StringBuilder subRestrictionQueryBuilder = new StringBuilder("(");
        subRestrictionQueryBuilder.append("(");
        subRestrictionQueryBuilder.append(ArchivesSearchField.CONTENT_MODEL.getFieldName()).append(":").append(
                FixedContentModel.LEVEL1.getName());
        subRestrictionQueryBuilder.append(" OR ").append(ArchivesSearchField.CONTENT_MODEL.getFieldName())
                .append(":").append(FixedContentModel.LEVEL2.getName());
        subRestrictionQueryBuilder.append(")");
        if (StringUtils.isNotBlank(level1Id)) {
            subRestrictionQueryBuilder.append(" AND ").append(ArchivesSearchField.LEVEL1.getFieldName()).append(":").append(level1Id);
        }
        subRestrictionQueryBuilder.append(")");
        return subRestrictionQueryBuilder;
    }

}
