/* 
 * Copyright 2014 FIZ Karlsruhe
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

package de.escidocng.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.escidocng.model.security.annotation.Permission;
import de.escidocng.model.security.annotation.PreAuth;
import de.escidocng.model.security.role.Role.RoleName;
import de.escidocng.model.state.EscidocngState;
import de.escidocng.service.RepositoryService;

/**
 * Web controller responsible for creating repository state views
 */
@Controller
@RequestMapping("/state")
public class StateController extends AbstractEscidocngController {

    @Autowired
    private RepositoryService repositoryService;

    /**
     * Controller method for retrieving a {@link de.escidocng.model.state.EscidocngState} object describing the
     * repository state using a HTTP GET, that returns a JSON representation
     * 
     * @return a JSON representation of the repository's {@link de.escidocng.model.state.EscidocngState}
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN) })
    public EscidocngState state() throws IOException {
        return repositoryService.status();
    }

}
