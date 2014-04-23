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
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*/
package net.objecthunter.larch.controller;

import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

/**
 * Web controller class responsible for the dashboard overview of the larch repository
 */
@Controller
@RequestMapping("/")
public class DashboardController {

    @Autowired
    private RepositoryService repositoryService;

    @RequestMapping(produces = "text/html")
    @ResponseBody
    public ModelAndView dashboardHtml() throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("describe", repositoryService.describe());
        return new ModelAndView("dashboard", model);
    }
}
