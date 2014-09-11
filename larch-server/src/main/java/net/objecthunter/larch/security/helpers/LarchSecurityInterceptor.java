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

package net.objecthunter.larch.security.helpers;

import java.io.IOException;
import java.lang.reflect.Method;

import net.objecthunter.larch.annotations.PostAuth;
import net.objecthunter.larch.annotations.PreAuth;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.security.Rights.ObjectType;
import net.objecthunter.larch.service.AuthorizationService;

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

/**
 * Interceptor used for securing the escidoc-ng framework.
 * <p/>
 * <p/>
 * This Interceptor is invoked every time an service calls one of its classes.
 *
 * @author Michael Hoppe
 */
@Component
@Aspect
public class LarchSecurityInterceptor implements Ordered {

    @Autowired
    protected AuthorizationService authorizationService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LarchSecurityInterceptor.class);

    /**
     * Around advice to perform the authorization of the current request.
     * <p/>
     *
     * @param joinPoint The current {@link ProceedingJoinPoint}.
     * @throws Throwable Thrown in case of an error.
     * @return
     */
    @Around("execution(* net.objecthunter.larch.controller.*.*(..))")
    public Object authorize(final ProceedingJoinPoint joinPoint) throws Throwable {
        final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        final Method calledMethod = methodSignature.getMethod();

        PreAuth preAuth = calledMethod.getAnnotation(PreAuth.class);
        PostAuth postAuth = calledMethod.getAnnotation(PostAuth.class);

        if (preAuth != null) {
            authorizationService.authorize(calledMethod, getId(preAuth, joinPoint), getVersionId(preAuth, joinPoint),
                    getObject(preAuth, joinPoint), preAuth
                            .springSecurityExpression(), preAuth.permission(), preAuth.concat(), joinPoint.getArgs());
        }
        Object obj = joinPoint.proceed();
        if (postAuth != null) {
            authorizationService.authorize(calledMethod, null, null, obj, postAuth
                    .springSecurityExpression(), postAuth.permission(), postAuth.concat(), joinPoint.getArgs());
        }
        return obj;
    }

    /**
     * Get id from method-parameters
     * 
     * @param preAuth Annotation
     * @param joinPoint
     * @return String Id or null
     */
    private String getId(final PreAuth preAuth, final ProceedingJoinPoint joinPoint) {
        if (preAuth != null && !ObjectType.INPUT_ENTITY.equals(preAuth.permission().objectType()) &&
                preAuth.permission().idIndex() >= 0 && joinPoint != null &&
                joinPoint.getArgs() != null &&
                joinPoint.getArgs().length > preAuth.permission().idIndex() &&
                joinPoint.getArgs()[preAuth.permission().idIndex()] instanceof String) {
            return (String) joinPoint.getArgs()[preAuth.permission().idIndex()];
        }
        return null;
    }

    /**
     * Get version-Id from method-parameters
     * 
     * @param preAuth Annotation
     * @param joinPoint
     * @return Integer versionId or null
     */
    private Integer getVersionId(final PreAuth preAuth, final ProceedingJoinPoint joinPoint) {
        if (preAuth != null &&
                preAuth.permission().versionIndex() >= 0 && joinPoint != null &&
                joinPoint.getArgs() != null &&
                joinPoint.getArgs().length > preAuth.permission().versionIndex() &&
                joinPoint.getArgs()[preAuth.permission().versionIndex()] instanceof Integer) {
            return (Integer) joinPoint.getArgs()[preAuth.permission().versionIndex()];
        }
        return null;
    }

    /**
     * Get Entity-Object from method-parameters
     * 
     * @param preAuth Annotation
     * @param joinPoint
     * @return Entity or null
     */
    private Entity getObject(final PreAuth preAuth, final ProceedingJoinPoint joinPoint) throws IOException {
        if (preAuth != null && ObjectType.INPUT_ENTITY.equals(preAuth.permission().objectType()) &&
                preAuth.permission().idIndex() >= 0 && joinPoint != null &&
                joinPoint.getArgs() != null &&
                joinPoint.getArgs().length > preAuth.permission().idIndex() &&
                joinPoint.getArgs()[preAuth.permission().idIndex()] instanceof Entity) {
            return (Entity) joinPoint.getArgs()[preAuth.permission().idIndex()];
        }
        return null;
    }

    /**
     * See Interface for functional description.
     */
    @Override
    public int getOrder() {
        return 0;
    }

}
