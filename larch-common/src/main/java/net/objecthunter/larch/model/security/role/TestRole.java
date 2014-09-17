/**
 * 
 */
package net.objecthunter.larch.model.security.role;

import net.objecthunter.larch.model.security.annotation.Permission;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * @author mih
 *
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "roleName")
    @JsonSubTypes({
        @Type(value = TestAdminRole.class, name = "ADMIN"),
        @Type(value = TestUserRole.class, name = "USER"),
        @Type(value = TestUserAdminRole.class, name = "USER_ADMIN"),
        @Type(value = TestAreaAdminRole.class, name = "AREA_ADMIN")
        })
public abstract class TestRole {
    
    protected RoleName roleName;
    
    
    /**
     * @param roleName the roleName to set
     */
    protected void setRoleName(RoleName roleName) {
        this.roleName = roleName;
    }

    /**
     * @return the roleName
     */
    public RoleName getRoleName() {
        return roleName;
    }
    
    public 


    public boolean compare(Permission permission, Object checkObject) {
        return false;
    }
    
    public boolean isValid() {
        return false;
    }
    
    public enum RoleName {
        ADMIN,
        USER,
        USER_ADMIN,
        AREA_ADMIN,
        ANY;
    }
    
    public interface RoleRight {}
    
    public enum DefaultRoleRight implements RoleRight {
        READ,
        WRITE;
    }


}
