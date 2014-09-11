/*
 * Copyright 2014 Michael Hoppe
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

package net.objecthunter.larch.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.objecthunter.larch.model.security.Rights.ObjectType;
import net.objecthunter.larch.model.security.Rights.PermissionType;

/**
 * @author mih
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Permission {

    int idIndex() default -1;

    int versionIndex() default -1;

    ObjectType objectType() default ObjectType.ENTITY;

    PermissionType permissionType() default PermissionType.NULL;
    
    String targetGroup() default "ROLE_USER";

}
