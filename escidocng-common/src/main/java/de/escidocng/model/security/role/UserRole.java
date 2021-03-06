/**
 * 
 */

package de.escidocng.model.security.role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.escidocng.model.Entity;
import de.escidocng.model.EntityHierarchy;
import de.escidocng.model.ContentModel.FixedContentModel;
import de.escidocng.model.Entity.EntityState;
import de.escidocng.model.security.ObjectType;
import de.escidocng.model.security.PermissionAnchorType;
import de.escidocng.model.security.PermissionType;
import de.escidocng.model.security.annotation.Permission;

/**
 * User-Role. Set READ or WRITE-Rights for a Permission (anchorId = level2Id). Rights READ_LEVEL2 and
 * WRITE_LEVEL2 enable Reading/Writing of the permission with the anchorId. The other Rights enable
 * Reading/Writing Entities/Binaries of Entities<br>
 * in a certain state that are somewhere in the tree below the permission with the anchorId.
 * 
 * @author mih
 */
public class UserRole extends Role {

    private RoleName roleName = RoleName.ROLE_USER;

    private List<Right> rights;

    private List<RoleRight> allowedRoleRights = new ArrayList<RoleRight>() {

        {
            add(RoleRight.READ_PENDING_METADATA);
            add(RoleRight.READ_SUBMITTED_METADATA);
            add(RoleRight.READ_PUBLISHED_METADATA);
            add(RoleRight.READ_WITHDRAWN_METADATA);
            add(RoleRight.WRITE_PENDING_METADATA);
            add(RoleRight.WRITE_SUBMITTED_METADATA);
            add(RoleRight.WRITE_PUBLISHED_METADATA);
            add(RoleRight.WRITE_WITHDRAWN_METADATA);
            add(RoleRight.READ_PENDING_BINARY);
            add(RoleRight.READ_SUBMITTED_BINARY);
            add(RoleRight.READ_PUBLISHED_BINARY);
            add(RoleRight.READ_WITHDRAWN_BINARY);
            add(RoleRight.WRITE_PENDING_BINARY);
            add(RoleRight.WRITE_SUBMITTED_BINARY);
            add(RoleRight.WRITE_PUBLISHED_BINARY);
            add(RoleRight.WRITE_WITHDRAWN_BINARY);
            add(RoleRight.READ_LEVEL2);
            add(RoleRight.WRITE_LEVEL2);
        }
    };

    private List<PermissionAnchorType> allowedPermissionAnchors = new ArrayList<PermissionAnchorType>() {

        {
            add(PermissionAnchorType.LEVEL2_ENTITY);
        }
    };

    private Map<String, RoleRight> validateMatrix = new HashMap<String, RoleRight>() {

        {
            put("" + PermissionType.READ + EntityState.PENDING + ObjectType.BINARY, RoleRight.READ_PENDING_BINARY);
            put("" + PermissionType.READ + EntityState.SUBMITTED + ObjectType.BINARY, RoleRight.READ_SUBMITTED_BINARY);
            put("" + PermissionType.READ + EntityState.PUBLISHED + ObjectType.BINARY, RoleRight.READ_PUBLISHED_BINARY);
            put("" + PermissionType.READ + EntityState.WITHDRAWN + ObjectType.BINARY, RoleRight.READ_WITHDRAWN_BINARY);
            put("" + PermissionType.READ + EntityState.PENDING + ObjectType.ENTITY, RoleRight.READ_PENDING_METADATA);
            put("" + PermissionType.READ + EntityState.SUBMITTED + ObjectType.ENTITY,
                    RoleRight.READ_SUBMITTED_METADATA);
            put("" + PermissionType.READ + EntityState.PUBLISHED + ObjectType.ENTITY,
                    RoleRight.READ_PUBLISHED_METADATA);
            put("" + PermissionType.READ + EntityState.WITHDRAWN + ObjectType.ENTITY,
                    RoleRight.READ_WITHDRAWN_METADATA);
            put("" + PermissionType.WRITE + EntityState.PENDING + ObjectType.BINARY, RoleRight.WRITE_PENDING_BINARY);
            put("" + PermissionType.WRITE + EntityState.SUBMITTED + ObjectType.BINARY,
                    RoleRight.WRITE_SUBMITTED_BINARY);
            put("" + PermissionType.WRITE + EntityState.PUBLISHED + ObjectType.BINARY,
                    RoleRight.WRITE_PUBLISHED_BINARY);
            put("" + PermissionType.WRITE + EntityState.WITHDRAWN + ObjectType.BINARY,
                    RoleRight.WRITE_WITHDRAWN_BINARY);
            put("" + PermissionType.WRITE + EntityState.PENDING + ObjectType.ENTITY, RoleRight.WRITE_PENDING_METADATA);
            put("" + PermissionType.WRITE + EntityState.SUBMITTED + ObjectType.ENTITY,
                    RoleRight.WRITE_SUBMITTED_METADATA);
            put("" + PermissionType.WRITE + EntityState.PUBLISHED + ObjectType.ENTITY,
                    RoleRight.WRITE_PUBLISHED_METADATA);
            put("" + PermissionType.WRITE + EntityState.WITHDRAWN + ObjectType.ENTITY,
                    RoleRight.WRITE_WITHDRAWN_METADATA);
        }
    };

    @Override
    public RoleName getRoleName() {
        return roleName;
    }

    @Override
    public List<RoleRight> allowedRights() {
        return allowedRoleRights;
    }

    @Override
    public List<Right> getRights() {
        return rights;
    }

    @Override
    public void setRights(List<Right> rights) throws IOException {
        this.rights = rights;
    }

    @Override
    public List<PermissionAnchorType> anchorTypes() {
        return allowedPermissionAnchors;
    }

    @Override
    public boolean compare(Permission permission, ObjectType objectType, Object checkObject,
            EntityHierarchy entityHierarchy) {
        if (!roleName.equals(permission.rolename())) {
            return false;
        }
        if (checkObject == null || !(checkObject instanceof Entity)) {
            return false;
        }
        if (objectType == null) {
            return false;
        }
        Entity checkEntity = (Entity) checkObject;
        if (entityHierarchy == null || entityHierarchy.getLevel2Id() == null) {
            return false;
        }
        if (this.rights == null || !hasRight(entityHierarchy.getLevel2Id()) ||
                getRight(entityHierarchy.getLevel2Id()).getRoleRights() == null) {
            return false;
        }
        if (getRight(entityHierarchy.getLevel2Id()).getRoleRights() == null) {
            return false;
        }
        if (FixedContentModel.LEVEL2.getName().equals(checkEntity.getContentModelId())) {
            if (permission.permissionType().equals(PermissionType.READ)) {
                if (!getRight(entityHierarchy.getLevel2Id()).getRoleRights().contains(RoleRight.READ_LEVEL2)) {
                    return false;
                }
                if (!getRight(entityHierarchy.getLevel2Id()).getRoleRights().contains(RoleRight.READ_LEVEL2)) {
                    return false;
                }
            } else if (permission.permissionType().equals(PermissionType.WRITE)) {
                if (!getRight(entityHierarchy.getLevel2Id()).getRoleRights().contains(RoleRight.WRITE_LEVEL2)) {
                    return false;
                }
            }
        } else {
            EntityState state = checkEntity.getState();
            if (state == null) {
                state = EntityState.PENDING;
            }
            if (!getRight(entityHierarchy.getLevel2Id()).getRoleRights().contains(
                    validateMatrix.get("" + permission.permissionType() + state + objectType))) {
                return false;
            }
        }
        return true;
    }

}
