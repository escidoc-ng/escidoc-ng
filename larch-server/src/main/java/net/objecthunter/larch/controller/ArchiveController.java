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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import net.objecthunter.larch.model.Archive;
import net.objecthunter.larch.model.security.ObjectType;
import net.objecthunter.larch.model.security.PermissionType;
import net.objecthunter.larch.model.security.annotation.Permission;
import net.objecthunter.larch.model.security.annotation.PreAuth;
import net.objecthunter.larch.model.security.role.Role.RoleName;
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

@Controller
@RequestMapping("/archive")
public class ArchiveController extends AbstractLarchController {

    @Autowired
    private ArchiveService archiveService;

    /**
     * Controller method to retrieve the Archived Data (zipfile) of an 
     * {@link net.objecthunter.larch.model.Entity}-Version
     * 
     * @param entityId The entity's id for which the Archived Data should be returned.
     * @param version The version of the entity for which the Archived Data should be returned.
     * @return An InputStream containing the Archived Data as Zipfile.
     * @throws IOException
     */
    @RequestMapping(value = "/{entityId}/{version}/content", method = RequestMethod.GET)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, versionIndex = 1, permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.READ),
            @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN, permissionType = PermissionType.READ) })
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

    /**
     * Controller method to archive an 
     * {@link net.objecthunter.larch.model.Entity}-Version
     * 
     * @param entityId The entity's id for which the Archived Data should be returned.
     * @param version The version of the entity for which the Archived Data should be returned.
     * @throws IOException
     */
    @RequestMapping(value = "/{entityId}/{version}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, versionIndex = 1, permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.WRITE),
            @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN, permissionType = PermissionType.WRITE) })
    public void archive(@PathVariable("entityId") final String entityId, @PathVariable("version") final int version) throws IOException {
        archiveService.archive(entityId, version);
    }

    /**
     * Controller method to retrieve archive-metadata for a particular entity-version.
     * 
     * @param entityId The entity's id for which the Archived Data should be returned.
     * @param version The version of the entity for which the Archived Data should be returned.
     * @return Archive Archive-Meatdata
     * @throws IOException
     */
    @RequestMapping(value="/{entityId}/{version}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(objectType = ObjectType.ENTITY, idIndex = 0, versionIndex = 1, permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN),
            @Permission(rolename = RoleName.ROLE_USER, permissionType = PermissionType.READ),
            @Permission(rolename = RoleName.ROLE_LEVEL1_ADMIN, permissionType = PermissionType.READ) })
    public Archive retrieveArchive(@PathVariable("entityId") final String entityId, @PathVariable("version") final int version) throws IOException {
        return archiveService.retrieve(entityId, version);
    }

}
