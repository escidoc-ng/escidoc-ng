/**
 * 
 */
package net.objecthunter.larch.service.backend.elasticsearch.queryrestriction;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.model.security.role.Role.RoleName;


/**
 * @author mih
 *
 */
public class QueryRestrictionFactory {
    private static Map<RoleName, Class> roleQueryRestrictionMap = new HashMap<RoleName, Class>() {{
        put(RoleName.ADMIN, AdminRoleQueryRestriction.class);
        put(RoleName.AREA_ADMIN, AreaAdminRoleQueryRestriction.class);
        put(RoleName.USER, UserRoleQueryRestriction.class);
        put(RoleName.USER_ADMIN, UserAdminRoleQueryRestriction.class);
    }};
    
    public static RoleQueryRestriction getRoleQueryRestriction(Role role) throws IOException {
        if (role == null) {
            throw new IOException("Role may not be null");
        }
        if (roleQueryRestrictionMap.get(role.getRoleName()) == null) {
            throw new IOException("No Query-Restriction Class found for role " + role.getRoleName());
        }
        try {
            return (RoleQueryRestriction)((Class)roleQueryRestrictionMap.get(role.getRoleName())).getDeclaredConstructor(Role.class).newInstance(role);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

}
