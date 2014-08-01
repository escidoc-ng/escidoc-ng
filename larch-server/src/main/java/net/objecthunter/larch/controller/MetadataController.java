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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import net.objecthunter.larch.helpers.AuditRecordHelper;
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.MetadataType;
import net.objecthunter.larch.model.MetadataValidationResult;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.MessagingService;
import net.objecthunter.larch.service.SchemaService;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web controller responsible for interaction on the meta data level
 */
@Controller
public class MetadataController extends AbstractLarchController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private ObjectMapper mapper;

    /**
     * Controller method for adding {@link net.objecthunter.larch.model.Metadata} with a given name to an
     * {@link net .objecthunter.larch.model.Entity} using a HTTP POST with multipart/form-data
     * 
     * @param entityId The is of the Entity to which the Metadata should be added
     * @param mdName The name of the Metadata
     * @param type The type of the Metadata
     * @param file The Spring MVC injected MutlipartFile containing the actual data from a html form submission
     * @return a redirection to the Entity to which the Metadata was added
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{workspaceId}/entity/{id}/metadata", method = RequestMethod.POST,
            consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public String addMetadata(@PathVariable("workspaceId") final String workspaceId,
            @PathVariable("id") final String entityId, @RequestParam("name") final String mdName,
            @RequestParam("type") final String type, @RequestParam("metadata") final MultipartFile file)
            throws IOException {
        final Entity e = entityService.retrieve(workspaceId, entityId);
        if (e.getMetadata() == null) {
            e.setMetadata(new HashMap<>());
        }
        else if (e.getMetadata().get(mdName) != null) {
            throw new IOException("Meta data " + mdName + " already exists on Entity " + entityId);
        }
        final Metadata md = new Metadata();
        md.setName(mdName);
        md.setData(IOUtils.toString(file.getInputStream()));
        md.setMimetype(file.getContentType());
        md.setType(type);
        md.setOriginalFilename(file.getOriginalFilename());
        e.getMetadata().put(mdName, md);
        entityService.update(workspaceId, e);
        this.entityService.createAuditRecord(AuditRecordHelper.createMetadataRecord(entityId));
        this.messagingService.publishCreateMetadata(entityId, mdName);
        return "redirect:/workspace/" + workspaceId + "/entity/" + entityId;
    }

    /**
     * Controller method for adding {@link net.objecthunter.larch.model.Metadata} with a given name to an
     * {@link net .objecthunter.larch.model.Entity} using a HTTP POST with application/json
     * 
     * @param entityId The is of the Entity to which the Metadata should be added
     * @param src the request body as an InputStream
     * @return a redirection to the Entity to which the Metadata was added
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{workspaceId}/entity/{id}/metadata", method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public void addMetadata(@PathVariable("workspaceId") final String workspaceId,
            @PathVariable("id") final String entityId, final InputStream src) throws IOException {
        final Entity e = entityService.retrieve(workspaceId, entityId);
        final Metadata md = this.mapper.readValue(src, Metadata.class);
        if (e.getMetadata() == null) {
            e.setMetadata(new HashMap<>());
        }
        else if (e.getMetadata().get(md.getName()) != null) {
            throw new IOException("Meta data " + md.getName() + " already exists on Entity " + entityId);
        }
        e.getMetadata().put(md.getName(), md);
        entityService.update(workspaceId, e);
        this.entityService.createAuditRecord(AuditRecordHelper.createMetadataRecord(entityId));
        this.messagingService.publishCreateMetadata(entityId, md.getName());
    }

    /**
     * Controller method for adding {@link net.objecthunter.larch.model.Metadata} with a given name to an
     * {@link net .objecthunter.larch.model.Binary} using a HTTP POST with application/json
     * 
     * @param entityId The is of the Entity to which the Metadata should be added
     * @param src the request body as an InputStream
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{workspaceId}/entity/{id}/binary/{binary-name}/metadata",
            method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public void addBinaryMetadata(@PathVariable("workspaceId") final String workspaceId,
            @PathVariable("id") final String entityId,
            @PathVariable("binary-name") final String binaryName, final InputStream src) throws IOException {

        final Entity e = this.entityService.retrieve(workspaceId, entityId);
        final Metadata md = this.mapper.readValue(src, Metadata.class);
        if (e.getBinaries() == null || !e.getBinaries().containsKey(binaryName)) {
            throw new FileNotFoundException("The binary " + binaryName + " does not exist ");
        }
        final Binary bin = e.getBinaries().get(binaryName);
        if (bin.getMetadata() == null) {
            bin.setMetadata(new HashMap<>());
        }
        if (bin.getMetadata().containsKey(md.getName())) {
            throw new IOException("The meta data " + md.getName() + " already exists on the binary " + binaryName +
                    ""
                    + " of the entity " + entityId);
        }
        bin.getMetadata().put(md.getName(), md);
        this.entityService.update(workspaceId, e);
        this.entityService.createAuditRecord(AuditRecordHelper.createBinaryMetadataRecord(entityId));
        this.messagingService.publishCreateBinaryMetadata(entityId, binaryName, md.getName());
    }

    /**
     * Controller method for adding {@link net.objecthunter.larch.model.Metadata} with a given name to an
     * {@link net .objecthunter.larch.model.Binary} using a HTTP POST with multipart/form-data
     * 
     * @param entityId The is of the Entity to which the Metadata should be added
     * @param binaryName the name of the binary
     * @param mdName the meta data set's name
     * @param type the meta data set's type
     * @param file the http multipart file containing the actual bytes
     * @throws IOException
     */
    @RequestMapping(value = "/workspace/{workspaceId}/entity/{id}/binary/{binary-name}/metadata",
            method = RequestMethod.POST,
            consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public String addBinaryMetadataHtml(@PathVariable("workspaceId") final String workspaceId,
            @PathVariable("id") final String entityId,
            @PathVariable("binary-name") final String binaryName, @RequestParam("name") final String mdName,
            @RequestParam("type") final String type, @RequestParam("metadata") final MultipartFile file)
            throws IOException {

        final Entity e = this.entityService.retrieve(workspaceId, entityId);
        final Metadata md = new Metadata();
        md.setName(mdName);
        md.setType(type);
        md.setData(IOUtils.toString(file.getInputStream()));
        md.setOriginalFilename(file.getOriginalFilename());
        md.setUtcCreated(file.getContentType());

        if (e.getBinaries() == null || !e.getBinaries().containsKey(binaryName)) {
            throw new FileNotFoundException("The binary " + binaryName + " does not exist on the entity " + entityId);
        }
        final Binary bin = e.getBinaries().get(binaryName);
        if (bin.getMetadata() == null) {
            bin.setMetadata(new HashMap<>());
        }
        if (bin.getMetadata().containsKey(md.getName())) {
            throw new IOException("The meta data " + md.getName() + " already exists on the binary " + binaryName +
                    ""
                    + " of the entity " + entityId);
        }
        bin.getMetadata().put(md.getName(), md);
        this.entityService.update(workspaceId, e);
        this.entityService.createAuditRecord(AuditRecordHelper.createBinaryMetadataRecord(entityId));
        this.messagingService.publishCreateBinaryMetadata(entityId, binaryName, mdName);
        return "redirect:/workspace/" + workspaceId + "/entity/" + entityId + "/binary/" + binaryName;
    }

    /**
     * Controller method to retrieve the XML data of a {@link net.objecthunter.larch.model.Metadata} object of an
     * {@link net.objecthunter.larch.model.Entity} using a HTTP GET
     * 
     * @param id The id of the Entity
     * @param metadataName The name of the Metadata to retrieve
     * @param accept the Spring MVC injected accept header of the HTTP GET request
     * @param resp the Spting MVC injected {@link javax.servlet.http.HttpServletResponse} to which the XML gets
     *        directly written
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "/workspace/{workspaceId}/entity/{id}/metadata/{metadata-name}/content", produces = {
                "application/xml", "text/xml" })
    @ResponseStatus(HttpStatus.OK)
    public void retrieveMetadataXml(@PathVariable("workspaceId") final String workspaceId,
            @PathVariable("id") final String id,
            @PathVariable("metadata-name") final String metadataName, @RequestHeader("Accept") final String accept,
            final HttpServletResponse resp) throws IOException {
        resp.setContentType("text/xml");
        resp.setHeader("Content-Disposition", "inline");
        final String data = entityService.retrieve(workspaceId, id).getMetadata().get(metadataName).getData();
        IOUtils.write(data, resp.getOutputStream());
        resp.flushBuffer();
    }

    /**
     * Controller method to retrieve the XML data of a {@link net.objecthunter.larch.model.Metadata} object of an
     * {@link net.objecthunter.larch.model.Entity} using a HTTP GET
     * 
     * @param id The id of the Entity
     * @param binaryName the name the name of the binary
     * @param metadataName The name of the Metadata to retrieve
     * @param accept the Spring MVC injected accept header of the HTTP GET request
     * @param resp the Spting MVC injected {@link javax.servlet.http.HttpServletResponse} to which the XML gets
     *        directly written
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "/workspace/{workspaceId}/entity/{id}/binary/{binary-name}/metadata/{metadata-name}/content",
            produces = {
                "application/xml", "text/xml" })
    @ResponseStatus(HttpStatus.OK)
    public void retrieveBinaryMetadataXml(@PathVariable("workspaceId") final String workspaceId,
            @PathVariable("id") final String id,
            @PathVariable("binary-name") final String binaryName,
            @PathVariable("metadata-name") final String metadataName, @RequestHeader("Accept") final String accept,
            final HttpServletResponse resp) throws IOException {
        resp.setContentType("text/xml");
        resp.setHeader("Content-Disposition", "inline");
        final Entity e = this.entityService.retrieve(workspaceId, id);
        if (e.getBinaries() == null || !e.getBinaries().containsKey(binaryName)) {
            throw new FileNotFoundException("The binary " + binaryName + " does not exist on entity " + id);
        }
        final Binary bin = e.getBinaries().get(binaryName);
        if (bin.getMetadata() == null || !bin.getMetadata().containsKey(metadataName)) {
            throw new FileNotFoundException("The metadata " + metadataName + " does not exist on the binary "
                    + binaryName + " of the entity " + id);
        }
        final String data = bin.getMetadata().get(metadataName).getData();
        IOUtils.write(data, resp.getOutputStream());
        resp.flushBuffer();
    }

    /**
     * Controller method to request the validation result for a {@link net.objecthunter.larch.model.Metadata} object
     * of a given {@link net.objecthunter.larch.model.Entity}
     * 
     * @param id the is of the Entity
     * @param metadataName the name of the Metadata
     * @return A JSON representation of a {@link net.objecthunter.larch.model.MetadataValidationResult}
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "/workspace/{workspaceId}/entity/{id}/metadata/{metadata-name}/validate",
            produces = { "application/json" })
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public MetadataValidationResult validate(@PathVariable("workspaceId") final String workspaceId,
            @PathVariable("id") final String id,
            @PathVariable("metadata-name") final String metadataName) throws IOException {
        return this.schemaService.validate(id, metadataName);
    }

    /**
     * Controller method to request the validation result for a {@link net.objecthunter.larch.model.Metadata} object
     * of a given {@link net.objecthunter.larch.model.Binary}
     * 
     * @param id the is of the Entity
     * @param binaryName the name of the binary
     * @param metadataName the name of the Metadata
     * @return A JSON representation of a {@link net.objecthunter.larch.model.MetadataValidationResult}
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "/workspace/{workspaceId}/entity/{id}/binary/{binary-name}/metadata/{metadata-name}/validate",
            produces = { "application/json" })
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public MetadataValidationResult validate(@PathVariable("workspaceId") final String workspaceId,
            @PathVariable("id") final String id,
            @PathVariable("binary-name") final String binaryName,
            @PathVariable("metadata-name") final String metadataName) throws IOException {
        return this.schemaService.validate(id, binaryName, metadataName);
    }

    /**
     * Controller method to retrieve the available {@link net.objecthunter.larch.model.MetadataType}s in the
     * repository as a JSON representation
     * 
     * @return A JSON representation of a list of MetadataType objects
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/metadatatype", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public List<MetadataType> retrieveTypes() throws IOException {
        return this.schemaService.getSchemaTypes();
    }

    /**
     * Controller method to retrieve the available {@link net.objecthunter.larch.model.MetadataType}s in the
     * repository in a HTML view
     * 
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView} used to render the HTML view
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/metadatatype", produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ModelAndView retrieveTypesHtml() throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("types", this.schemaService.getSchemaTypes());
        return new ModelAndView("metadatatype", model);
    }

    /**
     * Add a new {@link net.objecthunter.larch.model.MetadataType} to the repository that can be used to validate
     * different kind of Metadata objects.
     * 
     * @param src A JSON representation of the new {@link net.objecthunter.larch.model.MetadataType}
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, value = "/metadatatype", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public void addSchemaType(final InputStream src) throws IOException {
        final MetadataType newType = mapper.readValue(src, MetadataType.class);
        this.schemaService.createSchemaType(newType);
    }

    /**
     * Add a new {@link net.objecthunter.larch.model.MetadataType} to the repository that can be used to validate
     * different kind of Metadata objects, using a HTML form
     * 
     * @param name The name of the new {@link net.objecthunter.larch.model.MetadataType}
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, value = "/metadatatype", consumes = "multipart/form-data",
            produces = "text/html")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String addSchemaType(@RequestParam("name") final String name,
            @RequestParam("schemaUrl") final String schemUrl) throws IOException {
        final MetadataType newType = new MetadataType();
        newType.setName(name);
        newType.setSchemaUrl(schemUrl);
        this.schemaService.createSchemaType(newType);
        return "redirect:/metadatatype";
    }

    @RequestMapping(method = RequestMethod.GET,
            value = "/workspace/{workspaceId}/entity/{id}/metadata/{metadata-name}",
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Metadata retrieveMetadata(@PathVariable("workspaceId") final String workspaceId,
            @PathVariable("id") final String entityId,
            @PathVariable("metadata-name") final String mdName) throws IOException {
        final Entity e = this.entityService.retrieve(workspaceId, entityId);
        Metadata md = e.getMetadata().get(mdName);
        if (md == null) {
            throw new FileNotFoundException("Meta data " + mdName + " does not exist on entity " + entityId);
        }
        return md;
    }

    @RequestMapping(method = RequestMethod.GET,
            value = "/workspace/{workspaceId}/entity/{id}/metadata/{metadata-name}",
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView retrieveMetadataHtml(@PathVariable("workspaceId") final String workspaceId,
            @PathVariable("id") final String entityId,
            @PathVariable("metadata-name") final String mdName) throws IOException {
        final Entity e = this.entityService.retrieve(workspaceId, entityId);
        final Metadata md = e.getMetadata().get(mdName);
        if (md == null) {
            throw new FileNotFoundException("Meta data " + mdName + " does not exist on entity " + entityId);
        }
        final ModelMap model = new ModelMap();
        model.addAttribute("metadata", md);
        return new ModelAndView("metadata", model);
    }

    @RequestMapping(method = RequestMethod.GET,
            value = "/workspace/{workspaceId}/entity/{id}/binary/{binary-name}/metadata/{metadata-name}",
            produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Metadata retrieveBinaryMetadata(@PathVariable("workspaceId") final String workspaceId,
            @PathVariable("id") final String entityId,
            @PathVariable("binary-name") final String binaryName, @PathVariable("metadata-name") final String mdName)
            throws IOException {
        final Entity e = this.entityService.retrieve(workspaceId, entityId);
        final Binary b = e.getBinaries().get(binaryName);
        if (b == null) {
            throw new FileNotFoundException("Binary " + binaryName + " does not exist on entity " + entityId);
        }
        final Metadata md = b.getMetadata().get(mdName);
        if (md == null) {
            throw new FileNotFoundException("Meta data " + mdName + " does not exist on Binary " + binaryName
                    + " of entity " + entityId);
        }
        return md;
    }

    @RequestMapping(method = RequestMethod.GET,
            value = "/workspace/{workspaceId}/entity/{id}/binary/{binary-name}/metadata/{metadata-name}",
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView retrieveBinaryMetadataHtml(@PathVariable("workspaceId") final String workspaceId,
            @PathVariable("id") final String entityId,
            @PathVariable("binary-name") final String binaryName, @PathVariable("metadata-name") final String mdName)
            throws IOException {
        final Entity e = this.entityService.retrieve(workspaceId, entityId);
        final Binary b = e.getBinaries().get(binaryName);
        if (b == null) {
            throw new FileNotFoundException("Binary " + binaryName + " does not exist on entity " + entityId);
        }
        final Metadata md = b.getMetadata().get(mdName);
        if (md == null) {
            throw new FileNotFoundException("Meta data " + mdName + " does not exist on Binary " + binaryName
                    + " of entity " + entityId);
        }
        final ModelMap model = new ModelMap();
        model.addAttribute("metadata", md);
        return new ModelAndView("metadata", model);
    }

    @RequestMapping(method = RequestMethod.DELETE,
            value = "/workspace/{workspaceId}/entity/{id}/metadata/{metadata-name}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public void deleteMetadata(@PathVariable("workspaceId") final String workspaceId,
            @PathVariable("id") final String entityId,
            @PathVariable("metadata-name") final String mdName) throws IOException {
        this.entityService.deleteMetadata(workspaceId, entityId, mdName);
        this.entityService.createAuditRecord(AuditRecordHelper.deleteMetadataRecord(entityId));
        this.messagingService.publishDeleteMetadata(entityId, mdName);
    }

    @RequestMapping(method = RequestMethod.DELETE,
            value = "/workspace/{workspaceId}/entity/{id}/binary/{binary-name}/metadata/{metadata-name}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteBinaryMetadata(@PathVariable("workspaceId") final String workspaceId,
            @PathVariable("id") final String entityId,
            @PathVariable("binary-name") final String binaryName, @PathVariable("metadata-name") final String mdName)
            throws IOException {
        this.entityService.deleteBinaryMetadata(workspaceId, entityId, binaryName, mdName);
        this.entityService.createAuditRecord(AuditRecordHelper.deleteBinaryMetadataRecord(entityId));
        this.messagingService.publishDeleteBinaryMetadata(entityId, binaryName, mdName);
    }
}
