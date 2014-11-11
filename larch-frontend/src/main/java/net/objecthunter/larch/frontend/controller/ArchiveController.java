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
package net.objecthunter.larch.frontend.controller;

import net.objecthunter.larch.frontend.util.HttpHelper;
import net.objecthunter.larch.model.Archive;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@Controller
/**
 * @author Michael Hoppe
 */
public class ArchiveController extends AbstractController {

    @Autowired
    private HttpHelper httpHelper;

    @Autowired
    private Environment env;

    @RequestMapping(value="/archives/{offset}/{count}", method= RequestMethod.GET)
    public ModelAndView listArchives(@PathVariable("offset") final int offset, @PathVariable("count") final int count)
            throws IOException {

        final ModelMap model = new ModelMap();

        // retrieve the data from the backend and deserialize it into a collection
        final String data = httpHelper.doGet("/archive/list/" + offset + "/" + count);
        List<Archive> archives = this.mapper.readValue(data, this.mapper.getTypeFactory()
                .constructCollectionType(List.class, Archive.class));
        model.addAttribute("archives", archives);

        return new ModelAndView("archives", model);
    }

    @RequestMapping(value="/archive/{entityId}/{version}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.CREATED)
    public void archive(@PathVariable("entityId") final String entityId, @PathVariable("version") final int version)
            throws IOException {

        httpHelper.doPut("/archive/" + entityId + "/" + version, null, null);
    }

    @RequestMapping(value = "/archive/{entityId}/{version}/content", method = RequestMethod.GET)
    public String retrieveContent(@PathVariable("entityId") final String entityId, @PathVariable("version") final int version) throws IOException {
        return "redirect:" + env.getProperty("larch.server.url") + "/archive/" + entityId + "/" + version + "/content";
    }
}
