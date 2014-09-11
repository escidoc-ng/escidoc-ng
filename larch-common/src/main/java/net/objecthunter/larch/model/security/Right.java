/**
 * 
 */
package net.objecthunter.larch.model.security;

import net.objecthunter.larch.model.Entity.EntityState;


/**
 * @author mih
 *
 */
public class Right {

    private PermissionType permissionType;
    private ObjectType objectType;
    private EntityState state;
    private boolean tree = true;
    
    /**
     * @return the tree
     */
    public boolean isTree() {
        return tree;
    }

    
    /**
     * @param tree the tree to set
     */
    public void setTree(boolean tree) {
        this.tree = tree;
    }

    /**
     * @return the permissionType
     */
    public Right.PermissionType getPermissionType() {
        return permissionType;
    }
    
    /**
     * @param permissionType the permissionType to set
     */
    public void setPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
    }
    
    /**
     * @return the objectType
     */
    public Right.ObjectType getObjectType() {
        return objectType;
    }
    
    /**
     * @param objectType the objectType to set
     */
    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }
    
    /**
     * @return the state
     */
    public EntityState getState() {
        return state;
    }
    
    /**
     * @param state the state to set
     */
    public void setState(EntityState state) {
        this.state = state;
    }
    
    public enum PermissionType {
        READ,
        WRITE,
        NULL;
    }

    /**
     * Defines the type of the Object to check against permissions<br>
     * 
     * @author mih
     */
    public enum ObjectType {
        ENTITY,
        BINARY,
        INPUT_ENTITY;
    }

}

