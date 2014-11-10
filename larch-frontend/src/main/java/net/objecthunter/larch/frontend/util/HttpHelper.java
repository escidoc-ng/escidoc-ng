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
package net.objecthunter.larch.frontend.util;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpSession;

import net.objecthunter.larch.exceptions.AlreadyExistsException;
import net.objecthunter.larch.exceptions.InvalidParameterException;
import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.frontend.Constants;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Michael Hoppe
 */
public class HttpHelper {

    @Autowired
    private Environment env;

    @Autowired
    private ObjectMapper mapper;

    public String doGet(String url) throws IOException {
        HttpGet httpGet = new HttpGet(env.getProperty("larch.server.url") + url);
        String token = getOauthToken();
        if (StringUtils.isNotBlank(token)) {
            httpGet.setHeader("Authorization", "Bearer " + token);
        }
        HttpResponse response = httpClient().execute(httpGet);
        handleStatusCode(response);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }

    public HttpResponse doGetAsResponse(String url) throws IOException {
        HttpGet httpGet = new HttpGet(env.getProperty("larch.server.url") + url);
        String token = getOauthToken();
        if (StringUtils.isNotBlank(token)) {
            httpGet.setHeader("Authorization", "Bearer " + token);
        }
        return httpClient().execute(httpGet);
    }

    public String doPost(String url, HttpEntity content, String contentType) throws IOException {
        HttpPost httpPost = new HttpPost(env.getProperty("larch.server.url") + url);
        if (StringUtils.isNotBlank(contentType)) {
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
        }
        httpPost.setEntity(content);
        String token = getOauthToken();
        if (StringUtils.isNotBlank(token)) {
            httpPost.setHeader("Authorization", "Bearer " + token);
        }
        HttpResponse response = httpClient().execute(httpPost);
        handleStatusCode(response);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }

    public HttpResponse doPostAsResponse(String url, HttpEntity content, String contentType) throws IOException {
        HttpPost httpPost = new HttpPost(env.getProperty("larch.server.url") + url);
        if (StringUtils.isNotBlank(contentType)) {
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
        }
        httpPost.setEntity(content);
        String token = getOauthToken();
        if (StringUtils.isNotBlank(token)) {
            httpPost.setHeader("Authorization", "Bearer " + token);
        }
        return httpClient().execute(httpPost);
    }

    public String doPost(String url, HttpEntity content, String authHeader, String contentType) throws IOException {
        HttpPost httpPost = new HttpPost(env.getProperty("larch.server.url") + url);
        if (StringUtils.isNotBlank(contentType)) {
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
        }
        httpPost.setEntity(content);
        httpPost.setHeader("Authorization", authHeader);
        HttpResponse response = httpClient().execute(httpPost);
        handleStatusCode(response);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }

    public String doPut(String url, HttpEntity content, String contentType) throws IOException {
        HttpPut httpPut = new HttpPut(env.getProperty("larch.server.url") + url);
        if (StringUtils.isNotBlank(contentType)) {
            httpPut.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
        }
        httpPut.setEntity(content);
        String token = getOauthToken();
        if (StringUtils.isNotBlank(token)) {
            httpPut.setHeader("Authorization", "Bearer " + token);
        }
        HttpResponse response = httpClient().execute(httpPut);
        handleStatusCode(response);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }

    public String doDelete(String url) throws IOException {
        HttpDelete httpDelete = new HttpDelete(env.getProperty("larch.server.url") + url);
        String token = getOauthToken();
        if (StringUtils.isNotBlank(token)) {
            httpDelete.setHeader("Authorization", "Bearer " + token);
        }
        HttpResponse response = httpClient().execute(httpDelete);
        handleStatusCode(response);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }

    public String doPatch(String url, HttpEntity content, String contentType) throws IOException {
        HttpPatch httpPatch = new HttpPatch(env.getProperty("larch.server.url") + url);
        if (StringUtils.isNotBlank(contentType)) {
            httpPatch.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
        }
        httpPatch.setEntity(content);
        String token = getOauthToken();
        if (StringUtils.isNotBlank(token)) {
            httpPatch.setHeader("Authorization", "Bearer " + token);
        }
        HttpResponse response = httpClient().execute(httpPatch);
        handleStatusCode(response);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }

    private void handleStatusCode(HttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode < 400) {
            return;
        }
        String responseStr = EntityUtils.toString(response.getEntity());
        if (StringUtils.isBlank(responseStr)) {
            throw new IOException("invalid request");
        }
        Map<String,Object> userData = mapper.readValue(responseStr, Map.class);
        String message = (String)userData.get("message");
        String exceptionClass = (String)userData.get("exception");
        Exception ex = null;
        try {
           ex = ((Exception)Class.forName(exceptionClass).getConstructor(String.class).newInstance(message));
        } catch (Exception e) {}
        if (ex != null) {
            if (ex instanceof InvalidParameterException) {
                throw (InvalidParameterException)ex;
            } else if (ex instanceof AlreadyExistsException) {
                throw (AlreadyExistsException)ex;
            } else if (ex instanceof NotFoundException) {
                throw (NotFoundException)ex;
            } else if (ex instanceof IOException) {
                throw (IOException)ex;
            } else if (ex instanceof RuntimeException) {
                throw (RuntimeException)ex;
            }
            throw new IOException(ex.getMessage());
        } else {
            throw new IOException("invalid request");
        }
    }
    
    private String getOauthToken() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession();
        if (session.getAttribute(Constants.ACCESS_TOKEN_ATTRIBUTE_NAME) != null) {
            return (String)session.getAttribute(Constants.ACCESS_TOKEN_ATTRIBUTE_NAME);
        }
        return null;
    }

    private HttpClient httpClient() {
        return HttpClients.createDefault();
    }
}
