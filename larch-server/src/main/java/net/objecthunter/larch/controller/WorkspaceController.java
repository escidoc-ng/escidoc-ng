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

import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.service.EntityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/workspace")
public class WorkspaceController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public String create(@RequestBody final Workspace workspace) throws IOException {
        return this.entityService.createWorkspace(workspace);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Workspace retrieve(@PathVariable("id") final String id) throws IOException {
        return this.entityService.retrieveWorkspace(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("id") final String id, @RequestBody final Workspace workspace) throws IOException {
        if (!id.equals(workspace.getId())) {
            throw new IOException("Workspace id does not match id given in the URL");
        }
        this.entityService.updateWorkspace(workspace);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void patch(@PathVariable("id") final String id, @RequestBody final Workspace workspace) throws IOException {
        if (!id.equals(workspace.getId())) {
            throw new IOException("Workspace id does not match id given in the URL");
        }
        this.entityService.patchWorkspace(workspace);
    }
}
