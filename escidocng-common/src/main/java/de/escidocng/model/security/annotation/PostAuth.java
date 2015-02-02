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
 * WITHOUT WARRANTIES OR CONDITIONS OF ROLE_ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.escidocng.model.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.escidocng.model.security.ObjectType;

/**
 * Annotation for a Security-Check that has to get executed after a method was called.<br>
 * Holds Attributes to define how to get the Object to check<br>
 * + the Permissions a user has to have to be allowed to access the annotated method
 * 
 * @author mih
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface PostAuth {

    // number of method-parameter that holds the id of the object to check
    int idIndex() default -1;

    // number of method-parameter that holds the versionId of the object to check
    int versionIndex() default -1;

    // objectType of the object to check
    ObjectType objectType() default ObjectType.ENTITY;

    // Permissions the user has to have to be allowed to access the annotated method.
    Permission[] permissions();

}
