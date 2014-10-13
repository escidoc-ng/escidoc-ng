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
 * WITHOUT WARRANTIES OR CONDITIONS OF ROLE_ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package net.objecthunter.larch.frontend.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.objecthunter.larch.frontend.util.HttpHelper;
import net.objecthunter.larch.model.AlternativeIdentifier;
import net.objecthunter.larch.model.Entities;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.MetadataType;

import org.apache.http.entity.InputStreamEntity;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web controller responsible for interactions on the entity level
 */
@Controller
@RequestMapping("/entity")
public class EntityController extends AbstractController {

    @Autowired
    private HttpHelper httpHelper;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Controller method for retrieval of a HTML view of the current version of an
     * {@link net.objecthunter.larch.model.Entity}
     * 
     * @param id The is of the {@link net.objecthunter.larch.model.Entity} to retrieve
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView} for rendering the HTML view
     * @throws IOException
     */
    @RequestMapping(value = "/{id}", produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView retrieveHtml(@PathVariable("id") final String id) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("entity", mapper.readValue(httpHelper.doGet("/entity/" + id), Entity.class));
        model.addAttribute("metadataTypes", mapper.readValue(httpHelper.doGet("/metadatatype"), new TypeReference<List<MetadataType>>() {}));
        model.addAttribute("identifierTypes", AlternativeIdentifier.IdentifierType.values());
        return new ModelAndView("entity", model);
    }

    /**
     * Controller method for retrieval of a HTML view of a given version of an
     * {@link net.objecthunter.larch.model.Entity}
     * 
     * @param id the {@link net.objecthunter.larch.model.Entity}'s id
     * @param version the version number of the Entity version to retrieve
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView} for rendering the HTML view
     * @throws IOException
     */
    @RequestMapping(value = "/{id}/version/{version}", produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView retrieveHtml(@PathVariable("id") final String id, @PathVariable("version") final int version)
            throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("entity", mapper.readValue(httpHelper.doGet("/entity/" + id + "/version/" + version), Entity.class));
        return new ModelAndView("entity", model);
    }

    /**
     * Controller method for retrieval of a HTML view of all versions of an
     * {@link net.objecthunter.larch.model.Entity}
     * 
     * @param id the {@link net.objecthunter.larch.model.Entity}'s id
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView} for rendering the HTML view
     * @throws IOException
     */
    @RequestMapping(value = "/{id}/versions", produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView retrieveVersionsHtml(@PathVariable("id") final String id) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("entities", mapper.readValue(httpHelper.doGet("/entity/" + id + "/versions"), Entities.class));
        return new ModelAndView("versions", model);
    }

    /**
     * Controller method for creation of a new {@link net.objecthunter.larch.model.Entity} using a HTTP POST with the
     * JSON representation of the entity as the request body
     * 
     * @param src The Stream injected by Spring MVC containing the JSON representation of the Entity to create.
     * @return The id of the created entity.
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "text/plain")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public String create(final InputStream src)
            throws IOException {
        return httpHelper.doPost("/entity", new InputStreamEntity(src, -1), "application/json");
    }

    /**
     * Controller method for updating an {@link net.objecthunter.larch.model.Entity} using a HTTP PUT with a JSON
     * entity representation as request body
     * 
     * @param id The is of the Entity to update
     * @param src The Stream injected by Spring MVC containing the JSON representation of the updated Entity
     * @throws IOException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("id") final String id,
            final InputStream src) throws IOException {
        httpHelper.doPut("/entity/" + id, new InputStreamEntity(src, -1), "application/json");
    }

    /**
     * Controller method for deleting an {@link net.objecthunter.larch.model.Entity} using a HTTP DELETE request.
     * 
     * @param id The is of the Entity to delete
     * @throws IOException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") final String id)
            throws IOException {
        httpHelper.doDelete("/entity/" + id);
    }

    @RequestMapping(value = "/{id}/publish", method = RequestMethod.PUT, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView publishHtml(@PathVariable("id") final String id) throws IOException {
        httpHelper.doPut("/entity/" + id + "/publish", null, null);
        return this.retrieveHtml(id);
    }

    @RequestMapping(value = "/{id}/submit", method = RequestMethod.PUT, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView submitHtml(@PathVariable("id") final String id) throws IOException {
        httpHelper.doPut("/entity/" + id + "/submit", null, null);
        return this.retrieveHtml(id);
    }

    @RequestMapping(value = "/{id}/withdraw", method = RequestMethod.PUT, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView withdrawHtml(@PathVariable("id") final String id) throws IOException {
        httpHelper.doPut("/entity/" + id + "/withdraw", null, null);
        return this.retrieveHtml(id);
    }

}
