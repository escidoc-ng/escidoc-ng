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

package de.escidocng.measurement;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Interceptor used for measuring method-execution time.
 * <p/>
 * <p/>
 * This Interceptor is invoked every time a method in a configured class is called.
 *
 * @author Michael Hoppe
 */
@Component
@Aspect
public class DurationInterceptor implements Ordered {

    @Autowired
    private Environment env;
    
    private static OutputStreamWriter ostr;
    
    static {
        try {
            ostr = new OutputStreamWriter(new FileOutputStream("durations.csv", false), "UTF-8");
        } catch (UnsupportedEncodingException | FileNotFoundException e) {}
    }


    /**
     * Around advice to perform the measurement.
     * <p/>
     *
     * @param joinPoint The current {@link ProceedingJoinPoint}.
     * @throws Throwable Thrown in case of an error.
     * @return
     */
//    @Around("execution(* de.escidocng.controller.*.*(..)) " +
//            "|| execution(* de.escidocng.service.impl.*.*(..)) " +
//            "|| execution(* de.escidocng.service.backend.elasticsearch.*.*(..)) " +
//            "|| execution(* de.escidocng.service.backend.fs.*.*(..)) " +
//            "|| execution(* de.escidocng.service.backend.weedfs.*.*(..)) ")
    public Object measure(final ProceedingJoinPoint joinPoint) throws Throwable {
        final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        long time = System.nanoTime();

        Object obj = joinPoint.proceed();

        logDuration(methodSignature, System.nanoTime() - time);

        return obj;
    }

    /**
     * See Interface for functional description.
     */
    @Override
    public int getOrder() {
        return 0;
    }

    private void logDuration(MethodSignature methodSignature, long duration) {
        StringBuilder builder = new StringBuilder(methodSignature.getDeclaringTypeName());
        builder.append(".").append(methodSignature.getName());
        builder.append(";");
        builder.append(duration).append("\n");
        try {
            ostr.write(builder.toString());
            ostr.flush();
        } catch (Exception e) {}
    }

}
