/**
 * 
 */

package net.objecthunter.larch.model.security.role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.objecthunter.larch.model.EntityHierarchy;
import net.objecthunter.larch.model.security.ObjectType;
import net.objecthunter.larch.model.security.PermissionAnchorType;
import net.objecthunter.larch.model.security.PermissionType;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.annotation.Permission;

/**
 * @author mih
 */
public class UserAdminRole extends Role {

    private RoleName roleName = RoleName.USER_ADMIN;

    private Map<String, List<RoleRight>> rights;

    private List<RoleRight> allowedRoleRights = new ArrayList<RoleRight>() {

        {
            add(RoleRight.WRITE);
            add(RoleRight.READ);
        }
    };

    private List<PermissionAnchorType> allowedPermissionAnchors = new ArrayList<PermissionAnchorType>() {

        {
            add(PermissionAnchorType.USER);
        }
    };

    public RoleName getRoleName() {
        return roleName;
    }

    public List<RoleRight> allowedRights() {
        return allowedRoleRights;
    }

    /**
     * @return the rights
     */
    public Map<String, List<RoleRight>> getRights() {
        return rights;
    }

    /**
     * @param rights the rights to set
     */
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
        if (checkObject == null || !(checkObject instanceof User)) {
            return false;
        }
        User checkUser = (User) checkObject;
        if (this.rights == null || (!this.rights.containsKey(checkUser.getName()) && !this.rights.containsKey(""))) {
            return false;
        }
        if (permission.permissionType().equals(PermissionType.WRITE) &&
                !this.rights.get(checkUser.getName()).contains(RoleRight.WRITE) &&
                !this.rights.get("").contains(RoleRight.WRITE)) {
            return false;
        }
        if (permission.permissionType().equals(PermissionType.READ) &&
                !this.rights.get(checkUser.getName()).contains(RoleRight.READ) &&
                !this.rights.get("").contains(RoleRight.READ)) {
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
                        throw new IOException("right " + right + " not allowed");
                    }
                }
            }
        }
    }

}
