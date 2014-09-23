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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package net.objecthunter.larch.service;

import java.io.IOException;
import java.lang.reflect.Method;

import net.objecthunter.larch.model.security.ObjectType;
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
    void authorize(Method method, ObjectType objectType, String id, Integer versionId, Object result, Permission[] permissions, Object[] methodArgs) throws IOException;

}
