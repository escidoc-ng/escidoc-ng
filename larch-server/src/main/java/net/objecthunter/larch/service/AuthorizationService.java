/* 
 * Copyright 2014 Frank Asseg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ROLE_ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package net.objecthunter.larch.service;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.security.ObjectType;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.model.security.annotation.Permission;

public interface AuthorizationService {

    /**
     * Take the Parameters from the PreAuth or PostAuth-Annotation + the called Method.
     * Get calling user and try to evaluate the roles of the user against the Parameters.
     * Throws AuthorizationException if user is null, has no roles or none of his roles evaluates with true.
     * 
     * @param method
     * @param objectType
     * @param id
     * @param versionId
     * @param result
     * @param permissions
     * @param methodArgs
     * @throws IOException
     */
    void authorize(Method method, ObjectType objectType, String id, Integer versionId, Object result, Permission[] permissions) throws IOException;

    /**
     * Get id from method-parameters
     * 
     * @param idIndex
     * @param objectType
     * @param args
     * @return String or null
     */
    String getId(final int idIndex, final ObjectType objectType, final Object[] args);
    
    /**
     * Get version-Id from method-parameters
     * 
     * @param versionIndex
     * @param args
     * @return Integer versionId or null
     */
    Integer getVersionId(final int versionIndex, final Object[] args) throws IOException;
    
    /**
     * Get Entity-Object from method-parameters
     * 
     * @param idIndex
     * @param objectType
     * @param args
     * @return Entity or null
     */
    Entity getObject(final int idIndex, final ObjectType objectType, final Object[] args);
    
    /**
     * Get currently logged in User or null if no user is logged in.
     * 
     * @return User logged in user
     */
    User getCurrentUser();
    
}
