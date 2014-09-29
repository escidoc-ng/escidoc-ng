/**
 * 
 */

package net.objecthunter.larch.model.security.role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.EntityHierarchy;
import net.objecthunter.larch.model.security.ObjectType;
import net.objecthunter.larch.model.security.PermissionAnchorType;
import net.objecthunter.larch.model.security.PermissionType;
import net.objecthunter.larch.model.security.annotation.Permission;

/**
 * AreaAdmin-Role.
 * Set READ or WRITE-Rights for an LEVEL1_ENTITY.
 * 
 * @author mih
 *
 */
public class AreaAdminRole extends Role {

    private RoleName roleName = RoleName.ROLE_AREA_ADMIN;

    private Map<String, List<RoleRight>> rights;

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
        Entity checkEntity = (Entity) checkObject;
        if (entityHierarchy == null || entityHierarchy.getLevel1Id() == null) {
            return false;
        }
        // Only do something with LEVEL1_ENTITY or LEVEL2_ENTITY
        if (!FixedContentModel.LEVEL1.getName().equals(checkEntity.getContentModelId()) &&
                !FixedContentModel.LEVEL2.getName().equals(checkEntity.getContentModelId())) {
            return false;
        }
        if (this.rights == null || !this.rights.containsKey(entityHierarchy.getLevel1Id()) ||
                this.rights.get(entityHierarchy.getLevel1Id()) == null) {
            return false;
        }
        if (permission.permissionType().equals(PermissionType.READ) &&
                !this.rights.get(entityHierarchy.getLevel1Id()).contains(RoleRight.READ)) {
            return false;
        }
        if (permission.permissionType().equals(PermissionType.WRITE) &&
                !this.rights.get(entityHierarchy.getLevel1Id()).contains(RoleRight.WRITE)) {
            return false;
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
