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
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchArchiveIndexService.ArchivesSearchField;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

import org.apache.commons.lang3.StringUtils;

/**
 * @author mih
 */
public class UserRoleQueryRestriction extends RoleQueryRestriction {

    public UserRoleQueryRestriction(Role role) {
        super(role);
    }

    /**
     * User may see entities below level2 he has rights for + level2-entities he has rights for in correct state.
     */
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
                    if (RoleRight.READ_PENDING_METADATA.equals(userRight)) {
                        restrictionQueryBuilder.append(" OR ").append(
                                getDataEntitiesRestrictionQuery(EntityState.PENDING,
                                        rightSet.getKey()));
                    } else if (RoleRight.READ_PUBLISHED_METADATA.equals(userRight)) {
                        restrictionQueryBuilder.append(" OR ").append(
                                getDataEntitiesRestrictionQuery(EntityState.PUBLISHED,
                                        rightSet.getKey()));
                    } else if (RoleRight.READ_SUBMITTED_METADATA.equals(userRight)) {
                        restrictionQueryBuilder.append(" OR ").append(
                                getDataEntitiesRestrictionQuery(EntityState.SUBMITTED,
                                        rightSet.getKey()));
                    } else if (RoleRight.READ_WITHDRAWN_METADATA.equals(userRight)) {
                        restrictionQueryBuilder.append(" OR ").append(
                                getDataEntitiesRestrictionQuery(EntityState.WITHDRAWN,
                                        rightSet.getKey()));
                    } else if (RoleRight.READ_LEVEL2.equals(userRight)) {
                        restrictionQueryBuilder.append(" OR ").append(
                                getLevel2EntitiesRestrictionQuery(rightSet.getKey()));
                    }
                }
            }
        }
        restrictionQueryBuilder.append(")");
        return restrictionQueryBuilder.toString();
    }

    /**
     * User may not see users.
     */
    @Override
    public String getUsersRestrictionQuery() {
        StringBuilder restrictionQueryBuilder = new StringBuilder("(");
        // restrict to nothing
        restrictionQueryBuilder.append("name:NONEXISTING");
        restrictionQueryBuilder.append(")");
        return restrictionQueryBuilder.toString();
    }

    /**
     * User may see archives below level2 he has rights for + level2-archives he has rights for in correct state.
     */
    @Override
    public String getArchivesRestrictionQuery() {
        StringBuilder restrictionQueryBuilder = new StringBuilder("(");
        // restrict to nothing
        restrictionQueryBuilder.append(ArchivesSearchField.STATE.getFieldName()).append(":NONEXISTING");

        // add restrictions
        if (getRole() != null && getRole().getRights() != null) {
            for (Entry<String, List<RoleRight>> rightSet : getRole().getRights().entrySet()) {
                List<RoleRight> userRights = rightSet.getValue();
                for (RoleRight userRight : userRights) {
                    if (RoleRight.READ_PENDING_METADATA.equals(userRight)) {
                        restrictionQueryBuilder.append(" OR ").append(
                                getDataArchivesRestrictionQuery(EntityState.PENDING,
                                        rightSet.getKey()));
                    } else if (RoleRight.READ_PUBLISHED_METADATA.equals(userRight)) {
                        restrictionQueryBuilder.append(" OR ").append(
                                getDataArchivesRestrictionQuery(EntityState.PUBLISHED,
                                        rightSet.getKey()));
                    } else if (RoleRight.READ_SUBMITTED_METADATA.equals(userRight)) {
                        restrictionQueryBuilder.append(" OR ").append(
                                getDataArchivesRestrictionQuery(EntityState.SUBMITTED,
                                        rightSet.getKey()));
                    } else if (RoleRight.READ_WITHDRAWN_METADATA.equals(userRight)) {
                        restrictionQueryBuilder.append(" OR ").append(
                                getDataArchivesRestrictionQuery(EntityState.WITHDRAWN,
                                        rightSet.getKey()));
                    } else if (RoleRight.READ_LEVEL2.equals(userRight)) {
                        restrictionQueryBuilder.append(" OR ").append(
                                getLevel2ArchivesRestrictionQuery(rightSet.getKey()));
                    }
                }
            }
        }
        restrictionQueryBuilder.append(")");
        return restrictionQueryBuilder.toString();
    }

    /**
     * Generate a subquery that restrict to a certain level2 and entities with certain state
     * 
     * @param state
     * @param level2Id
     * @return StringBuilder subRestrictionQuery
     */
    private StringBuilder getDataEntitiesRestrictionQuery(EntityState state, String level2Id) {
        StringBuilder subRestrictionQueryBuilder = new StringBuilder("(");
        subRestrictionQueryBuilder.append(EntitiesSearchField.STATE.getFieldName()).append(":").append(state.name());
        subRestrictionQueryBuilder.append(" AND NOT ").append(EntitiesSearchField.CONTENT_MODEL.getFieldName()).append(
                ":").append(FixedContentModel.LEVEL1.getName());
        subRestrictionQueryBuilder.append(" AND NOT ").append(EntitiesSearchField.CONTENT_MODEL.getFieldName()).append(
                ":").append(FixedContentModel.LEVEL2.getName());
        if (StringUtils.isNotBlank(level2Id)) {
            subRestrictionQueryBuilder.append(" AND ").append(EntitiesSearchField.LEVEL2.getFieldName()).append(
                    ":").append(level2Id);
        }
        subRestrictionQueryBuilder.append(")");
        return subRestrictionQueryBuilder;
    }

    /**
     * Generate a subquery that restrict to a certain level2
     * 
     * @param level2Id
     * @return BoolQueryBuilder subRestrictionQuery
     */
    private StringBuilder getLevel2EntitiesRestrictionQuery(String level2Id) {
        StringBuilder subRestrictionQueryBuilder = new StringBuilder("(");
        subRestrictionQueryBuilder.append(EntitiesSearchField.CONTENT_MODEL.getFieldName()).append(":").append(
                FixedContentModel.LEVEL2.getName());
        if (StringUtils.isNotBlank(level2Id)) {
            subRestrictionQueryBuilder.append(" AND ").append(EntitiesSearchField.LEVEL2.getFieldName()).append(":")
                    .append(
                            level2Id);
        }
        subRestrictionQueryBuilder.append(")");
        return subRestrictionQueryBuilder;
    }

    /**
     * Generate a subquery that restrict to a certain level2 and archives with certain state
     * 
     * @param state
     * @param level2Id
     * @return StringBuilder subRestrictionQuery
     */
    private StringBuilder getDataArchivesRestrictionQuery(EntityState state, String level2Id) {
        StringBuilder subRestrictionQueryBuilder = new StringBuilder("(");
        subRestrictionQueryBuilder.append(ArchivesSearchField.STATE.getFieldName()).append(":").append(state.name());
        subRestrictionQueryBuilder.append(" AND NOT ").append(ArchivesSearchField.CONTENT_MODEL.getFieldName()).append(
                ":").append(FixedContentModel.LEVEL1.getName());
        subRestrictionQueryBuilder.append(" AND NOT ").append(ArchivesSearchField.CONTENT_MODEL.getFieldName()).append(
                ":").append(FixedContentModel.LEVEL2.getName());
        if (StringUtils.isNotBlank(level2Id)) {
            subRestrictionQueryBuilder.append(" AND ").append(ArchivesSearchField.LEVEL2.getFieldName()).append(
                    ":").append(level2Id);
        }
        subRestrictionQueryBuilder.append(")");
        return subRestrictionQueryBuilder;
    }

    /**
     * Generate a subquery that restrict to a certain level2
     * 
     * @param level2Id
     * @return BoolQueryBuilder subRestrictionQuery
     */
    private StringBuilder getLevel2ArchivesRestrictionQuery(String level2Id) {
        StringBuilder subRestrictionQueryBuilder = new StringBuilder("(");
        subRestrictionQueryBuilder.append(ArchivesSearchField.CONTENT_MODEL.getFieldName()).append(":").append(
                FixedContentModel.LEVEL2.getName());
        if (StringUtils.isNotBlank(level2Id)) {
            subRestrictionQueryBuilder.append(" AND ").append(ArchivesSearchField.LEVEL2.getFieldName()).append(":")
                    .append(
                            level2Id);
        }
        subRestrictionQueryBuilder.append(")");
        return subRestrictionQueryBuilder;
    }
}
