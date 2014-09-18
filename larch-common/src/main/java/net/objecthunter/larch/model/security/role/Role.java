/**
 * 
 */
package net.objecthunter.larch.model.security.role;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
        @Type(value = AdminRole.class, name = "ADMIN"),
        @Type(value = UserRole.class, name = "USER"),
        @Type(value = UserAdminRole.class, name = "USER_ADMIN"),
        @Type(value = AreaAdminRole.class, name = "AREA_ADMIN")
        })
public abstract class Role {
    
    public abstract RoleName getRoleName();
    
    public abstract Map<String, List<RoleRight>> getRights();

    public abstract void setRights(Map<String, List<RoleRight>> rights) throws IOException;
    
    public static Role getRoleObject(RoleName roleName) throws IOException {
        if (RoleName.ADMIN.equals(roleName)) {
            return new AdminRole();
        } else if (RoleName.USER.equals(roleName)) {
            return new UserRole();
        } else if (RoleName.USER_ADMIN.equals(roleName)) {
            return new UserAdminRole();
        } else if (RoleName.AREA_ADMIN.equals(roleName)) {
            return new AreaAdminRole();
        } else {
            throw new IOException("roleName not supported");
        }
    }
    
    public abstract boolean compare(Permission permission, Object checkObject);

    public abstract boolean validate();

    public enum RoleName {
        ADMIN,
        USER,
        USER_ADMIN,
        AREA_ADMIN,
        ANY;
    }

    public enum RoleRight {
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
        WRITE_PERMISSION,
        READ,
        WRITE;

    }

}
