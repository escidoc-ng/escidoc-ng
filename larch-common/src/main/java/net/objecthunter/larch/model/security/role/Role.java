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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * Abstract Class that defines the methods an extending class has to implement<br>
 * and that holds Enums used by these methods.
 * JSON-Annotations enable parsing JSON-String as distinct Role-classes, dependent on the RoleName.
 * 
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
    
    /**
     * get the name of the Role
     * 
     * @return RoleName the RoleName
     */
    public abstract RoleName getRoleName();
    
    /**
     * Get the Rights for the different anchorIds as Map.
     * 
     * @return Map<String, List<RoleRight>> rights
     */
    public abstract Map<String, List<RoleRight>> getRights();

    /**
     * Set the Rights.
     * 
     * @param rights
     * @throws IOException
     */
    public abstract void setRights(Map<String, List<RoleRight>> rights) throws IOException;
    
    /**
     * Returns the anchorTypes that are supported by the extending role.
     * 
     * @return List<PermissionAnchorType> the supported anchorTypes
     */
    public abstract List<PermissionAnchorType> anchorTypes();

    /**
     * Return the RoleRights that are allowed to set in the extending role.
     * 
     * @return List<RoleRight> the RoleRights that are allowed to set in the extending role.
     */
    public abstract List<RoleRight> allowedRights();

    /**
     * Compare the Role + Rights against the Parameters taken from the Method-Annotation.
     * Return true if the user may access the method with the extending role, otherwise false.
     * 
     * @param permission
     * @param objectType
     * @param checkObject
     * @param entityHierarchy
     * @return boolean
     */
    public abstract boolean compare(Permission permission, ObjectType objectType, Object checkObject, EntityHierarchy entityHierarchy);

    /**
     * Validate the Rights in the extending role against the allowed Rights.
     * 
     * @throws IOException
     */
    public abstract void validate() throws IOException;

    /**
     * Get an extending Class of this class, depending on the RoleName
     * 
     * @param roleName
     * @return Role
     * @throws IOException
     */
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
    
    /**
     * Holds all RoleNames supported by the system.
     * 
     * @author mih
     *
     */
    public enum RoleName {
        ADMIN,
        USER,
        USER_ADMIN,
        AREA_ADMIN,
        ANY;
    }

    /**
     * Holds all RoleRights supported by the system.
     * 
     * @author mih
     *
     */
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
