/**
 * 
 */
package net.objecthunter.larch.model.security.role;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.objecthunter.larch.model.security.annotation.Permission;

/**
 * @author mih
 *
 */
public class TestAreaAdminRole extends TestRole {
    
    public TestAreaAdminRole() {
        super();
        setRoleName(RoleName.AREA_ADMIN);
    }
    
    private Map<String, List<DefaultRoleRight>> rights;
    
    /**
     * @return the rights
     */
    public Map<String, List<DefaultRoleRight>> getRights() {
        return rights;
    }

    
    /**
     * @param rights the rights to set
     */
    public void setRights(Map<String, List<DefaultRoleRight>> rights) {
        this.rights = new HashMap<String, List<DefaultRoleRight>>();
        if (rights != null) {
            for (String key : rights.keySet()) {
                this.rights.put(key, rights.get(key));
            }
        }
    }
    
    @Override
    public boolean compare(Permission permission, Object checkObject) {
        if (!roleName.equals(permission.rolename())) {
            return false;
        }
        return false;
    }

}
