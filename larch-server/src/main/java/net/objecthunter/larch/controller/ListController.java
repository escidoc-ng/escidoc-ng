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

import net.objecthunter.larch.model.Entity.EntityType;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.service.EntityService;

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

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing all stored
     * {@link net.objecthunter.larch.model.Entity}s
     * 
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}s
     * @throws IOException
     */
    @RequestMapping(value = "/list/{entityType}", method = RequestMethod.GET, produces = { "application/json", "application/xml",
        "text/xml" })
    @ResponseBody
    public SearchResult listEntities(@PathVariable("entityType") final String entityType) throws IOException {
        return entityService.scanEntities(EntityType.valueOf(entityType.toUpperCase()), 0);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s.
     * 
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/list/{entityType}", method = RequestMethod.GET, produces = { "text/html" })
    @ResponseBody
    public ModelAndView listEntitiesHtml(@PathVariable("entityType") final String entityType) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listEntities(entityType));
        return new ModelAndView("list", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s.
     * 
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/browse/{entityType}", method = RequestMethod.GET, produces = { "text/html" })
    @ResponseBody
    public ModelAndView browseEntitiesHtml(@PathVariable("entityType") final String entityType) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listEntities(entityType));
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
    @RequestMapping(value = "/list/{entityType}/{offset}", method = RequestMethod.GET)
    @ResponseBody
    public SearchResult listEntities(@PathVariable("entityType") final String entityType, @PathVariable("offset") final int offset) throws IOException {
        return this.entityService.scanEntities(EntityType.valueOf(entityType.toUpperCase()), offset);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s from a given offset.
     * 
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/list/{entityType}/{offset}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView listEntitiesHtml(@PathVariable("entityType") final String entityType, @PathVariable("offset") final int offset) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listEntities(entityType, offset));
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
    @RequestMapping(value = "/browse/{entityType}/{offset}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView browseEntitiesHtml(@PathVariable("entityType") final String entityType, @PathVariable("offset") final int offset) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listEntities(entityType, offset));
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
    @RequestMapping(value = "/list/{entityType}/{offset}/{numrecords}", method = RequestMethod.GET)
    @ResponseBody
    public SearchResult listEntities(@PathVariable("entityType") final String entityType, @PathVariable("offset") final int offset,
            @PathVariable("numrecords") final int numRecords) throws IOException {
        return this.entityService.scanEntities(EntityType.valueOf(entityType.toUpperCase()), offset, numRecords);
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
    @RequestMapping(value = "/list/{entityType}/{offset}/{numrecords}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView listEntitiesHtml(@PathVariable("entityType") final String entityType, @PathVariable("offset") final int offset,
            @PathVariable("numrecords") final int numRecords) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listEntities(entityType, offset, numRecords));
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
    @RequestMapping(value = "/browse/{entityType}/{offset}/{numrecords}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody
    public ModelAndView browseEntitiesHtml(@PathVariable("entityType") final String entityType, @PathVariable("offset") final int offset,
            @PathVariable("numrecords") final int numRecords) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("result", this.listEntities(entityType, offset, numRecords));
        return new ModelAndView("browse", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing all stored
     * {@link net.objecthunter.larch.model.Entity}s in the given permission.
     * 
     * @param ancestorId workspaceId
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}s
     * @throws IOException
     */
    @RequestMapping(value = "/{id}/children/{entityType}/list", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SearchResult listChildEntities(@PathVariable("id") final String ancestorId, @PathVariable("entityType") final String entityType) throws IOException {
        return this.entityService.scanChildEntities(ancestorId, EntityType.valueOf(entityType.toUpperCase()), 0);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s in the given permission.
     * 
     * @param ancestorId workspaceId
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/{id}/children/{entityType}/list", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView listChildEntitiesHtml(@PathVariable("id") final String ancestorId, @PathVariable("entityType") final String entityType) throws IOException {
        final ModelMap model = new ModelMap("result", listChildEntities(ancestorId, entityType));
        return new ModelAndView("list", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s in the given workspace.
     * 
     * @param ancestorId workspaceId
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/{id}/children/{entityType}/browse", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView browseChildEntitiesHtml(@PathVariable("id") final String ancestorId, @PathVariable("entityType") final String entityType) throws IOException {
        final ModelMap model = new ModelMap("result", listChildEntities(ancestorId, entityType));
        return new ModelAndView("browse", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing all stored
     * {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param ancestorId workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}s
     * @throws IOException
     */
    @RequestMapping(value = "/{id}/children/{entityType}/list/{offset}", method = RequestMethod.GET,
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SearchResult
            listChildEntities(@PathVariable("id") final String ancestorId, @PathVariable("entityType") final String entityType, @PathVariable("offset") final int offset)
                    throws IOException {
        return this.entityService.scanChildEntities(ancestorId, EntityType.valueOf(entityType.toUpperCase()), offset);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param ancestorId workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/{id}/children/{entityType}/list/{offset}", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView listChildEntitiesHtml(@PathVariable("id") final String ancestorId, @PathVariable("entityType") final String entityType,
            @PathVariable("offset") final int offset)
            throws IOException {
        final ModelMap model = new ModelMap("result", listChildEntities(ancestorId, entityType, offset));
        return new ModelAndView("list", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing stored
     * {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param ancestorId workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/{id}/children/{entityType}/browse/{offset}", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView browseChildEntitiesHtml(@PathVariable("id") final String ancestorId, @PathVariable("entityType") final String entityType,
            @PathVariable("offset") final int offset)
            throws IOException {
        final ModelMap model = new ModelMap("result", listChildEntities(ancestorId, entityType, offset));
        return new ModelAndView("browse", model);
    }

    /**
     * Controller method for getting {@link net.objecthunter.larch.model.SearchResult} containing a given number of
     * stored {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param ancestorId workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A SearchResult containing {@link net.objecthunter.larch.model.Entity}s
     * @throws IOException
     */
    @RequestMapping(value = "/{id}/children/{entityType}/list/{offset}/{numRecords}", method = RequestMethod.GET,
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public SearchResult listChildEntities(@PathVariable("id") final String ancestorId, @PathVariable("entityType") final String entityType,
            @PathVariable("offset") final int offset,
            @PathVariable("numRecords") final int numRecords) throws IOException {
        return this.entityService.scanChildEntities(ancestorId, EntityType.valueOf(entityType.toUpperCase()), offset, numRecords);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing a given number of
     * stored {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param ancestorId workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the list result
     * @throws IOException
     */
    @RequestMapping(value = "/{id}/children/{entityType}/list/{offset}/{numRecords}", method = RequestMethod.GET,
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView listChildEntitiesHtml(@PathVariable("id") final String ancestorId, @PathVariable("entityType") final String entityType,
            @PathVariable("offset") final int offset,
            @PathVariable("numRecords") final int numRecords) throws IOException {
        final ModelMap model = new ModelMap("result", listChildEntities(ancestorId, entityType, offset, numRecords));
        return new ModelAndView("list", model);
    }

    /**
     * Controller method for getting a HTML View using Spring MVC templating mechanism containing a given number of
     * stored {@link net.objecthunter.larch.model.Entity}s from a given offset in the given workspace.
     * 
     * @param ancestorId workspaceId
     * @param offset The offset to use when creating the {@link net.objecthunter.larch.model.SearchResult}
     * @param numRecords The maximal number of records to return
     * @return A {@link org.springframework.web.servlet.ModelAndView} showing the browse result
     * @throws IOException
     */
    @RequestMapping(value = "/{id}/children/{entityType}/browse/{offset}/{numRecords}", method = RequestMethod.GET,
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView browseChildEntitiesHtml(@PathVariable("id") final String ancestorId, @PathVariable("entityType") final String entityType,
            @PathVariable("offset") final int offset,
            @PathVariable("numRecords") final int numRecords) throws IOException {
        final ModelMap model = new ModelMap("result", listChildEntities(ancestorId, entityType, offset, numRecords));
        return new ModelAndView("browse", model);
    }

}
