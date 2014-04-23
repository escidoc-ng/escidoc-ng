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

import net.objecthunter.larch.model.Describe;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

/**
 * Web controller class responsible for creating {@link net.objecthunter.larch.model.Describe} views
 */
@Controller
@RequestMapping("/describe")
@Component
public class DescribeController extends AbstractLarchController {

    @Autowired
    private RepositoryService repositoryService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Describe describe() throws IOException {
        return repositoryService.describe();
    }

    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView describeHtml() throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("describe", repositoryService.describe());
        return new ModelAndView("describe",model);
    }
}
