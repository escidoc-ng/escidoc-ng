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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Web controller for error
 */
@Controller
public class ErrorController extends AbstractLarchController {

    /**
     * Controller method for error
     * 
     * @return String forward to error-endpoint
     */
    @RequestMapping(value = "/error-page")
    public String error(HttpServletRequest request) {
        if (request != null) {
            if (StringUtils.isNotBlank(request.getParameter("path"))) {
                request.setAttribute("javax.servlet.error.request_uri", request.getParameter("path"));
            }
            if (StringUtils.isNotBlank(request.getParameter("message"))) {
                request.setAttribute("javax.servlet.error.message", request.getParameter("message"));
            }
            if (StringUtils.isNotBlank(request.getParameter("status"))) {
                try {
                    request.setAttribute("javax.servlet.error.status_code", Integer.parseInt(request
                            .getParameter("status")));
                } catch (NumberFormatException e) {
                    request.setAttribute("javax.servlet.error.status_code", 500);
                }
            }
        }
        return "forward:/error";
    }

}
