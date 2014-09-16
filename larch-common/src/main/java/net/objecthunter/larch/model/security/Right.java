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

    private ObjectType objectType;
    private PermissionType permissionType;
    private EntityState state;
    private boolean tree = true;
    
    /**
     * Default Constructor
     */
    public Right() {
    }
    
    public Right(ObjectType objectType, PermissionType permissionType, EntityState state, boolean isTree) {
        this.objectType = objectType;
        this.permissionType = permissionType;
        this.state = state;
        this.tree = isTree;
    }
    
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
        RIGHT,
        USER,
        INPUT_ENTITY;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Right right = (Right) o;

        if (objectType != null ? !objectType.equals(right.objectType) : right.objectType != null) return false;
        if (permissionType != null ? !permissionType.equals(right.permissionType) : right.permissionType != null) return false;
        if (state != null ? !state.equals(right.state) : right.state != null) return false;
        if (tree == true ? right.tree == false : right.tree == true) return false;

        return true;
    }


}

