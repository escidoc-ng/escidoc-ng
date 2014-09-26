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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.objecthunter.larch.frontend.Constants;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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

/**
 * Web controller for login
 */
@Controller
public class LoginController {

    @Autowired
    private Environment env;

    /**
     * Controller method for logging in
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = { "text/html" })
    public String login() throws IOException {
        OAuthClientRequest oauthRequest = null;
        try {
            oauthRequest = OAuthClientRequest
                    .authorizationLocation(env.getProperty("larch.server.url") + "/oauth/authorize")
                    .setClientId(env.getProperty("larch.oauth.clientId"))
                    .setResponseType("code")
                    .setRedirectURI(env.getProperty("self.url") + ":" + env.getProperty("server.port") + "/login/token")
                    .buildQueryMessage();
        } catch (OAuthSystemException e) {
            throw new IOException(e.getMessage());
        }
        return "redirect:" + oauthRequest.getLocationUri();
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

            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(env.getProperty("larch.server.url") + "/oauth/token");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("grant_type", "authorization_code"));
            nvps.add(new BasicNameValuePair("client_id", env.getProperty("larch.oauth.clientId")));
            nvps.add(new BasicNameValuePair("client_secret", env.getProperty("larch.oauth.clientSecret")));
            nvps.add(new BasicNameValuePair("code", code));
            nvps.add(new BasicNameValuePair("redirect_uri",
                    env.getProperty("self.url") + ":" + env.getProperty("server.port") + "/login/token"));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            String authorization = env.getProperty("larch.oauth.clientId") + ":" + env.getProperty("larch.oauth.clientSecret");
            byte[] encodedBytes = Base64.encodeBase64(authorization.getBytes());
            authorization = "Basic " + new String(encodedBytes);
            httpPost.setHeader("Authorization", authorization);
            CloseableHttpResponse response2 = httpclient.execute(httpPost);

            String test = null;
            String token = null;
            try {
                HttpEntity entity2 = response2.getEntity();
                test = EntityUtils.toString(entity2);
                JSONObject obj = new JSONObject(test);
                token = obj.getString("access_token");
            } finally {
                response2.close();
            }

            request.getSession().setAttribute(Constants.ACCESS_TOKEN_ATTRIBUTE_NAME, token);
        } catch (OAuthProblemException e) {
            throw new IOException(e.getMessage());
        }
        return "redirect:/";
    }

}
