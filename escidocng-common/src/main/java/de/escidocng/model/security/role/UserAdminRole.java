/**
 * 
 */

package de.escidocng.model.security.role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.escidocng.model.EntityHierarchy;
import de.escidocng.model.security.ObjectType;
import de.escidocng.model.security.PermissionAnchorType;
import de.escidocng.model.security.PermissionType;
import de.escidocng.model.security.User;
import de.escidocng.model.security.annotation.Permission;

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

}
