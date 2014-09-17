/**
 * 
 */
package net.objecthunter.larch.model.security.role;

import net.objecthunter.larch.model.security.annotation.Permission;

/**
 * @author mih
 *
 */
public class TestAdminRole extends TestRole {
    
    public TestAdminRole() {
        super();
        setRoleName(RoleName.ADMIN);
    }
        
    @Override
    public boolean compare(Permission permission, Object checkObject) {
        if (!roleName.equals(permission.rolename())) {
            return false;
        }
        return true;
    }

}
