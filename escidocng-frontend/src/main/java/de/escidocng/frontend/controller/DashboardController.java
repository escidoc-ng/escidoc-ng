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
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.escidocng.frontend.util.HttpHelper;
import de.escidocng.model.Describe;

/**
 * Web controller class responsible for the dashboard overview of the escidocng repository
 */
@Controller
@RequestMapping("/")
public class DashboardController extends AbstractController {

    @Autowired
    private HttpHelper httpHelper;

    /**
     * Controller method for creating a HTML view using Spring MVC templating mechanism showing a dashboard
     * 
     * @return a Spring MVC {@link org.springframework.web.servlet.ModelAndView} used for rendering the HTML view.
     * @throws IOException
     */
    @RequestMapping(produces = "text/html")
    @ResponseBody
    public ModelAndView dashboardHtml() throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("describe", mapper.readValue(httpHelper.doGet("/describe"), Describe.class));
        return new ModelAndView("dashboard", model);
    }
}
