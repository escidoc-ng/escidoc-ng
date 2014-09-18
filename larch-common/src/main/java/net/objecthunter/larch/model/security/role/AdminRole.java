/**
 * 
 */
package net.objecthunter.larch.model.security.role;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.objecthunter.larch.model.EntityHierarchy;
import net.objecthunter.larch.model.security.ObjectType;
import net.objecthunter.larch.model.security.PermissionAnchorType;
import net.objecthunter.larch.model.security.annotation.Permission;
import net.objecthunter.larch.model.security.role.Role.RoleRight;

/**
 * @author mih
 *
 */
public class AdminRole extends Role {
    private RoleName roleName = RoleName.ADMIN;
    
    public RoleName getRoleName() {
        return roleName;
    }
    
    @Override
    public void setRights(Map<String,java.util.List<RoleRight>> rights) throws IOException {
        if (rights != null) {
            throw new IOException("not allowed to set rigths for this role");
        }
    }

    @Override
    public Map<String,java.util.List<RoleRight>> getRights(){return null;};
    
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

    @Override
    public void validate() throws IOException {
    }

}
