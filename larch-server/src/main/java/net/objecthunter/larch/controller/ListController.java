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

import net.objecthunter.larch.annotations.PreAuth;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.PublishService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Web controller for creating simple list views
 */
@Controller
public class ListController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private PublishService publishService;

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing all stored
     * {@link net.objecthunter.larch.model.Entity}s
     * 
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}s
     * @throws IOException
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = { "application/json", "application/xml",
        "text/xml" })
    @ResponseBody
    public SearchResult listEntities() throws IOException {
        return entityService.scanEntities(0);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s.
     * 
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = { "text/html" })
    @ResponseBody
    public ModelAndView listEntitiesHtml() throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listEntities());
        return new ModelAndView("list", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s.
     * 
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/browse", method = RequestMethod.GET, produces = { "text/html" })
    @ResponseBody
    public ModelAndView browseEntitiesHtml() throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listEntities());
        return new ModelAndView("browse", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing all stored
     * {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}s
     * @throws IOException
     */
    @RequestMapping(value = "/list/{offset}", method = RequestMethod.GET)
    @ResponseBody
    public SearchResult listEntities(@PathVariable("offset") final int offset) throws IOException {
        return this.entityService.scanEntities(offset);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/list/{offset}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView listEntitiesHtml(@PathVariable("offset") final int offset) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listEntities(offset));
        return new ModelAndView("list", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/browse/{offset}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView browseEntitiesHtml(@PathVariable("offset") final int offset) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listEntities(offset));
        return new ModelAndView("browse", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing a given number of
     * stored {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}s
     * @throws IOException
     */
    @RequestMapping(value = "/list/{offset}/{numrecords}", method = RequestMethod.GET)
    @ResponseBody
    public SearchResult listEntities(@PathVariable("offset") final int offset,
            @PathVariable("numrecords") final int numRecords) throws IOException {
        return this.entityService.scanEntities(offset, numRecords);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing a given number of
     * stored {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/list/{offset}/{numrecords}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView listEntitiesHtml(@PathVariable("offset") final int offset,
            @PathVariable("numrecords") final int numRecords) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listEntities(offset, numRecords));
        return new ModelAndView("list", model);
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
    @RequestMapping(value = "/browse/{offset}/{numrecords}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView browseEntitiesHtml(@PathVariable("offset") final int offset,
            @PathVariable("numrecords") final int numRecords) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listEntities(offset, numRecords));
        return new ModelAndView("browse", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing all published
     * {@link net.objecthunter.larch.model.Entity}s
     * 
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}s
     * @throws IOException
     */
    @RequestMapping(value = "/list/published", method = RequestMethod.GET, produces = { "application/json",
        "application/xml", "text/xml" })
    @ResponseBody
    public SearchResult listPublishedEntities() throws IOException {
        return publishService.scanEntities(0);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing published
     * {@link net.objecthunter.larch.model.Entity}s.
     * 
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/list/published", method = RequestMethod.GET, produces = { "text/html" })
    @ResponseBody
    public ModelAndView listPublishedEntitiesHtml() throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listPublishedEntities());
        return new ModelAndView("publishedlist", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing published
     * {@link net.objecthunter.larch.model.Entity}s.
     * 
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/browse/published", method = RequestMethod.GET, produces = { "text/html" })
    @ResponseBody
    public ModelAndView browsePublishedEntitiesHtml() throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listPublishedEntities());
        return new ModelAndView("browsepublished", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing all published
     * {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}s
     * @throws IOException
     */
    @RequestMapping(value = "/list/published/{offset}", method = RequestMethod.GET)
    @ResponseBody
    public SearchResult listPublishedEntities(@PathVariable("offset") final int offset) throws IOException {
        return this.publishService.scanEntities(offset);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing published
     * {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/list/published/{offset}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView listPublishedEntitiesHtml(@PathVariable("offset") final int offset) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listPublishedEntities(offset));
        return new ModelAndView("publishedlist", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing published
     * {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/browse/published/{offset}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView browsePublishedEntitiesHtml(@PathVariable("offset") final int offset) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listPublishedEntities(offset));
        return new ModelAndView("browsepublished", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing a given number of
     * published {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}s
     * @throws IOException
     */
    @RequestMapping(value = "/list/published/{offset}/{numrecords}", method = RequestMethod.GET)
    @ResponseBody
    public SearchResult listPublishedEntities(@PathVariable("offset") final int offset,
            @PathVariable("numrecords") final int numRecords) throws IOException {
        return this.publishService.scanEntities(offset, numRecords);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing a given number of
     * published {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/list/published/{offset}/{numrecords}", method = RequestMethod.GET,
            produces = "text/html")
    @ResponseBody
    public ModelAndView listPublishedEntitiesHtml(@PathVariable("offset") final int offset,
            @PathVariable("numrecords") final int numRecords) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listPublishedEntities(offset, numRecords));
        return new ModelAndView("publishedlist", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing a given number of
     * published {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/browse/published/{offset}/{numrecords}", method = RequestMethod.GET,
            produces = "text/html")
    @ResponseBody
    public ModelAndView browsePublishedEntitiesHtml(@PathVariable("offset") final int offset,
            @PathVariable("numrecords") final int numRecords) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listPublishedEntities(offset, numRecords));
        return new ModelAndView("browsepublished", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing all stored
     * {@link net.objecthunter.larch.model.Entity}s in the given workspace.
     * 
     * @param id workspaceId
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}s
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/list", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SearchResult listWorkspaceEntities(@PathVariable("id") final String id) throws IOException {
        return this.entityService.scanWorkspaceEntities(id, 0);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s in the given workspace.
     * 
     * @param id workspaceId
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/list", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView listWorkspaceEntitiesHtml(@PathVariable("id") final String id) throws IOException {
        final ModelMap model = new ModelMap("result", listWorkspaceEntities(id));
        return new ModelAndView("list", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s in the given workspace.
     * 
     * @param id workspaceId
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/browse", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView browseWorkspaceEntitiesHtml(@PathVariable("id") final String id) throws IOException {
        final ModelMap model = new ModelMap("result", listWorkspaceEntities(id));
        return new ModelAndView("browse", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing all stored
     * {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param id workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}s
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/list/{offset}", method = RequestMethod.GET,
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SearchResult
            listWorkspaceEntities(@PathVariable("id") final String id, @PathVariable("offset") final int offset)
                    throws IOException {
        return this.entityService.scanWorkspaceEntities(id, offset);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param id workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/list/{offset}", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView listWorkspaceEntitiesHtml(@PathVariable("id") final String id,
            @PathVariable("offset") final int offset)
            throws IOException {
        final ModelMap model = new ModelMap("result", listWorkspaceEntities(id, offset));
        return new ModelAndView("list", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param id workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/browse/{offset}", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView browseWorkspaceEntitiesHtml(@PathVariable("id") final String id,
            @PathVariable("offset") final int offset)
            throws IOException {
        final ModelMap model = new ModelMap("result", listWorkspaceEntities(id, offset));
        return new ModelAndView("browse", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing a given number of
     * stored {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param id workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}s
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/list/{offset}/{numRecords}", method = RequestMethod.GET,
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SearchResult listWorkspaceEntities(@PathVariable("id") final String id,
            @PathVariable("offset") final int offset,
            @PathVariable("numRecords") final int numRecords) throws IOException {
        return this.entityService.scanWorkspaceEntities(id, offset, numRecords);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing a given number of
     * stored {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param id workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/list/{offset}/{numRecords}", method = RequestMethod.GET,
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView listWorkspaceEntitiesHtml(@PathVariable("id") final String id,
            @PathVariable("offset") final int offset,
            @PathVariable("numRecords") final int numRecords) throws IOException {
        final ModelMap model = new ModelMap("result", listWorkspaceEntities(id, offset, numRecords));
        return new ModelAndView("list", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing a given number of
     * stored {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param id workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/browse/{offset}/{numRecords}", method = RequestMethod.GET,
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView browseWorkspaceEntitiesHtml(@PathVariable("id") final String id,
            @PathVariable("offset") final int offset,
            @PathVariable("numRecords") final int numRecords) throws IOException {
        final ModelMap model = new ModelMap("result", listWorkspaceEntities(id, offset, numRecords));
        return new ModelAndView("browse", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing all stored
     * {@link net.objecthunter.larch.model.Entity}s in the given workspace.
     * 
     * @param id workspaceId
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}s
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/list/published", method = RequestMethod.GET,
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SearchResult listPublishedWorkspaceEntities(@PathVariable("id") final String id) throws IOException {
        return this.publishService.scanWorkspaceEntities(id, 0);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s in the given workspace.
     * 
     * @param id workspaceId
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/list/published", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView listPublishedWorkspaceEntitiesHtml(@PathVariable("id") final String id) throws IOException {
        final ModelMap model = new ModelMap("result", listPublishedWorkspaceEntities(id));
        return new ModelAndView("list", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s in the given workspace.
     * 
     * @param id workspaceId
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/browse/published", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView browsePublishedWorkspaceEntitiesHtml(@PathVariable("id") final String id) throws IOException {
        final ModelMap model = new ModelMap("result", listPublishedWorkspaceEntities(id));
        return new ModelAndView("browse", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing all stored
     * {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param id workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}s
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/list/published/{offset}", method = RequestMethod.GET,
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SearchResult
            listPublishedWorkspaceEntities(@PathVariable("id") final String id,
                    @PathVariable("offset") final int offset)
                    throws IOException {
        return this.publishService.scanWorkspaceEntities(id, offset);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param id workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/list/published/{offset}", method = RequestMethod.GET,
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView listPublishedWorkspaceEntitiesHtml(@PathVariable("id") final String id,
            @PathVariable("offset") final int offset)
            throws IOException {
        final ModelMap model = new ModelMap("result", listPublishedWorkspaceEntities(id, offset));
        return new ModelAndView("list", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param id workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/browse/published/{offset}", method = RequestMethod.GET,
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView browsePublishedWorkspaceEntitiesHtml(@PathVariable("id") final String id,
            @PathVariable("offset") final int offset)
            throws IOException {
        final ModelMap model = new ModelMap("result", listPublishedWorkspaceEntities(id, offset));
        return new ModelAndView("browse", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing a given number of
     * stored {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param id workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}s
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/list/published/{offset}/{numRecords}", method = RequestMethod.GET,
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SearchResult listPublishedWorkspaceEntities(@PathVariable("id") final String id,
            @PathVariable("offset") final int offset,
            @PathVariable("numRecords") final int numRecords) throws IOException {
        return this.publishService.scanWorkspaceEntities(id, offset, numRecords);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing a given number of
     * stored {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param id workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/list/published/{offset}/{numRecords}", method = RequestMethod.GET,
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView listPublishedWorkspaceEntitiesHtml(@PathVariable("id") final String id,
            @PathVariable("offset") final int offset,
            @PathVariable("numRecords") final int numRecords) throws IOException {
        final ModelMap model = new ModelMap("result", listPublishedWorkspaceEntities(id, offset, numRecords));
        return new ModelAndView("list", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing a given number of
     * stored {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param id workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{id}/browse/published/{offset}/{numRecords}", method = RequestMethod.GET,
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView browsePublishedWorkspaceEntitiesHtml(@PathVariable("id") final String id,
            @PathVariable("offset") final int offset,
            @PathVariable("numRecords") final int numRecords) throws IOException {
        final ModelMap model = new ModelMap("result", listPublishedWorkspaceEntities(id, offset, numRecords));
        return new ModelAndView("browse", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing all stored
     * {@link net.objecthunter.larch.model.Workspace}s
     * 
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Workspace}s
     * @throws IOException
     */
    @RequestMapping(value = "/workspace-list", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(springSecurityExpression = "!isAnonymous()")
    public SearchResult listWorkspaces() throws IOException {
        return this.entityService.scanWorkspaces(0);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Workspace}s.
     * 
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/workspace-list", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(springSecurityExpression = "!isAnonymous()")
    public ModelAndView listWorkspacesHtml() throws IOException {
        final ModelMap model = new ModelMap("result", this.listWorkspaces());
        return new ModelAndView("workspaces", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing all stored
     * {@link net.objecthunter.larch.model.Workspace}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Workspace}s
     * @throws IOException
     */
    @RequestMapping(value = "/workspace-list/{offset}", method = RequestMethod.GET,
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(springSecurityExpression = "!isAnonymous()")
    public SearchResult listWorkspaces(@PathVariable("offset") final int offset) throws IOException {
        return this.entityService.scanWorkspaces(offset);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Workspace}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/workspace-list/{offset}", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(springSecurityExpression = "!isAnonymous()")
    public ModelAndView listWorkspacesHtml(@PathVariable("offset") final int offset) throws IOException {
        final ModelMap model = new ModelMap("result", this.listWorkspaces(offset));
        return new ModelAndView("workspaces", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing a given number of
     * stored {@link net.objecthunter.larch.model.Workspace}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Workspace}s
     * @throws IOException
     */
    @RequestMapping(value = "/workspace-list/{offset}/{numRecords}", method = RequestMethod.GET,
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(springSecurityExpression = "!isAnonymous()")
    public SearchResult listWorkspaces(@PathVariable("offset") final int offset,
            @PathVariable("numRecords") final int numRecords) throws IOException {
        return this.entityService.scanWorkspaces(offset, numRecords);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing a given number of
     * stored {@link net.objecthunter.larch.model.Workspace}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/workspace-list/{offset}/{numRecords}", method = RequestMethod.GET,
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(springSecurityExpression = "!isAnonymous()")
    public ModelAndView listWorkspacesHtml(@PathVariable("offset") final int offset,
            @PathVariable("numRecords") final int numRecords) throws IOException {
        final ModelMap model = new ModelMap("result", this.listWorkspaces(offset, numRecords));
        return new ModelAndView("workspaces", model);
    }

}
