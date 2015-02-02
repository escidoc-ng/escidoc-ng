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

package de.escidocng.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.escidocng.model.SearchResult;
import de.escidocng.service.ArchiveService;
import de.escidocng.service.CredentialsService;
import de.escidocng.service.EntityService;

/**
 * Web controller responsible for search.
 */
@Controller
@RequestMapping("/search")
public class SearchController extends AbstractEscidocngController {

    @Autowired
    private EntityService entityService;

    @Autowired
    private CredentialsService credentialsService;

    @Autowired
    private ArchiveService archiveService;

    /**
     * Controller method for searching {@link de.escidocng.model.Entity}s in the repository using an HTTP
     * POST which returns a JSON representation of the {@link de.escidocng.model.SearchResult}.<br>
     * The request can contain the following parameters:<br>
     * query: search-query.<br>
     * offset: hit-number to start searchresult-list with.<br>
     * maxRecords: maximum number of records to return with searchresult-list.<br>
     * <br>Supported Search-Fields:<br>
     * id<br>
     * label<br>
     * contentModelId<br>
     * parentId<br>
     * tags<br>
     * state<br>
     * version<br>
     * level1<br>
     * level2<br>
     * _all<br>
     * 
     * @param query the search query.
     * @param offset it-number to start searchresult-list with.
     * @param maxRecords maximum number of records to return with searchresult-list
     * @return A {@link de.escidocng.model.SearchResult} containing the found
     *         {@link de.escidocng .model.Entity}s as s JSON representation
     */
    @RequestMapping(method = RequestMethod.GET, value="/entities")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public SearchResult searchEntities(@RequestParam(
            value = "query", defaultValue = "*:*") final String query, @RequestParam(
            value = "offset", defaultValue = "0") final int offset, @RequestParam(
            value = "maxRecords", defaultValue = "50") final int maxRecords) throws IOException {
        if (maxRecords > -1) {
            return entityService.searchEntities(query, offset, maxRecords);
        } else {
            return entityService.searchEntities(query, offset);
        }
    }

    /**
     * Controller method for searching {@link de.escidocng.model.security.User}s in the repository using an HTTP
     * POST which returns a JSON representation of the {@link de.escidocng.model.SearchResult}.<br>
     * The request can contain the following parameters:<br>
     * query: search-query.<br>
     * offset: hit-number to start searchresult-list with.<br>
     * maxRecords: maximum number of records to return with searchresult-list.<br>
     * <br>Supported Search-Fields:<br>
     * name<br>
     * firstName<br>
     * lastName<br>
     * email<br>
     * _all<br>
     * 
     * @param query the search query.
     * @param offset it-number to start searchresult-list with.
     * @param maxRecords maximum number of records to return with searchresult-list
     * @return A {@link de.escidocng.model.SearchResult} containing the found
     *         {@link de.escidocng .model.User}s as s JSON representation
     */
    @RequestMapping(method = RequestMethod.GET, value="/users")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public SearchResult searchUsers(@RequestParam(
            value = "query", defaultValue = "*:*") final String query, @RequestParam(
            value = "offset", defaultValue = "0") final int offset, @RequestParam(
            value = "maxRecords", defaultValue = "50") final int maxRecords) throws IOException {
        return credentialsService.searchUsers(query, offset, maxRecords);
    }

    /**
     * Controller method for searching {@link de.escidocng.model.security.Archive}s in the repository using an
     * HTTP GET which returns a JSON representation of the {@link de.escidocng.model.SearchResult}.<br>
     * The request can contain the following parameters:<br>
     * query: search-query.<br>
     * offset: hit-number to start searchresult-list with.<br>
     * maxRecords: maximum number of records to return with searchresult-list.<br>
     * <br>Supported Search-Fields:<br>
     * entityId<br>
     * entityVersion<br>
     * contentModelId<br>
     * creator<br>
     * state<br>
     * level1<br>
     * level2<br>
     * _all<br>
     * 
     * @param query the search query.
     * @param offset it-number to start searchresult-list with.
     * @param maxRecords maximum number of records to return with searchresult-list
     * @return A {@link de.escidocng.model.SearchResult} containing the found
     *         {@link de.escidocng.model.Archive}s as s JSON representation
     */
    @RequestMapping(method = RequestMethod.GET, value="/archives")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public SearchResult searchArchives(@RequestParam(
            value = "query", defaultValue = "*:*") final String query, @RequestParam(
            value = "offset", defaultValue = "0") final int offset, @RequestParam(
            value = "maxRecords", defaultValue = "50") final int maxRecords)
            throws IOException {
        return archiveService.searchArchives(query, offset, maxRecords);
    }

}