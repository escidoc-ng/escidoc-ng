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

package net.objecthunter.larch.controller;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.objecthunter.larch.exceptions.InvalidParameterException;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.security.annotation.PostAuth;
import net.objecthunter.larch.model.security.annotation.PreAuth;
import net.objecthunter.larch.security.helpers.AuthorizeHttpServletRequest;
import net.objecthunter.larch.service.AuthorizationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web controller responsible for authorize-requests
 */
@Controller
public class AuthController extends AbstractLarchController {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private ObjectMapper mapper;

    private Map<Method, List<Matcher>> requestUriMatchers = new HashMap<Method, List<Matcher>>();

    /**
     * Controller method for checking if user may call request. Just send Request you want to check with prefixed url.
     * Prefix is /is-authorized HttpServletRequest is wrapped in AuthorizeHttpServletRequest.
     * AuthorizeHttpServletRequest cuts off /is-authorized to be able to determine called Method
     * AuthorizeHttpServletRequest removes accept-header text/html --> All -html-Methods in the controller must call
     * non-html-method which is annotated with PreAuth or PostAuth.
     * 
     * @param username The name of the user
     */
    @RequestMapping(value = "/authorize/**")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void isAuthorized(HttpServletRequest request) throws IOException {
        AuthorizeHttpServletRequest req = new AuthorizeHttpServletRequest(request, "/authorize");
        try {
            // Get Method
            HandlerExecutionChain chain = this.handlerMapping.getHandler(req);
            if (chain != null) {
                HandlerMethod handlerMethod = (HandlerMethod) chain.getHandler();
                Method method = null;
                if (handlerMethod != null) {
                    method = handlerMethod.getMethod();
                }
                if (method == null) {
                    throw new InvalidParameterException("Method not found for given url");
                }

                List<Object> methodArgs = new ArrayList<Object>();
                Object[] methodArgsArr = null;
                // special Handling for create-entity
                if (method.getDeclaringClass().equals(EntityController.class) &&
                        method.getName().equals("create") && method.getParameters() != null &&
                        method.getParameters().length == 1 &&
                        method.getParameters()[0].getType().equals(InputStream.class)) {
                    method = EntityController.class.getMethod("create", Entity.class);
                    try {
                        Entity e = mapper.readValue(req.getInputStream(), Entity.class);
                        e.setState(EntityState.PENDING);
                        methodArgs.add(e);
                    } catch (Exception e) {
                        throw new InvalidParameterException("no entity found in input-stream");
                    }
                } else {
                    // Get Method-Arguments from called URL
                    List<Matcher> requestUriMatchers = getRequestUriMatchers(method);
                    for (Matcher mat : requestUriMatchers) {
                        if (mat.reset(req.getRequestURI()).matches()) {
                            for (int i = 1; i <= mat.groupCount(); i++) {
                                methodArgs.add(mat.group(i));
                            }
                            break;
                        }
                    }
                }
                methodArgsArr = methodArgs.toArray();

                // Call Authorization-Service
                if (method != null) {
                    PreAuth preAuth = method.getAnnotation(PreAuth.class);
                    PostAuth postAuth = method.getAnnotation(PostAuth.class);
                    if (preAuth != null) {
                        authorizationService
                                .authorize(method, preAuth.objectType(), authorizationService
                                        .getId(preAuth.idIndex(), preAuth
                                                .objectType(),
                                                methodArgsArr), authorizationService
                                        .getVersionId(preAuth.versionIndex(), methodArgsArr),
                                        authorizationService
                                                .getObject(preAuth.idIndex(), preAuth.objectType(), methodArgsArr),
                                        preAuth
                                                .permissions());
                    }
                    if (postAuth != null) {
                        authorizationService.authorize(method, postAuth.objectType(), authorizationService
                                .getId(postAuth
                                        .idIndex(), postAuth.objectType(),
                                        methodArgsArr), authorizationService
                                .getVersionId(postAuth.versionIndex(), methodArgsArr),
                                authorizationService
                                        .getObject(postAuth.idIndex(), postAuth.objectType(), methodArgsArr),
                                postAuth
                                        .permissions());
                    }
                }
            }
        } catch (IOException | AccessDeniedException | InsufficientAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Retrieve all request-uris for the given method. Read RequestMapping-Annotations.
     * 
     * @param method
     * @return
     */
    private List<Matcher> getRequestUriMatchers(Method method) {
        if (!requestUriMatchers.containsKey(method)) {
            List<Matcher> methodRequestUriMatchers = new ArrayList<Matcher>();
            RequestMapping classRequestMapping = method.getDeclaringClass().getAnnotation(RequestMapping.class);
            RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
            if (classRequestMapping != null && classRequestMapping.value() != null &&
                    classRequestMapping.value().length > 0) {
                for (String classRequestMappingString : classRequestMapping.value()) {
                    if (methodRequestMapping != null && methodRequestMapping.value() != null &&
                            methodRequestMapping.value().length > 0) {
                        for (String methodRequestMappingString : methodRequestMapping.value()) {
                            methodRequestUriMatchers.add(Pattern.compile(
                                    (classRequestMappingString + methodRequestMappingString).replaceAll("\\{.*?\\}",
                                            "([^\\/]*?)")).matcher(""));
                        }
                    } else {
                        methodRequestUriMatchers.add(Pattern.compile(
                                classRequestMappingString.replaceAll("\\{.*?\\}", "([^\\/]*?)")).matcher(""));
                    }
                }
            } else if (methodRequestMapping != null && methodRequestMapping.value() != null &&
                    methodRequestMapping.value().length > 0) {
                for (String methodRequestMappingString : methodRequestMapping.value()) {
                    methodRequestUriMatchers.add(Pattern.compile(
                            methodRequestMappingString.replaceAll("\\{.*?\\}", "([^\\/]*?)")).matcher(""));
                }
            }
            requestUriMatchers.put(method, methodRequestUriMatchers);
        }
        return requestUriMatchers.get(method);
    }

}