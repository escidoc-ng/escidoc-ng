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

package net.objecthunter.larch.frontend.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import net.objecthunter.larch.frontend.util.HttpHelper;

import org.apache.http.entity.InputStreamEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Web controller responsible for authorize-requests
 */
@Controller
public class AuthController extends AbstractController {

    @Autowired
    private HttpHelper httpHelper;

    /**
     * Controller method for checking if user may call request. Just send Request you want to check with prefixed url.
     * Prefix is /is-authorized HttpServletRequest is wrapped in AuthorizeHttpServletRequest.
     * AuthorizeHttpServletRequest cuts off /is-authorized to be able to determine called Method
     * AuthorizeHttpServletRequest removes accept-header text/html --> All -html-Methods in the controller must call
     * non-html-method which is annotated with PreAuth or PostAuth.
     * 
     * @param request HttpServletRequest
     */
    @RequestMapping(value = "/authorize/**")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void isAuthorized(HttpServletRequest request) throws IOException {
        if (request.getMethod().equals("GET")) {
            StringBuilder url = new StringBuilder(request.getRequestURI());
            if (request.getQueryString() != null) {
                url.append("?").append(request.getQueryString());
            }
            httpHelper.doGet(request.getRequestURI() + "?" + request.getQueryString());
        } else if (request.getMethod().equals("PUT")) {
            httpHelper.doPut(request.getRequestURI(), new InputStreamEntity(request.getInputStream(), -1), "application/json");
        } else if (request.getMethod().equals("POST")) {
            httpHelper.doPost(request.getRequestURI(), new InputStreamEntity(request.getInputStream(), -1), "application/json");
        } else if (request.getMethod().equals("DELETE")) {
            httpHelper.doDelete(request.getRequestURI());
        } else if (request.getMethod().equals("PATCH")) {
            httpHelper.doPatch(request.getRequestURI(), new InputStreamEntity(request.getInputStream(), -1), "application/json");
        }
    }

}