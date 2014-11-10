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

import javax.servlet.http.HttpServletRequest;

import net.objecthunter.larch.frontend.Constants;
import net.objecthunter.larch.frontend.util.HttpHelper;
import net.objecthunter.larch.model.security.User;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Web controller for login
 */
@Controller
public class LoginController extends AbstractController {

    @Autowired
    private Environment env;

    @Autowired
    private HttpHelper httpHelper;

    /**
     * Controller method for logging in
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = { "text/html" })
    public String login() throws IOException {
        OAuthClientRequest oauthRequest = null;
        try {
            oauthRequest = OAuthClientRequest
                    .authorizationLocation(env.getProperty("larch.login.url") + "/oauth/authorize")
                    .setClientId(env.getProperty("larch.oauth.clientId"))
                    .setResponseType("code")
                    .setRedirectURI(env.getProperty("self.url") + "/login/token")
                    .buildQueryMessage();
        } catch (OAuthSystemException e) {
            throw new IOException(e.getMessage());
        }
        return "redirect:" + oauthRequest.getLocationUri();
    }

    /**
     * Controller method for logging in
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET, produces = { "text/html" })
    public String logout(HttpServletRequest request) throws IOException {
        httpHelper.doPost("/logout", null, null);
        request.getSession().removeAttribute(Constants.ACCESS_TOKEN_ATTRIBUTE_NAME);
        request.getSession().removeAttribute(Constants.CURRENT_USER_NAME);
        request.getSession().invalidate();
        return "redirect:/";
    }

    /**
     * Controller method for getting the oauth-token
     * 
     * @return a Spring MVC {@link org.springframework.web.servlet.ModelAndView} for rendering the HTML view
     */
    @RequestMapping(value = "/login/token", method = RequestMethod.GET, produces = { "text/html" })
    public String getOauthToken(HttpServletRequest request) throws IOException {
        try {
            OAuthAuthzResponse oar = OAuthAuthzResponse.oauthCodeAuthzResponse(request);
            String code = oar.getCode();
            
            //content
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("grant_type", "authorization_code"));
            nvps.add(new BasicNameValuePair("client_id", env.getProperty("larch.oauth.clientId")));
            nvps.add(new BasicNameValuePair("client_secret", env.getProperty("larch.oauth.clientSecret")));
            nvps.add(new BasicNameValuePair("code", code));
            nvps.add(new BasicNameValuePair("redirect_uri",
                    env.getProperty("self.url") + "/login/token"));

            //auth header
            String authorization = env.getProperty("larch.oauth.clientId") + ":" + env.getProperty("larch.oauth.clientSecret");
            byte[] encodedBytes = Base64.encodeBase64(authorization.getBytes());
            authorization = "Basic " + new String(encodedBytes);

            String response = httpHelper.doPost("/oauth/token", new UrlEncodedFormEntity(nvps), authorization, null);

            JSONObject obj = new JSONObject(response);
            request.getSession().setAttribute(Constants.ACCESS_TOKEN_ATTRIBUTE_NAME, obj.getString("access_token"));
            
            String userJson = httpHelper.doGet("/current-user");
            request.getSession().setAttribute(Constants.CURRENT_USER_NAME, mapper.readValue(userJson, User.class));
            
        } catch (OAuthProblemException e) {
            throw new IOException(e.getMessage());
        }
        return "redirect:/";
    }

}
