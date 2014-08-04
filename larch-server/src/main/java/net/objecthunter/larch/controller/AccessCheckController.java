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

package net.objecthunter.larch.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import net.objecthunter.larch.service.EntityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Web controller access-check-requests
 */
@Controller
@RequestMapping("/access")
public class AccessCheckController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    /**
     * Controller method for checking if a request is allowed to execute for the user.
     * 
     * @param request The request to check
     * @throws IOException
     */
    @RequestMapping(value = "/check")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public void checkAccess(HttpServletRequest request) throws IOException {

    }

}
