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

package de.escidocng.frontend.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.escidocng.frontend.util.HttpHelper;
import de.escidocng.model.state.EscidocngState;

/**
 * Web controller responsible for creating repository state views
 */
@Controller
@RequestMapping("/state")
public class StateController extends AbstractController {

    @Autowired
    private HttpHelper httpHelper;

    /**
     * Controller method for retrieving a {@link de.escidocng.model.state.EscidocngState} object describing the
     * repository state using a HTTP GET, that returns a HTML ciew
     * 
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView}
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView stateHtml() throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("state", mapper.readValue(httpHelper.doGet("/state"), EscidocngState.class));
        return new ModelAndView("state", model);
    }
}
