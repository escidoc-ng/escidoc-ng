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
package net.objecthunter.larch.controller;

import net.objecthunter.larch.model.Archive;
import net.objecthunter.larch.service.ArchiveService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@Controller
@RequestMapping("/archive")
public class ArchiveController extends AbstractLarchController {

    @Autowired
    private ArchiveService archiveService;

    @RequestMapping(value = "{entityId}/{version}/content", method = RequestMethod.GET)
    public void retrieveContent(@PathVariable("entityId") final String entityId, @PathVariable("version") final int version,
                         HttpServletResponse resp) {
        try (InputStream src = this.archiveService.retrieveData(entityId, version);
            OutputStream sink = resp.getOutputStream()) {

            resp.setStatus(200);
            resp.setHeader("Content-Disposition", "attachment;filename=" + "aip_" + entityId + "_v" + version + ".zip");
            resp.setHeader("Content-Length", String.valueOf(this.archiveService.sizeof(entityId, version)));
            IOUtils.copy(src, sink);

        } catch (FileNotFoundException e) {
            resp.setStatus(404);
        } catch (IOException e) {
            resp.setStatus(500);
        }
    }

    @RequestMapping(value = "{entityId}/{version}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.CREATED)
    public void archive(@PathVariable("entityId") final String entityId, @PathVariable("version") final int version) throws IOException {
        archiveService.archive(entityId, version);
    }

    @RequestMapping(value = "/list/{offset}/{count}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<Archive> listArchives(@PathVariable("offset") final int offset, @PathVariable("count") final int count) throws IOException {
        return archiveService.list(offset, count);
    }

    @RequestMapping(value="{entityId}/{version}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Archive retrieveArchive(@PathVariable("entityId") final String entityId, @PathVariable("version") final int version) throws IOException {
        return archiveService.retrieve(entityId, version);
    }

}
