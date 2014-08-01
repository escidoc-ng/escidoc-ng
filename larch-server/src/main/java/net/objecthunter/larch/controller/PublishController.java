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

import net.objecthunter.larch.model.Entities;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.service.MessagingService;
import net.objecthunter.larch.service.PublishService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web controller responsible for interactions on the published entity level
 */
@Controller
@RequestMapping("/workspace/{workspaceId}/published")
public class PublishController extends AbstractLarchController {

    @Autowired
    private PublishService publishedEntityService;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Controller method for retrieval of a JSON representation of the current published version of an
     * {@link net.objecthunter .larch.model.Entity}
     * 
     * @param id the {@link net.objecthunter.larch.model.Entity}'s id
     * @return An Entity object which gets transformed into a JSON response by Spring MVC
     * @throws IOException
     */
    @RequestMapping("/{id}")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Entity retrieve(@PathVariable("id") final String id) throws IOException {
        return publishedEntityService.retrieve(id);
    }

    /**
     * Controller method for retrieval of a HTML view of the current published version of an
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
        model.addAttribute("entity", publishedEntityService.retrieve(id));
        return new ModelAndView("publishedentity", model);
    }

    /**
     * Controller method for retrieval of a JSON representation of the current published version of an
     * {@link net.objecthunter .larch.model.Entity}
     * 
     * @param id the {@link net.objecthunter.larch.model.Entity}'s id
     * @return An Entity object which gets transformed into a JSON response by Spring MVC
     * @throws IOException
     */
    @RequestMapping("/{entityId}/list")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public Entities retrieveForEntityId(@PathVariable("entityId") final String entityId) throws IOException {
        return publishedEntityService.retrievePublishedEntities(entityId);
    }

    /**
     * Controller method for retrieval of a HTML view of the current published version of an
     * {@link net.objecthunter.larch.model.Entity}
     * 
     * @param id The is of the {@link net.objecthunter.larch.model.Entity} to retrieve
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView} for rendering the HTML view
     * @throws IOException
     */
    @RequestMapping(value = "/{entityId}/list", produces = "text/html")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView retrieveHtmlForEntityId(@PathVariable("entityId") final String entityId) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("entities", publishedEntityService.retrievePublishedEntities(entityId));
        // ModelAndView m = new ModelAndView("publishedentities", model);
        return new ModelAndView("publishedentities", model);
    }
}
