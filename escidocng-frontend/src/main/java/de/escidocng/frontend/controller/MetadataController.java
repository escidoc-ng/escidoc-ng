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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.escidocng.frontend.util.HttpHelper;
import de.escidocng.model.Metadata;
import de.escidocng.model.MetadataType;
import de.escidocng.model.security.annotation.Permission;
import de.escidocng.model.security.annotation.PreAuth;
import de.escidocng.model.security.role.Role.RoleName;

/**
 * Web controller responsible for interaction on the meta data level
 */
@Controller
public class MetadataController extends AbstractController {

    @Autowired
    private HttpHelper httpHelper;

    /**
     * Controller method for adding {@link de.escidocng.model.Metadata} with a given name to an
     * {@link de.escidocng.model.Entity} using a HTTP POST with multipart/form-data
     * 
     * @param entityId The is of the Entity to which the Metadata should be added
     * @param mdName The name of the Metadata
     * @param type The type of the Metadata
     * @param file The Spring MVC injected MutlipartFile containing the actual data from a html form submission
     * @return a redirection to the Entity to which the Metadata was added
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/metadata", method = RequestMethod.POST,
            consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public String addMetadataHtml(@PathVariable("id") final String entityId,
            @RequestParam("name") final String mdName,
            @RequestParam("type") final String type, @RequestParam(value = "indexInline", required = false,
                    defaultValue = "false") final String indexInline, @RequestParam("metadata") final MultipartFile file)
            throws IOException {
        HttpEntity multipart =
                MultipartEntityBuilder.create()
                        .addTextBody("name", mdName)
                        .addTextBody("type", type)
                        .addTextBody("indexInline", indexInline)
                        .addBinaryBody("data", file.getInputStream(), ContentType.create(file.getContentType()),
                                file.getOriginalFilename())
                        .build();
        httpHelper.doPost("/entity/" + entityId + "/metadata", multipart, null);
        return "redirect:/entity/" + entityId;
    }

    /**
     * Controller method for adding {@link de.escidocng.model.Metadata} with a given name to an
     * {@link de.escidocng.model.Binary} using a HTTP POST with multipart/form-data
     * 
     * @param entityId The is of the Entity to which the Metadata should be added
     * @param binaryName the name of the binary
     * @param mdName the meta data set's name
     * @param type the meta data set's type
     * @param file the http multipart file containing the actual bytes
     * @throws IOException
     */
    @RequestMapping(value = "/entity/{id}/binary/{binary-name}/metadata",
            method = RequestMethod.POST,
            consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public String addBinaryMetadataHtml(@PathVariable("id") final String entityId,
            @PathVariable("binary-name") final String binaryName, @RequestParam("name") final String mdName,
            @RequestParam("type") final String type, @RequestParam(value = "indexInline", required = false,
                    defaultValue = "false") final String indexInline, @RequestParam("metadata") final MultipartFile file)
            throws IOException {
        HttpEntity multipart = MultipartEntityBuilder.create()
                .addTextBody("name", mdName)
                .addTextBody("type", type)
                .addTextBody("indexInline", indexInline)
                .addBinaryBody("data", file.getInputStream(), ContentType.create(file.getContentType()),
                        file.getOriginalFilename())
                .build();
        httpHelper.doPost("/entity/" + entityId + "/binary/" + binaryName + "/metadata", multipart, null);
        return "redirect:/entity/" + entityId + "/binary/" + binaryName;
    }

    /**
     * Controller method to retrieve the XML data of a {@link de.escidocng.model.Metadata} object of an
     * {@link de.escidocng.model.Entity} using a HTTP GET
     * 
     * @param id The id of the Entity
     * @param metadataName The name of the Metadata to retrieve
     * @param accept the Spring MVC injected accept header of the HTTP GET request
     * @param resp the Spting MVC injected {@link javax.servlet.http.HttpServletResponse} to which the XML gets
     *        directly written
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET,
            value = "/entity/{id}/metadata/{metadata-name}/content", produces = {
                "application/xml", "text/xml" })
    @ResponseStatus(HttpStatus.OK)
    public void retrieveMetadataXml(@PathVariable("id") final String id,
            @PathVariable("metadata-name") final String metadataName,
            final HttpServletResponse resp) throws IOException {
        resp.setContentType("text/xml");
        resp.setHeader("Content-Disposition", "inline");
        HttpResponse serverResponse =
                httpHelper.doGetAsResponse("/entity/" + id + "/metadata/" + metadataName + "/content");
        IOUtils.copy(serverResponse.getEntity().getContent(), resp.getOutputStream());
        resp.flushBuffer();
    }

    /**
     * Controller method to retrieve the XML data of a {@link de.escidocng.model.Metadata} object of an
     * {@link de.escidocng.model.Entity} using a HTTP GET
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
            value = "/entity/{id}/binary/{binary-name}/metadata/{metadata-name}/content",
            produces = {
                "application/xml", "text/xml" })
    @ResponseStatus(HttpStatus.OK)
    public void retrieveBinaryMetadataXml(@PathVariable("id") final String id,
            @PathVariable("binary-name") final String binaryName,
            @PathVariable("metadata-name") final String metadataName,
            final HttpServletResponse resp) throws IOException {
        resp.setContentType("text/xml");
        resp.setHeader("Content-Disposition", "inline");
        HttpResponse serverResponse =
                httpHelper.doGetAsResponse("/entity/" + id + "/binary/" + binaryName + "/metadata/" + metadataName +
                        "/content");
        IOUtils.copy(serverResponse.getEntity().getContent(), resp.getOutputStream());
        resp.flushBuffer();
    }

    /**
     * Controller method to retrieve the available {@link de.escidocng.model.MetadataType}s in the
     * repository in a HTML view
     * 
     * @return A Spring MVC {@link org.springframework.web.servlet.ModelAndView} used to render the HTML view
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/metadatatype", produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView retrieveTypesHtml() throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("types", mapper.readValue(httpHelper.doGet("/metadatatype"),
                new TypeReference<List<MetadataType>>() {}));
        return new ModelAndView("metadatatype", model);
    }

    /**
     * Add a new {@link de.escidocng.model.MetadataType} to the repository that can be used to validate
     * different kind of Metadata objects, using a HTML form
     * 
     * @param name The name of the new {@link de.escidocng.model.MetadataType}
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, value = "/metadatatype", consumes = "multipart/form-data",
            produces = "text/html")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuth(permissions = {
        @Permission(rolename = RoleName.ROLE_ADMIN) })
    public String addSchemaType(@RequestParam("name") final String name,
            @RequestParam("schemaUrl") final String schemaUrl) throws IOException {
        final StringEntity entity = new StringEntity(mapper.writeValueAsString(new MetadataType(name, schemaUrl)));
        httpHelper.doPost("/metadatatype", entity, "application/json");
        return "redirect:/metadatatype";
    }

    @RequestMapping(method = RequestMethod.GET,
            value = "/entity/{id}/metadata/{metadata-name}",
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView retrieveMetadataHtml(@PathVariable("id") final String entityId,
            @PathVariable("metadata-name") final String mdName) throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("entityId", entityId);
        model.addAttribute("md", mapper.readValue(httpHelper.doGet("/entity/" + entityId + "/metadata/" + mdName),
                Metadata.class));
        return new ModelAndView("metadata", model);
    }

    @RequestMapping(method = RequestMethod.GET,
            value = "/entity/{id}/binary/{binary-name}/metadata/{metadata-name}",
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView retrieveBinaryMetadataHtml(@PathVariable("id") final String entityId,
            @PathVariable("binary-name") final String binaryName, @PathVariable("metadata-name") final String mdName)
            throws IOException {
        final ModelMap model = new ModelMap();
        model.addAttribute("entityId", entityId);
        model.addAttribute("binaryName", binaryName);
        model.addAttribute("md", mapper.readValue(httpHelper.doGet("/entity/" + entityId + "/binary/" + binaryName +
                "/metadata/" + mdName),
                Metadata.class));
        return new ModelAndView("binarymetadata", model);
    }

    @RequestMapping(method = RequestMethod.DELETE,
            value = "/entity/{id}/metadata/{metadata-name}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteMetadata(@PathVariable("id") final String entityId,
            @PathVariable("metadata-name") final String mdName) throws IOException {
        httpHelper.doDelete("/entity/" + entityId + "/metadata/" + mdName);
    }

    @RequestMapping(method = RequestMethod.DELETE,
            value = "/entity/{id}/binary/{binary-name}/metadata/{metadata-name}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteBinaryMetadata(@PathVariable("id") final String entityId,
            @PathVariable("binary-name") final String binaryName, @PathVariable("metadata-name") final String mdName)
            throws IOException {
        httpHelper.doDelete("/entity/" + entityId + "/binary/" + binaryName + "/metadata/" + mdName);
    }
}
