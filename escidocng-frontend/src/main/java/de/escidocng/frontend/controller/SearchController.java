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
import java.net.URLEncoder;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import de.escidocng.frontend.util.HttpHelper;
import de.escidocng.model.Archive;
import de.escidocng.model.SearchResult;

/**
 * Web controller responsible for search views
 */
@Controller
@RequestMapping("/search")
public class SearchController extends AbstractController {

    @Autowired
    private HttpHelper httpHelper;

    /**
     * Controller method for displaying a HTML search form
     * 
     * @return a Spring MVC {@link org.springframework.web.servlet.ModelAndView} used to render the HTML view
     */
    @RequestMapping(method = RequestMethod.GET, value = "/entities/form", produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView searchHtml() {
        final ModelMap model = new ModelMap();
        return new ModelAndView("entitysearch", model);
    }

    /**
     * Controller method for searching {@link de.escidocng.model.Entity}s in the repository using an HTTP
     * GET which returns a JSON representation of the {@link de.escidocng.model.SearchResult}. The request
     * can contain parameters key:searchfield, value:searchvalue and 2 special parameters offset and maxRecords.<br>
     * offset: hit-number to start searchresult-list with.<br>
     * maxRecords: maximum number of records to return with searchresult-list.<br>
     * values of different Search-Parameter-Names are concatenated with AND,<br>
     * values of same search-parameter-names are concatenated with OR.<br>
     * 
     * @param request The request with all parameters.
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView} used to render the HTML view
     */
    @RequestMapping(method = RequestMethod.GET, value = "/entities", produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView searchMatchFieldsHtml(@RequestParam(
            value = "query", defaultValue = "*:*") final String query, @RequestParam(
            value = "offset", defaultValue = "0") final int offset, @RequestParam(
            value = "maxRecords", defaultValue = "50") final int maxRecords) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", mapper.readValue(httpHelper.doGet("/search/entities?query=" + URLEncoder.encode(query, "UTF-8") + "&offset=" +
                offset + "&maxRecords=" + maxRecords), SearchResult.class));
        return new ModelAndView("entitysearchresult", model);
    }

    /**
     * Controller method for displaying a HTML usersearch form
     * 
     * @return a Spring MVC {@link org.springframework.web.servlet.ModelAndView} used to render the HTML view
     */
    @RequestMapping(method = RequestMethod.GET, value = "/users/form", produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView searchUsersHtml() {
        final ModelMap model = new ModelMap();
        return new ModelAndView("usersearch", model);
    }

    /**
     * Controller method for searching {@link de.escidocng.model.security.User}s in the repository using an
     * HTTP GET which returns a JSON representation of the {@link de.escidocng.model.SearchResult}. The
     * request can contain a parameter query and 2 parameters offset and maxRecords.<br>
     * offset: hit-number to start searchresult-list with.<br>
     * maxRecords: maximum number of records to return with searchresult-list.<br>
     * 
     * @param request The request with all parameters.
     * @return A {@link de.escidocng.model.SearchResult} containing the found
     *         {@link de.escidocng.model.security.User}s as s JSON representation
     */
    @RequestMapping(method = RequestMethod.GET, value = "/users", produces = { "application/json" })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView searchUsers(@RequestParam(
            value = "query", defaultValue = "*:*") final String query, @RequestParam(
            value = "offset", defaultValue = "0") final int offset, @RequestParam(
            value = "maxRecords", defaultValue = "50") final int maxRecords) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", mapper.readValue(httpHelper.doGet("/search/users?query=" + URLEncoder.encode(query, "UTF-8") + "&offset=" +
                offset + "&maxRecords=" + maxRecords), SearchResult.class));
        return new ModelAndView("usersearchresult", model);
    }

    /**
     * Controller method for displaying a HTML archivesearch form
     * 
     * @return a Spring MVC {@link org.springframework.web.servlet.ModelAndView} used to render the HTML view
     */
    @RequestMapping(method = RequestMethod.GET, value = "/archives/form", produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView searchArchivesHtml() {
        final ModelMap model = new ModelMap();
        return new ModelAndView("archivesearch", model);
    }

    /**
     * Controller method for searching {@link de.escidocng.model.security.Archive}s in the repository using an
     * HTTP GET which returns a JSON representation of the {@link de.escidocng.model.SearchResult}. The
     * request can contain a parameter query and 2 parameters offset and maxRecords.<br>
     * offset: hit-number to start searchresult-list with.<br>
     * maxRecords: maximum number of records to return with searchresult-list.<br>
     * 
     * @param request The request with all parameters.
     * @return A {@link de.escidocng.model.SearchResult} containing the found
     *         {@link de.escidocng.model.Archive}s as s JSON representation
     */
    @RequestMapping(method = RequestMethod.GET, value="/archives")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView searchArchives(@RequestParam(
            value = "query", defaultValue = "*:*") final String query, @RequestParam(
            value = "offset", defaultValue = "0") final int offset, @RequestParam(
            value = "maxRecords", defaultValue = "50") final int maxRecords)
            throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", mapper.readValue(httpHelper.doGet("/search/archives?query=" + URLEncoder.encode(query, "UTF-8") + "&offset=" +
                offset + "&maxRecords=" + maxRecords), SearchResult.class));
        return new ModelAndView("archivesearchresult", model);
    }

}
