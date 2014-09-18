/**
 * 
 */
package net.objecthunter.larch.model.security.role;

import java.io.IOException;
import java.util.Map;

import net.objecthunter.larch.model.security.annotation.Permission;

/**
 * @author mih
 *
 */
public class TestAdminRole extends TestRole {
    private RoleName roleName = RoleName.ADMIN;
    
    public RoleName roleName() {
        return roleName;
    }
    
    @Override
    public void setRights(Map<String,java.util.List<RoleRight>> rights) throws IOException {
        throw new IOException("not allowed to set rigths for this role");
    }

    @Override
    public Map<String,java.util.List<RoleRight>> getRights(){return null;};
    
    @Override
    public boolean compare(Permission permission, Object checkObject) {
        if (!roleName.equals(permission.rolename())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean validate() {
        return true;
    }

}
