/*
 * Copyright 2014 Michael Hoppe
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

package de.escidocng.security.helpers;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

/**
 * Checks if called method matches and additionally if request contains beareer-token
 * 
 * @author mih
 */
public class EscidocngOauthRegexRequestMatcher implements RequestMatcher {

    private final static Log logger = LogFactory.getLog(EscidocngOauthRegexRequestMatcher.class);

    private final Pattern pattern;

    private final HttpMethod httpMethod;

    /**
     * Creates a case-sensitive {@code Pattern} instance to match against the request.
     *
     * @param pattern the regular expression to compile into a pattern.
     * @param httpMethod the HTTP method to match. May be null to match all methods.
     */
    public EscidocngOauthRegexRequestMatcher(String pattern, String httpMethod) {
        this(pattern, httpMethod, false);
    }

    /**
     * As above, but allows setting of whether case-insensitive matching should be used.
     *
     * @param pattern the regular expression to compile into a pattern.
     * @param httpMethod the HTTP method to match. May be null to match all methods.
     * @param caseInsensitive if true, the pattern will be compiled with the {@link Pattern#CASE_INSENSITIVE} flag
     *        set.
     */
    public EscidocngOauthRegexRequestMatcher(String pattern, String httpMethod, boolean caseInsensitive) {
        if (caseInsensitive) {
            this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        } else {
            this.pattern = Pattern.compile(pattern);
        }
        this.httpMethod = StringUtils.hasText(httpMethod) ? HttpMethod.valueOf(httpMethod) : null;
    }

    /**
     * Performs the match of the request URL ({@code servletPath + pathInfo + queryString}) against the compiled
     * pattern. If the query string is present, a question mark will be prepended.
     *
     * @param request the request to match
     * @return true if the pattern matches the URL, false otherwise.
     */
    @Override
    public boolean matches(HttpServletRequest request) {
        if (httpMethod != null && request.getMethod() != null &&
                httpMethod != HttpMethod.valueOf(request.getMethod())) {
            return false;
        }

        String url = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String query = request.getQueryString();

        if (pathInfo != null || query != null) {
            StringBuilder sb = new StringBuilder(url);

            if (pathInfo != null) {
                sb.append(pathInfo);
            }

            if (query != null) {
                sb.append('?').append(query);
            }
            url = sb.toString();
        }

        if (pattern.matcher(url).matches()) {
            if (request.getHeader("Authorization") != null && request.getHeader("Authorization").startsWith("Bearer")) {
                return true;
            } else if (request.getHeader("Authorization") == null && request.getHeader("user-agent") != null &&
                    request.getHeader("user-agent").contains("HttpClient")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
