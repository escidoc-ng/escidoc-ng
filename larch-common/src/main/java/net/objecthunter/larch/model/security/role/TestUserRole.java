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
public class TestUserRole extends TestRole {
    
    public TestUserRole() {
        super();
        setRoleName(RoleName.USER);
    }
    
    private Map<String, List<UserRoleRight>> rights;
    
    /**
     * @return the rights
     */
    public Map<String, List<UserRoleRight>> getRights() {
        return rights;
    }

    
    /**
     * @param rights the rights to set
     */
    public void setRights(Map<String, List<UserRoleRight>> rights) {
        this.rights = new HashMap<String, List<UserRoleRight>>();
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
        if (checkObject == null || !(checkObject instanceof String)) {
            return false;
        }
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public enum UserRoleRight implements RoleRight {
        READ_PENDING_METADATA,
        READ_SUBMITTED_METADATA,
        READ_PUBLISHED_METADATA,
        READ_WITHDRAWN_METADATA,
        WRITE_PENDING_METADATA,
        WRITE_SUBMITTED_METADATA,
        WRITE_PUBLISHED_METADATA,
        WRITE_WITHDRAWN_METADATA,
        READ_PENDING_BINARY,
        READ_SUBMITTED_BINARY,
        READ_PUBLISHED_BINARY,
        READ_WITHDRAWN_BINARY,
        WRITE_PENDING_BINARY,
        WRITE_SUBMITTED_BINARY,
        WRITE_PUBLISHED_BINARY,
        WRITE_WITHDRAWN_BINARY,
        READ_PERMISSION,
        WRITE_PERMISSION;
    }

}
