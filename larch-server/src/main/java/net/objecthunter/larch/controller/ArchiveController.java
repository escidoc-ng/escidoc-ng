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

import net.objecthunter.larch.service.ArchiveService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Controller
@RequestMapping("/archive")
public class ArchiveController extends AbstractLarchController {

    @Autowired
    private ArchiveService archiveService;

    @RequestMapping(value = "{entityId}/{version}", method = RequestMethod.GET)
    public void retrieve(@PathVariable("entityId") final String entityId, @PathVariable("version") final int version,
                         HttpServletResponse resp) {
        try (InputStream src = this.archiveService.retrieve(entityId, version);
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
}
