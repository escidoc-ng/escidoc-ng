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

package de.escidocng.security.helpers;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.escidocng.model.security.annotation.PostAuth;
import de.escidocng.model.security.annotation.PreAuth;
import de.escidocng.service.AuthorizationService;

/**
 * Interceptor used for securing the escidoc-ng framework.
 * <p/>
 * <p/>
 * This Interceptor is invoked every time a method in a class of package de.escidocng.controller is called.
 *
 * @author Michael Hoppe
 */
@Component
@Aspect
public class EscidocngSecurityInterceptor implements Ordered {

    @Autowired
    protected AuthorizationService authorizationService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EscidocngSecurityInterceptor.class);

    /**
     * Around advice to perform the authorization of the current request.
     * <p/>
     *
     * @param joinPoint The current {@link ProceedingJoinPoint}.
     * @throws Throwable Thrown in case of an error.
     * @return
     */
    @Around("execution(* de.escidocng.controller.*.*(..))")
    public Object authorize(final ProceedingJoinPoint joinPoint) throws Throwable {
        final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        final Method calledMethod = methodSignature.getMethod();

        PreAuth preAuth = calledMethod.getAnnotation(PreAuth.class);
        PostAuth postAuth = calledMethod.getAnnotation(PostAuth.class);

        if (preAuth != null) {
            authorizationService.authorize(calledMethod, preAuth.objectType(), authorizationService.getId(preAuth.idIndex(), preAuth.objectType(),
                    joinPoint.getArgs()), authorizationService.getVersionId(preAuth.versionIndex(), joinPoint.getArgs()),
                    authorizationService.getObject(preAuth.idIndex(), preAuth.objectType(), joinPoint.getArgs()), preAuth.permissions());
        }
        Object obj = joinPoint.proceed();
        if (postAuth != null) {
            authorizationService.authorize(calledMethod, postAuth.objectType(), null, null,
                    obj, postAuth.permissions());
        }
        return obj;
    }

    /**
     * See Interface for functional description.
     */
    @Override
    public int getOrder() {
        return 0;
    }

}
