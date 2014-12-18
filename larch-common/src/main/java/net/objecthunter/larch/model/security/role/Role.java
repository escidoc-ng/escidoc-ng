/**
 * 
 */
package net.objecthunter.larch.model.security.role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        @Type(value = AdminRole.class, name = "ROLE_ADMIN"),
        @Type(value = UserRole.class, name = "ROLE_USER"),
        @Type(value = UserAdminRole.class, name = "ROLE_USER_ADMIN"),
        @Type(value = Level1AdminRole.class, name = "ROLE_LEVEL1_ADMIN")
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
     * @return List<Right> rights
     */
    public abstract List<Right> getRights();

    /**
     * Set the Rights.
     * 
     * @param rights
     * @throws IOException
     */
    public abstract void setRights(List<Right> rights) throws IOException;
    
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
     * Validate the Rights in the extending role against the allowed Rights in the Annotation.
     * 
     * @throws IOException
     */
    public abstract void validate() throws IOException;
    
    /**
     * Check if right-list contains Right with given anchorId.
     * 
     * @param anchorId
     * @return boolean true|false
     */
    public boolean hasRight(String anchorId) {
        if (anchorId != null && getRights() != null) {
            for(Right r : getRights()) {
                if (anchorId.equals(r.getAnchorId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * get Right with given anchorId.
     * 
     * @param anchorId
     * @return Right
     */
    public Right getRight(String anchorId) {
        if (anchorId != null && getRights() != null) {
            for(Right r : getRights()) {
                if (anchorId.equals(r.getAnchorId())) {
                    return r;
                }
            }
        }
        return null;
    }
    
    /**
     * remove Right with given anchorId.
     * 
     * @param anchorId
     */
    public void removeRight(String anchorId) {
        int index = -1;
        if (anchorId != null && getRights() != null) {
            for (int i = 0; i < getRights().size(); i++) {
                if (anchorId.equals(getRights().get(i).getAnchorId())) {
                    index = i;
                    break;
                }
            }
        }
        if (index > -1) {
            getRights().remove(index);
        }
    }
    
    /**
     * retrieve all anchorIds.
     * 
     * @return List of anchorIds
     */
    public List<String> retrieveAnchorIds() {
        List<String> anchorIds = new ArrayList<String>();
        if (getRights() != null) {
            for (Right right : getRights()) {
                if (right.getAnchorId() != null) {
                    anchorIds.add(right.getAnchorId());
                }
            }
        }
        return anchorIds;
    }
    

    
    
    /**
     * Get an extending Class of this class, depending on the RoleName
     * 
     * @param roleName
     * @return Role
     * @throws IOException
     */
    public static Role getRoleObject(RoleName roleName) throws IOException {
        if (RoleName.ROLE_ADMIN.equals(roleName)) {
            return new AdminRole();
        } else if (RoleName.ROLE_USER.equals(roleName)) {
            return new UserRole();
        } else if (RoleName.ROLE_USER_ADMIN.equals(roleName)) {
            return new UserAdminRole();
        } else if (RoleName.ROLE_LEVEL1_ADMIN.equals(roleName)) {
            return new Level1AdminRole();
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
        ROLE_ADMIN,
        ROLE_USER,
        ROLE_USER_ADMIN,
        ROLE_LEVEL1_ADMIN,
        ROLE_ANY;
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
        READ_LEVEL2,
        WRITE_LEVEL2,
        READ,
        WRITE;

    }

}
