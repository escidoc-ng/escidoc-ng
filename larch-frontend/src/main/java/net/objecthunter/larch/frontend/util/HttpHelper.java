/**
 * 
 */
package net.objecthunter.larch.frontend.util;

import java.net.URL;

import org.apache.http.client.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;


/**
 * @author mih
 *
 */
public class HttpHelper {
    
    @Autowired
    private HttpClient httpClient;

    @Autowired
    private Environment env;
    
    public String doGet(URL url) {
//        httpClient.;
        return null;
    }


}
