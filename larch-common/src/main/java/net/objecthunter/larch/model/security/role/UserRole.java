/**
 * 
 */

package net.objecthunter.larch.model.security.role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.EntityHierarchy;
import net.objecthunter.larch.model.security.ObjectType;
import net.objecthunter.larch.model.security.PermissionAnchorType;
import net.objecthunter.larch.model.security.PermissionType;
import net.objecthunter.larch.model.security.annotation.Permission;

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

    private Map<String, List<RoleRight>> rights;

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
    public Map<String, List<RoleRight>> getRights() {
        return rights;
    }

    @Override
    public void setRights(Map<String, List<RoleRight>> rights) throws IOException {
        validate(rights);
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
        if (this.rights == null || !this.rights.containsKey(entityHierarchy.getLevel2Id()) ||
                this.rights.get(entityHierarchy.getLevel2Id()) == null) {
            return false;
        }
        if (this.rights.get(entityHierarchy.getLevel2Id()) == null) {
            return false;
        }
        if (FixedContentModel.LEVEL2.getName().equals(checkEntity.getContentModelId())) {
            if (permission.permissionType().equals(PermissionType.READ)) {
                if (!this.rights.get(entityHierarchy.getLevel2Id()).contains(RoleRight.READ_LEVEL2)) {
                    return false;
                }
            } else if (permission.permissionType().equals(PermissionType.WRITE)) {
                if (!this.rights.get(entityHierarchy.getLevel2Id()).contains(RoleRight.WRITE_LEVEL2)) {
                    return false;
                }
            }
        } else {
            if (!this.rights.get(entityHierarchy.getLevel2Id()).contains(
                    validateMatrix.get("" + permission.permissionType() + checkEntity.getState() + objectType))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void validate() throws IOException {
        validate(rights);
    }

    private void validate(Map<String, List<RoleRight>> rights) throws IOException {
        if (rights != null) {
            for (List<RoleRight> value : rights.values()) {
                for (RoleRight right : value) {
                    if (!allowedRoleRights.contains(right)) {
                        throw new IOException("right " + right + " not allowed for role " + getRoleName());
                    }
                }
            }
        }
    }

}
