/**
 * 
 */

package net.objecthunter.larch.frontend.util;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpSession;

import net.objecthunter.larch.frontend.Constants;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author mih
 */
public class HttpHelper {

    @Autowired
    private HttpClient httpClient;

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
        HttpResponse response = httpClient.execute(httpGet);
        handleStatusCode(response);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }

    public String doPost(String url, HttpEntity content) throws IOException {
        HttpPost httpPost = new HttpPost(env.getProperty("larch.server.url") + url);
        httpPost.setEntity(content);
        String token = getOauthToken();
        if (StringUtils.isNotBlank(token)) {
            httpPost.setHeader("Authorization", "Bearer " + token);
        }
        HttpResponse response = httpClient.execute(httpPost);
        handleStatusCode(response);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }

    public String doPost(String url, HttpEntity content, String authHeader) throws IOException {
        HttpPost httpPost = new HttpPost(env.getProperty("larch.server.url") + url);
        httpPost.setEntity(content);
        httpPost.setHeader("Authorization", authHeader);
        HttpResponse response = httpClient.execute(httpPost);
        handleStatusCode(response);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }

    public String doPut(String url, HttpEntity content) throws IOException {
        HttpPut httpPut = new HttpPut(env.getProperty("larch.server.url") + url);
        httpPut.setEntity(content);
        String token = getOauthToken();
        if (StringUtils.isNotBlank(token)) {
            httpPut.setHeader("Authorization", "Bearer " + token);
        }
        HttpResponse response = httpClient.execute(httpPut);
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
        HttpResponse response = httpClient.execute(httpDelete);
        handleStatusCode(response);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity);
    }

    public String doPatch(String url, HttpEntity content) throws IOException {
        HttpPatch httpPatch = new HttpPatch(env.getProperty("larch.server.url") + url);
        httpPatch.setEntity(content);
        String token = getOauthToken();
        if (StringUtils.isNotBlank(token)) {
            httpPatch.setHeader("Authorization", "Bearer " + token);
        }
        HttpResponse response = httpClient.execute(httpPatch);
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
        Map<String,Object> userData = mapper.readValue(responseStr, Map.class);
        String message = (String)userData.get("message");
        String exceptionClass = (String)userData.get("exception");
        Exception ex = null;
        try {
           ex = ((Exception)Class.forName(exceptionClass).getConstructor(String.class).newInstance(message));
        } catch (Exception e) {}
        if (ex != null) {
            throw (IOException)ex;
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

}
