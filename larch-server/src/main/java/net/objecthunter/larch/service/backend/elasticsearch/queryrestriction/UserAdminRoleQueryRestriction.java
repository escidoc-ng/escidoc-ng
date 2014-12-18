/**
 * 
 */
package net.objecthunter.larch.service.backend.elasticsearch.queryrestriction;

import java.util.List;

import net.objecthunter.larch.model.security.role.Right;
import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.model.security.role.Role.RoleRight;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mih
 *
 */
public class UserAdminRoleQueryRestriction extends RoleQueryRestriction {

    public UserAdminRoleQueryRestriction(Role role) {
        super(role);
    }

    /**
     * User-Admin may see no entities.
     */
    @Override
    public String getEntitiesRestrictionQuery() {
        StringBuilder restrictionQueryBuilder = new StringBuilder("(");
        // restrict to nothing
        restrictionQueryBuilder.append(EntitiesSearchField.STATE.getFieldName()).append(":NONEXISTING");
        restrictionQueryBuilder.append(")");
        return restrictionQueryBuilder.toString();
    }

    /**
     * User-Admin may see users according to the rights he has (blank: all users, otherwise users with id of right).
     */
    @Override
    public String getUsersRestrictionQuery() {
        StringBuilder restrictionQueryBuilder = new StringBuilder("(");
        // restrict to nothing
        restrictionQueryBuilder.append("name:NONEXISTING");

        // add restrictions
        if (getRole() != null && getRole().getRights() != null) {
            for (Right right : getRole().getRights()) {
                List<RoleRight> userRights = right.getRoleRights();
                for (RoleRight userRight : userRights) {
                    if (RoleRight.READ.equals(userRight)) {
                        if (StringUtils.isNotBlank(right.getAnchorId())) {
                            restrictionQueryBuilder.append(" OR name:").append(right.getAnchorId());
                        } else {
                            restrictionQueryBuilder.append(" OR name:*");
                        }
                    }
                }
            }
        }
        restrictionQueryBuilder.append(")");
        return restrictionQueryBuilder.toString();
    }

    /**
     * User-Admin may see no archives.
     */
    @Override
    public String getArchivesRestrictionQuery() {
        StringBuilder restrictionQueryBuilder = new StringBuilder("(");
        // restrict to nothing
        restrictionQueryBuilder.append(EntitiesSearchField.STATE.getFieldName()).append(":NONEXISTING");
        restrictionQueryBuilder.append(")");
        return restrictionQueryBuilder.toString();
    }

}
