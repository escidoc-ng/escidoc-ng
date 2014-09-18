/**
 * 
 */
package net.objecthunter.larch.model.security.role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.objecthunter.larch.model.security.annotation.Permission;

/**
 * @author mih
 *
 */
public class TestUserRole extends TestRole {
    
    private RoleName roleName = RoleName.USER;
    
    private Map<String, List<RoleRight>> rights;
    
    private List<RoleRight> allowedRoleRights = new ArrayList<RoleRight>(){{
        add(RoleRight.READ_PENDING_METADATA);
        add(RoleRight.READ_SUBMITTED_METADATA);
        add(RoleRight.READ_PUBLISHED_METADATA);
        add(RoleRight.READ_WITHDRAWN_METADATA);
        add(RoleRight.WRITE_PENDING_METADATA);
        add(RoleRight.WRITE_SUBMITTED_METADATA);
        add(RoleRight.WRITE_PUBLISHED_METADATA);
        add(RoleRight.WRITE_WITHDRAWN_METADATA);
        add(RoleRight.READ_PENDING_BINARY);
        add(RoleRight.READ_SUBMITTED_BINARY);
        add(RoleRight.READ_PUBLISHED_BINARY);
        add(RoleRight.READ_WITHDRAWN_BINARY);
        add(RoleRight.WRITE_PENDING_BINARY);
        add(RoleRight.WRITE_SUBMITTED_BINARY);
        add(RoleRight.WRITE_PUBLISHED_BINARY);
        add(RoleRight.WRITE_WITHDRAWN_BINARY);
        add(RoleRight.READ_PERMISSION);
        add(RoleRight.WRITE_PERMISSION);
    }};
    
    public RoleName roleName() {
        return roleName;
    }
    
    public List<RoleRight> allowedRights() {
        return allowedRoleRights;
    }
    
    /**
     * @return the rights
     */
    @Override
    public Map<String, List<RoleRight>> getRights() {
        return rights;
    }

    
    /**
     * @param rights the rights to set
     */
    @Override
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
        if (checkObject == null || !(checkObject instanceof String)) {
            return false;
        }
        return false;
    }

    @Override
    public boolean validate() {
        return true;
    }

}
