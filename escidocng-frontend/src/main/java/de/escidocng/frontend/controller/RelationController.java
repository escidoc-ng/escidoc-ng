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
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.escidocng.frontend.util.HttpHelper;

/**
 * Web controller responsible for interactions on the relation level
 */
@Controller
@RequestMapping("/entity/{id}/relation")
public class RelationController extends AbstractController {

    @Autowired
    private HttpHelper httpHelper;

    /**
     * Controller method for adding a new triple relating an {@link de.escidocng.model.Entity} via a
     * predicate to an object using a HTTP POSTm that redirects to an HTML view of the
     * {@link de.escidocng.model.Entity}
     * 
     * @param id the id of the Entity which should be the subject of this relation
     * @param predicate the predicate of the relation
     * @param object the object of the relation
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    public String createHtml(@PathVariable("id") final String id, @RequestParam("predicate") final String predicate,
            @RequestParam("object") final String object) throws IOException {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("predicate", predicate));
        nvps.add(new BasicNameValuePair("object", object));
        httpHelper.doPost("/entity/" + id + "/relation", new UrlEncodedFormEntity(nvps), null);
        return "redirect:/entity/" + id;
    }
}
