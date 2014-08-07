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
import java.util.List;

import net.objecthunter.larch.annotations.PreAuth;
import net.objecthunter.larch.annotations.WorkspacePermission;
import net.objecthunter.larch.annotations.WorkspacePermission.ObjectType;
import net.objecthunter.larch.annotations.WorkspacePermission.WorkspacePermissionType;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.model.Workspace;
import net.objecthunter.larch.service.EntityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WorkspaceController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    @RequestMapping(value = "/workspace", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @PreAuth(springSecurityExpression = "hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public String create(@RequestBody final Workspace workspace) throws IOException {
        workspace.setOwner(this.getCurrentUser().getName());
        return this.entityService.createWorkspace(workspace);
    }

    @RequestMapping(value = "/workspace/{id}", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView retrieveHtml(@PathVariable("id") final String id) throws IOException {
        final ModelMap model = new ModelMap("workspace", this.entityService.retrieveWorkspace(id));
        return new ModelAndView("workspace", model);
    }

    @RequestMapping(value = "/workspace/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(springSecurityExpression = "hasAnyRole('ROLE_USER', 'ROLE_ADMIN')",
            workspacePermission = @WorkspacePermission(idIndex = 0,
                    objectType = ObjectType.WORKSPACE, workspacePermissionType = WorkspacePermissionType.READ))
    public Workspace retrieve(@PathVariable("id") final String id) throws IOException {
        return this.entityService.retrieveWorkspace(id);
    }

    @RequestMapping(value = "/workspace/{id}/browse/{offset}/{numRecords}", method = RequestMethod.GET,
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public SearchResult browse(@PathVariable("id") final String id, @PathVariable("offset") final int offset,
            @PathVariable("numRecords") final int numRecords) throws IOException {
        return this.entityService.scanWorkspace(id, offset, numRecords);
    }

    @RequestMapping(value = "/workspace/{id}/browse/{offset}", method = RequestMethod.GET,
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public SearchResult browse(@PathVariable("id") final String id, @PathVariable("offset") final int offset)
            throws IOException {
        return this.entityService.scanWorkspace(id, offset, 0);
    }

    @RequestMapping(value = "/workspace/{id}/browse", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public SearchResult browse(@PathVariable("id") final String id) throws IOException {
        return this.entityService.scanWorkspace(id, 0, 0);
    }

    @RequestMapping(value = "/workspace/{id}/browse/{offset}/{numRecords}", method = RequestMethod.GET,
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ModelAndView browseHtml(@PathVariable("id") final String id, @PathVariable("offset") final int offset,
            @PathVariable("numRecords") final int numRecords) throws IOException {
        final ModelMap model = new ModelMap("result", this.entityService.scanWorkspace(id, offset, numRecords));
        return new ModelAndView("browse", model);
    }

    @RequestMapping(value = "/workspace/{id}/browse/{offset}", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ModelAndView browseHtml(@PathVariable("id") final String id, @PathVariable("offset") final int offset)
            throws IOException {
        return this.browseHtml(id, offset, 0);
    }

    @RequestMapping(value = "/workspace/{id}/browse", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ModelAndView browseHtml(@PathVariable("id") final String id) throws IOException {
        return this.browseHtml(id, 0, 0);
    }

    @RequestMapping(value = "/workspace-list/{offset}/{numRecords}", method = RequestMethod.GET,
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public List<Workspace> retrieveList(@PathVariable("offset") final int offset) throws IOException {
        return this.entityService.scanWorkspaces(null, offset, 0);
    }

    @RequestMapping(value = "/workspace-list", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public List<Workspace> retrieveList() throws IOException {
        return this.entityService.scanWorkspaces(null, 0, 0);
    }

    @RequestMapping(value = "/workspace-list/{offset}", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public List<Workspace> retrieveList(@PathVariable("offset") final int offset,
            @PathVariable("numRecords") final int numRecords) throws IOException {
        return this.entityService.scanWorkspaces(null, offset, numRecords);
    }

    @RequestMapping(value = "/workspace-list/{offset}/{numRecords}", method = RequestMethod.GET,
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ModelAndView retrieveListHtml(@PathVariable("offset") final int offset,
            @PathVariable("numRecords") final int numRecords) throws IOException {
        final ModelMap model = new ModelMap("workspaces", this.retrieveList(offset, numRecords));
        return new ModelAndView("workspaces", model);
    }

    @RequestMapping(value = "/workspace-list/{offset}", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ModelAndView retrieveListHtml(@PathVariable("offset") final int offset) throws IOException {
        final ModelMap model = new ModelMap("workspaces", this.retrieveList(offset, 0));
        return new ModelAndView("workspaces", model);
    }

    @RequestMapping(value = "/workspace-list", method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ModelAndView retrieveListHtml() throws IOException {
        final ModelMap model = new ModelMap("workspaces", this.retrieveList(0, 0));
        return new ModelAndView("workspaces", model);
    }

    @RequestMapping(value = "/workspace/{id}", method = RequestMethod.PUT, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(springSecurityExpression = "hasAnyRole('ROLE_USER', 'ROLE_ADMIN')",
            workspacePermission = @WorkspacePermission(idIndex = 0,
                    objectType = ObjectType.WORKSPACE, workspacePermissionType = WorkspacePermissionType.WRITE))
    public void update(@PathVariable("id") final String id, @RequestBody final Workspace workspace)
            throws IOException {
        if (!id.equals(workspace.getId())) {
            throw new IOException("Workspace id does not match id given in the URL");
        }
        this.entityService.updateWorkspace(workspace);
    }

    @RequestMapping(value = "/workspace/{id}", method = RequestMethod.PATCH, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @PreAuth(springSecurityExpression = "hasAnyRole('ROLE_USER', 'ROLE_ADMIN')",
            workspacePermission = @WorkspacePermission(idIndex = 0,
                    objectType = ObjectType.WORKSPACE, workspacePermissionType = WorkspacePermissionType.WRITE))
    public void patch(@PathVariable("id") final String id, @RequestBody final Workspace workspace) throws IOException {
        if (!id.equals(workspace.getId())) {
            throw new IOException("Workspace id does not match id given in the URL");
        }
        this.entityService.patchWorkspace(workspace);
    }
}
