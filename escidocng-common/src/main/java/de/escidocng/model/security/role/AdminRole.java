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
import de.escidocng.model.security.annotation.Permission;

/**
 * Admin-Role.
 * Not allowed to set any Rights.
 * 
 * @author mih
 *
 */
public class AdminRole extends Role {
    private RoleName roleName = RoleName.ROLE_ADMIN;
    
    private List<RoleRight> allowedRoleRights = new ArrayList<RoleRight>();
    
    @Override
    public RoleName getRoleName() {
        return roleName;
    }
    
    @Override
    public List<RoleRight> allowedRights() {
        return allowedRoleRights;
    }

    @Override
    public void setRights(List<Right> rights) throws IOException {
        if (rights != null) {
            throw new IOException("not allowed to set rights for role " + getRoleName());
        }
    }

    @Override
    public List<Right> getRights(){return null;};
    
    @Override
    public List<PermissionAnchorType> anchorTypes() {
        return null;
    }

    @Override
    public boolean compare(Permission permission, ObjectType objectType, Object checkObject, EntityHierarchy entityHierarchy) {
        if (!roleName.equals(permission.rolename())) {
            return false;
        }
        return true;
    }

}
