/**
 * 
 */
package net.objecthunter.larch.security.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;


/**
 * @author mih
 *
 */
public class AuthorizeHttpServletRequest implements HttpServletRequest {
    
    private HttpServletRequest origRequest;
    private String pathRemoval;
    
    public AuthorizeHttpServletRequest(HttpServletRequest origRequest, String pathRemoval) {
        this.origRequest = origRequest;
        this.pathRemoval = pathRemoval;
    }
    //MODIFIED/////////////////////////////////////////////////////////////////////
    @Override
    public String getServletPath() {
        if (origRequest.getServletPath() == null) {
            return null;
        }
        return origRequest.getServletPath().replaceFirst(pathRemoval, "");
    }

    @Override
    public String getPathInfo() {
        if (origRequest.getPathInfo() == null) {
            return null;
        }
        return origRequest.getPathInfo().replaceFirst(pathRemoval, "");
    }

    @Override
    public String getPathTranslated() {
        if (origRequest.getPathTranslated() == null) {
            return null;
        }
        return origRequest.getPathTranslated().replaceFirst(pathRemoval, "");
    }

    @Override
    public String getRequestURI() {
        if (origRequest.getRequestURI() == null) {
            return null;
        }
        return origRequest.getRequestURI().replaceFirst(pathRemoval, "");
    }

    @Override
    public StringBuffer getRequestURL() {
        if (origRequest.getRequestURL() == null) {
            return null;
        }
        return new StringBuffer(origRequest.getRequestURL().toString().replaceFirst(pathRemoval, ""));
    }

    @Override
    public String getHeader(String name) {
        if (name.toLowerCase().equals("accept")) {
            return null;
        }
        return origRequest.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (name.toLowerCase().equals("accept")) {
            return null;
        }
        return origRequest.getHeaders(name);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    
    //COPIED///////////////////////////////////////////////////////////////////////////////

    @Override
    public Object getAttribute(String name) {
        return origRequest.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return origRequest.getAttributeNames();
    }

    @Override
    public String getCharacterEncoding() {
        return origRequest.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
    }

    @Override
    public int getContentLength() {
        return origRequest.getContentLength();
    }

    @Override
    public String getContentType() {
        return origRequest.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return origRequest.getInputStream();
    }

    @Override
    public String getParameter(String name) {
        return origRequest.getParameter(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return origRequest.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        return origRequest.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return origRequest.getParameterMap();
    }

    @Override
    public String getProtocol() {
        return origRequest.getProtocol();
    }

    @Override
    public String getScheme() {
        return origRequest.getScheme();
    }

    @Override
    public String getServerName() {
        return origRequest.getServerName();
    }

    @Override
    public int getServerPort() {
        return origRequest.getServerPort();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return origRequest.getReader();
    }

    @Override
    public String getRemoteAddr() {
        return origRequest.getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return origRequest.getRemoteHost();
    }

    @Override
    public void setAttribute(String name, Object o) {
    }

    @Override
    public void removeAttribute(String name) {
    }

    @Override
    public Locale getLocale() {
        return origRequest.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return origRequest.getLocales();
    }

    @Override
    public boolean isSecure() {
        return origRequest.isSecure();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return origRequest.getRequestDispatcher(path);
    }

    @Override
    public String getRealPath(String path) {
        return origRequest.getRealPath(path);
    }

    @Override
    public int getRemotePort() {
        return origRequest.getRemotePort();
    }

    @Override
    public String getLocalName() {
        return origRequest.getLocalName();
    }

    @Override
    public String getLocalAddr() {
        return origRequest.getLocalAddr();
    }

    @Override
    public int getLocalPort() {
        return origRequest.getLocalPort();
    }

    @Override
    public ServletContext getServletContext() {
        return origRequest.getServletContext();
    }

    @Override
    public AsyncContext startAsync() {
        return origRequest.startAsync();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
        return origRequest.startAsync(servletRequest, servletResponse);
    }

    @Override
    public boolean isAsyncStarted() {
        return origRequest.isAsyncStarted();
    }

    @Override
    public boolean isAsyncSupported() {
        return origRequest.isAsyncSupported();
    }

    @Override
    public AsyncContext getAsyncContext() {
        return origRequest.getAsyncContext();
    }

    @Override
    public DispatcherType getDispatcherType() {
        return origRequest.getDispatcherType();
    }

    @Override
    public String getAuthType() {
        return origRequest.getAuthType();
    }

    @Override
    public Cookie[] getCookies() {
        return origRequest.getCookies();
    }

    @Override
    public long getDateHeader(String name) {
        return origRequest.getDateHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return origRequest.getHeaderNames();
    }

    @Override
    public int getIntHeader(String name) {
        return origRequest.getIntHeader(name);
    }

    @Override
    public String getMethod() {
        return origRequest.getMethod();
    }

    @Override
    public String getContextPath() {
        return origRequest.getContextPath();
    }

    @Override
    public String getQueryString() {
        return origRequest.getQueryString();
    }

    @Override
    public String getRemoteUser() {
        return origRequest.getRemoteUser();
    }

    @Override
    public boolean isUserInRole(String role) {
        return origRequest.isUserInRole(role);
    }

    @Override
    public Principal getUserPrincipal() {
        return origRequest.getUserPrincipal();
    }

    @Override
    public String getRequestedSessionId() {
        return origRequest.getRequestedSessionId();
    }

    @Override
    public HttpSession getSession(boolean create) {
        return origRequest.getSession(create);
    }

    @Override
    public HttpSession getSession() {
        return origRequest.getSession();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return origRequest.isRequestedSessionIdValid();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return origRequest.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return origRequest.isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return origRequest.isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return origRequest.authenticate(response);
    }

    @Override
    public void login(String username, String password) throws ServletException {
        origRequest.login(username, password);
    }

    @Override
    public void logout() throws ServletException {
        origRequest.logout();
    }

    @Override
    public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {
        return origRequest.getParts();
    }

    @Override
    public Part getPart(String name) throws IOException, IllegalStateException, ServletException {
        return origRequest.getPart(name);
    }

}
