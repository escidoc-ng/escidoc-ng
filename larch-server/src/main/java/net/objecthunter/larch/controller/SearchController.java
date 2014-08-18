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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.PublishService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Web controller responsible for search views
 */
@Controller
@RequestMapping("/search")
public class SearchController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private PublishService publishService;

    /**
     * Controller method for searching {@link net.objecthunter.larch.model.Entity}s in the repository using an HTTP
     * POST which returns a JSON representation of the {@link net.objecthunter.larch.model.SearchResult}
     * 
     * @param query The search query
     * @return A {@link net.objecthunter.larch.model.SearchResult} containing the found
     *         {@link net.objecthunter.larch .model.Entity}s as s JSON representation
     */
    @RequestMapping(method = RequestMethod.POST, produces = { "application/json" })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public SearchResult searchMatchFields(final HttpServletRequest request) throws IOException {
        return entityService.searchEntities(fillSearchFields(request));
    }

    /**
     * Controller method for searching {@link net.objecthunter.larch.model.Entity}s in the publish repository using an
     * HTTP POST which returns a JSON representation of the {@link net.objecthunter.larch.model.SearchResult}
     * 
     * @param query The search query
     * @return A {@link net.objecthunter.larch.model.SearchResult} containing the found
     *         {@link net.objecthunter.larch .model.Entity}s as s JSON representation
     */
    @RequestMapping(value = "/published", method = RequestMethod.POST, produces = { "application/json" })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public SearchResult searchPublishedMatchFields(final HttpServletRequest request) throws IOException {
        return publishService.searchEntities(fillSearchFields(request));
    }

    /**
     * Controller method for displaying a HTML search page
     * 
     * @return a Spring MVC {@link org.springframework.web.servlet.ModelAndView} used to render the HTML view
     */
    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView searchHtml() {
        final ModelMap model = new ModelMap();
        return new ModelAndView("search", model);
    }

    /**
     * Controller method for searching {@link net.objecthunter.larch.model.Entity}s in the repository using an HTTP
     * POST which returns a HTML view of the {@link net.objecthunter.larch.model.SearchResult}
     * 
     * @param query The search query
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView} used to render the HTML view
     */
    @RequestMapping(method = RequestMethod.POST, produces = { "text/html" })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView searchMatchFieldsHtml(final HttpServletRequest request) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", searchMatchFields(request));
        return new ModelAndView("searchresult", model);
    }

    /**
     * Controller method for searching {@link net.objecthunter.larch.model.Entity}s in the repository using an HTTP
     * POST which returns a HTML view of the {@link net.objecthunter.larch.model.SearchResult}
     * 
     * @param query The search query
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView} used to render the HTML view
     */
    @RequestMapping(value = "/published", method = RequestMethod.POST, produces = { "text/html" })
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView searchPublishedMatchFieldsHtml(final HttpServletRequest request) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", searchPublishedMatchFields(request));
        return new ModelAndView("searchresultpublished", model);
    }

    /**
     * Fill all Parameters that are search-fields into Map.
     * 
     * @param request HttpServletRequest
     * @return Map<EntitiesSearchField, String[]> key: searchField, value: searchStrings (Words)
     */
    private Map<EntitiesSearchField, String[]> fillSearchFields(HttpServletRequest request) {
        Map<EntitiesSearchField, String[]> queryMap = new HashMap<EntitiesSearchField, String[]>();
        Map<String, String[]> parameters = request.getParameterMap();
        for (Entry<String, String[]> parameter : parameters.entrySet()) {
            EntitiesSearchField entitiesSearchField = EntitiesSearchField.getWithRequestParameter(parameter.getKey());
            if (entitiesSearchField != null) {
                List<String> values = new ArrayList<String>();
                if (parameter.getValue() != null && parameter.getValue().length > 0) {
                    for (int i = 0; i < parameter.getValue().length; i++) {
                        if (StringUtils.isNotBlank(parameter.getValue()[i])) {
                            String[] words = parameter.getValue()[i].split("\\s");
                            for (int j = 0; j < words.length; j++) {
                                if (StringUtils.isNotBlank(words[j])) {
                                    values.add(words[j]);
                                }
                            }
                        }
                    }
                }
                if (!values.isEmpty()) {
                    queryMap.put(entitiesSearchField, values.toArray(new String[values.size()]));
                }
            }
        }
        return queryMap;
    }
}
