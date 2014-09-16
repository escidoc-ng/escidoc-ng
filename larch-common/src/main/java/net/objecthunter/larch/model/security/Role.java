/**
 * 
 */
package net.objecthunter.larch.model.security;


/**
 * @author mih
 *
 */
public enum Role {
    ADMIN,
    USER,
    AREA_ADMIN,
    USER_ADMIN,
    ANY;

    public String getName() {
        return this.toString();
    }
    
}

