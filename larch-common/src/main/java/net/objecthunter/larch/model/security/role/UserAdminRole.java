/**
 * 
 */

package net.objecthunter.larch.model.security.role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.objecthunter.larch.model.EntityHierarchy;
import net.objecthunter.larch.model.security.ObjectType;
import net.objecthunter.larch.model.security.PermissionAnchorType;
import net.objecthunter.larch.model.security.PermissionType;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.annotation.Permission;

/**
 * UserAdmin-Role. Set READ or WRITE-Rights for an User or for all users (anchorId = "").
 * 
 * @author mih
 */
public class UserAdminRole extends Role {

    private RoleName roleName = RoleName.ROLE_USER_ADMIN;

    private List<Right> rights;

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
        if (checkObject == null || !(checkObject instanceof User)) {
            return false;
        }
        User checkUser = (User) checkObject;
        if (this.rights == null || (!hasRight(checkUser.getName()) && !hasRight(""))) {
            return false;
        }
        if (permission.permissionType().equals(PermissionType.WRITE) &&
                (!hasRight(checkUser.getName()) || getRight(checkUser.getName()).getRoleRights() == null || !getRight(
                        checkUser.getName()).getRoleRights().contains(
                        RoleRight.WRITE)) &&
                (!hasRight("") || getRight("").getRoleRights() == null || !getRight(
                        "").getRoleRights().contains(
                        RoleRight.WRITE))) {
            return false;
        }
        if (permission.permissionType().equals(PermissionType.READ) &&
                (!hasRight(checkUser.getName()) || getRight(checkUser.getName()).getRoleRights() == null || !getRight(
                        checkUser.getName()).getRoleRights().contains(
                        RoleRight.READ)) &&
                (!hasRight("") || getRight("").getRoleRights() == null || !getRight(
                        "").getRoleRights().contains(
                        RoleRight.READ))) {
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
