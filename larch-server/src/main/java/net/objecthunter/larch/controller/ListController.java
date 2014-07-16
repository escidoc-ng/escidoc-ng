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

import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.PublishService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Web controller for creating simple list views
 */
@Controller
@RequestMapping("/list")
public class ListController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private PublishService publishService;

    /**
     * Controller method for a retrieving a HTML view using a HTTP GET containing a list of
     * {@link net.objecthunter .larch.model.Entity}s
     * 
     * @return a Spring MVC {@link org.springframework.web.servlet.ModelAndView} for rendering the HTML view
     */
    @RequestMapping(method = RequestMethod.GET, produces = { "text/html" })
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ModelAndView listHtml() {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.list());
        return new ModelAndView("list", model);
    }

    /**
     * Controller method to receive a JSON representation using an HTTP GET of a
     * {@link net.objecthunter.larch.model .SearchResult} object
     * 
     * @return A SearchResult containing the Entities
     */
    @RequestMapping(method = RequestMethod.GET, produces = { "application/json", "application/xml", "text/xml" })
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public SearchResult list() {
        return entityService.scanIndex(0);
    }

    /**
     * Controller method for a retrieving a HTML view using a HTTP GET containing a list of
     * {@link net.objecthunter .larch.model.Entity}s
     * 
     * @return a Spring MVC {@link org.springframework.web.servlet.ModelAndView} for rendering the HTML view
     */
    @RequestMapping(value = "/published", method = RequestMethod.GET, produces = { "text/html" })
    @ResponseBody
    public ModelAndView listPublishedHtml() {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listPublished());
        return new ModelAndView("publishedlist", model);
    }

    /**
     * Controller method to receive a JSON representation using an HTTP GET of a
     * {@link net.objecthunter.larch.model .SearchResult} object
     * 
     * @return A SearchResult containing the Entities
     */
    @RequestMapping(value = "/published", method = RequestMethod.GET, produces = { "application/json",
        "application/xml", "text/xml" })
    @ResponseBody
    public SearchResult listPublished() {
        return publishService.scanIndex(0);
    }

}
