/**
 * 
 */
package net.objecthunter.larch.model.security;


/**
 * @author mih
 *
 */
public class Role {
    
    private Group group;
    
    private Rights rights;

    
    /**
     * @return the group
     */
    public Group getGroup() {
        return group;
    }

    
    /**
     * @param group the group to set
     */
    public void setGroup(Group group) {
        this.group = group;
    }

    
    /**
     * @return the rights
     */
    public Rights getRights() {
        return rights;
    }

    
    /**
     * @param rights the rights to set
     */
    public void setRights(Rights rights) {
        this.rights = rights;
    }

}
