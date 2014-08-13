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

import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.PublishService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Web controller responsible for creating browse views
 */
@Controller
@RequestMapping("/browse")
public class BrowseController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private PublishService publishService;

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing all stored
     * {@link net.objecthunter.larch.model.Entity}s
     * 
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}'s identifiers
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public SearchResult browse() throws IOException {
        return this.entityService.scanEntities(0);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing all stored
     * {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}'s identifiers
     * @throws IOException
     */
    @RequestMapping(value = "/{offset}", method = RequestMethod.GET)
    @ResponseBody
    public SearchResult browse(@PathVariable("offset") final int offset) throws IOException {
        return this.entityService.scanEntities(offset);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing a given number of
     * stored {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}'s identifiers
     * @throws IOException
     */
    @RequestMapping(value = "/{offset}/{numrecords}", method = RequestMethod.GET)
    @ResponseBody
    public SearchResult browse(@PathVariable("offset") final int offset,
            @PathVariable("numrecords") final int numRecords) throws IOException {
        return this.entityService.scanEntities(offset, numRecords);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s.
     * 
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView browseHtml() throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.browse());
        return new ModelAndView("browse", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/{offset}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView browseHtml(@PathVariable("offset") final int offset) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.browse(offset));
        return new ModelAndView("browse", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing a given number of
     * stored {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/{offset}/{numrecords}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView browseHtml(@PathVariable("offset") final int offset,
            @PathVariable("numrecords") final int numRecords) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.browse(offset, numRecords));
        return new ModelAndView("browse", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing all stored
     * {@link net.objecthunter.larch.model.Entity}s
     * 
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}'s identifiers
     * @throws IOException
     */
    @RequestMapping(value = "/published", method = RequestMethod.GET)
    @ResponseBody
    public SearchResult browsePublished() throws IOException {
        return this.publishService.scanIndex(0);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing all stored
     * {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}'s identifiers
     * @throws IOException
     */
    @RequestMapping(value = "/published/{offset}", method = RequestMethod.GET)
    @ResponseBody
    public SearchResult browsePublished(@PathVariable("offset") final int offset) throws IOException {
        return this.publishService.scanIndex(offset);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing a given number of
     * stored {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}'s identifiers
     * @throws IOException
     */
    @RequestMapping(value = "/published/{offset}/{numrecords}", method = RequestMethod.GET)
    @ResponseBody
    public SearchResult browsePublished(@PathVariable("offset") final int offset,
            @PathVariable("numrecords") final int numRecords) throws IOException {
        return this.publishService.scanIndex(offset, numRecords);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s.
     * 
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/published", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView browsePublishedHtml() throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.browsePublished());
        return new ModelAndView("browsepublished", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/published/{offset}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView browsePublishedHtml(@PathVariable("offset") final int offset) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.browsePublished(offset));
        return new ModelAndView("browsepublished", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing a given number of
     * stored {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/published/{offset}/{numrecords}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView browsePublishedHtml(@PathVariable("offset") final int offset,
            @PathVariable("numrecords") final int numRecords) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.browsePublished(offset, numRecords));
        return new ModelAndView("browsepublished", model);
    }
}
