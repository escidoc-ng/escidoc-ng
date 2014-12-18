/**
 * 
 */

package net.objecthunter.larch.model.security.role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.EntityHierarchy;
import net.objecthunter.larch.model.security.ObjectType;
import net.objecthunter.larch.model.security.PermissionAnchorType;
import net.objecthunter.larch.model.security.PermissionType;
import net.objecthunter.larch.model.security.annotation.Permission;

/**
 * Level1Admin-Role. Set READ or WRITE-Rights for an LEVEL1_ENTITY.
 * 
 * @author mih
 */
public class Level1AdminRole extends Role {

    private RoleName roleName = RoleName.ROLE_LEVEL1_ADMIN;

    private List<Right> rights;

    private List<RoleRight> allowedRoleRights = new ArrayList<RoleRight>() {

        {
            add(RoleRight.WRITE);
            add(RoleRight.READ);
        }
    };

    private List<PermissionAnchorType> allowedPermissionAnchors = new ArrayList<PermissionAnchorType>() {

        {
            add(PermissionAnchorType.LEVEL1_ENTITY);
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
        Entity checkEntity = (Entity) checkObject;
        if (entityHierarchy == null || entityHierarchy.getLevel1Id() == null) {
            return false;
        }
        // Only do something with LEVEL1_ENTITY or LEVEL2_ENTITY
        if (!FixedContentModel.LEVEL1.getName().equals(checkEntity.getContentModelId()) &&
                !FixedContentModel.LEVEL2.getName().equals(checkEntity.getContentModelId())) {
            return false;
        }
        if (!hasRight(entityHierarchy.getLevel1Id())) {
            return false;
        }
        if (permission.permissionType().equals(PermissionType.READ) &&
                (getRight(entityHierarchy.getLevel1Id()).getRoleRights() == null || !getRight(
                        entityHierarchy.getLevel1Id()).getRoleRights()
                        .contains(RoleRight.READ))) {
            return false;
        }
        if (permission.permissionType().equals(PermissionType.WRITE) &&
                (getRight(entityHierarchy.getLevel1Id()).getRoleRights() == null || !getRight(
                        entityHierarchy.getLevel1Id()).getRoleRights()
                        .contains(RoleRight.WRITE))) {
            return false;
        }
        return true;
    }

    @Override
    public void validate() throws IOException {
        validate(rights);
    }

    private void validate(List<Right> rights) throws IOException {
        if (rights != null) {
            for (Right value : rights) {
                if (value.getRoleRights() != null) {
                    for (RoleRight right : value.getRoleRights()) {
                        if (!allowedRoleRights.contains(right)) {
                            throw new IOException("right " + right + " not allowed for role " + getRoleName());
                        }
                    }
                }
            }
        }
    }

}
