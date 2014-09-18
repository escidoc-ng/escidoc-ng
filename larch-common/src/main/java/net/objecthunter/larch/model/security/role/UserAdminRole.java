/**
 * 
 */
package net.objecthunter.larch.model.security.role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.objecthunter.larch.model.security.annotation.Permission;
import net.objecthunter.larch.model.security.role.Role.RoleRight;

/**
 * @author mih
 *
 */
public class UserAdminRole extends Role {
    
    private RoleName roleName = RoleName.USER_ADMIN;
    
    private Map<String, List<RoleRight>> rights;
    
    private List<RoleRight> allowedRoleRights = new ArrayList<RoleRight>(){{
        add(RoleRight.WRITE);
        add(RoleRight.READ);
    }};
    
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
        if (rights != null) {
            for (List<RoleRight> value : rights.values()) {
                for(RoleRight right : value) {
                    if (!allowedRoleRights.contains(right)) {
                        throw new IOException("right " + right + " not allowed");
                    }
                }
            }
        }
        this.rights = rights;
    }
    
    @Override
    public boolean compare(Permission permission, Object checkObject) {
        if (!roleName.equals(permission.rolename())) {
            return false;
        }
        return false;
    }

    @Override
    public boolean validate() {
        if (rights != null) {
            for (List<RoleRight> value : rights.values()) {
                for(RoleRight right : value) {
                    if (!allowedRoleRights.contains(right)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
