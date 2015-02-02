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
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.type.TypeReference;

import de.escidocng.frontend.util.HttpHelper;
import de.escidocng.model.AlternativeIdentifier;
import de.escidocng.model.Archive;
import de.escidocng.model.Entity;
import de.escidocng.model.MetadataType;

@Controller
/**
 * @author Michael Hoppe
 */
public class ArchiveController extends AbstractController {

    @Autowired
    private HttpHelper httpHelper;

    @Autowired
    private Environment env;

    @RequestMapping(value="/archive/{entityId}/{version}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.CREATED)
    public void archive(@PathVariable("entityId") final String entityId, @PathVariable("version") final int version)
            throws IOException {

        httpHelper.doPut("/archive/" + entityId + "/" + version, null, null);
    }

    @RequestMapping(value = "/archive/{entityId}/{version}", method = RequestMethod.GET)
    public ModelAndView retrieveArchiveHtml(@PathVariable("entityId") final String entityId, @PathVariable("version") final int version) throws IOException {
        final ModelMap model = new ModelMap();
        final Archive archive = mapper.readValue(httpHelper.doGet("/archive/" + entityId + "/" + version), Archive.class);
        model.addAttribute("archive", archive);
        return new ModelAndView("archive", model);
    }

    @RequestMapping(value = "/archive/{entityId}/{version}/content", method = RequestMethod.GET)
    public void retrieveContent(@PathVariable("entityId") final String entityId, @PathVariable("version") final int version,
            final HttpServletResponse response) throws IOException {
        HttpResponse serverResponse = httpHelper.doGetAsResponse("/archive/" + entityId + "/" + version + "/content");
        response.setContentType(ContentType.getOrDefault(serverResponse.getEntity()).getMimeType());
        response.setContentLength((int)serverResponse.getEntity().getContentLength());
        response.setHeader("Content-Disposition", serverResponse.getFirstHeader("Content-Disposition").getValue());
        IOUtils.copy(serverResponse.getEntity().getContent(), response.getOutputStream());
        response.flushBuffer();
    }
}
