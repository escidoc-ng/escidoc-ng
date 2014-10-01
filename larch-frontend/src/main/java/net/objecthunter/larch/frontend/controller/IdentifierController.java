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
 * WITHOUT WARRANTIES OR CONDITIONS OF ROLE_ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package net.objecthunter.larch.frontend.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.objecthunter.larch.frontend.util.HttpHelper;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Web controller responsible for interactions on the identifier level
 */
@Controller
@RequestMapping("/entity/{id}")
public class IdentifierController extends AbstractController {

    @Autowired
    private HttpHelper httpHelper;

    /**
     * Controller method for adding a new identifier using a HTTP POST that redirects to an HTML view of the
     * {@link net.objecthunter.larch.model.Entity}
     * 
     * @param entityId the id of the Entity to which the identifier is added
     * @param type the type of the identifier
     * @param value the value of the identifier
     * @throws IOException
     */
    @RequestMapping(value = "/identifier", method = RequestMethod.POST, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    public String createHtml(@PathVariable("id") final String entityId, @RequestParam("type") final String type,
            @RequestParam("value") final String value) throws IOException {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("type", type));
        nvps.add(new BasicNameValuePair("value", value));
        httpHelper.doPost("/entity/" + entityId + "/identifier", new UrlEncodedFormEntity(nvps));
        return "redirect:/entity/" + entityId;
    }

    /**
     * Controller method to delete an identifier
     * 
     * @param entityId the entity's id
     * @param type the type of the identifier
     * @param value the value of the identifier
     * @throws IOException
     */
    @RequestMapping(value = "/identifier/{type}/{value}", method = RequestMethod.DELETE,
            produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String deleteHtml(@PathVariable("id") final String entityId, @PathVariable("type") final String type,
            @PathVariable("value") final String value) throws IOException {
        httpHelper.doDelete("/entity/" + entityId + "/identifier/" + type + "/" + value);
        return "redirect:/entity/" + entityId;
    }

}
