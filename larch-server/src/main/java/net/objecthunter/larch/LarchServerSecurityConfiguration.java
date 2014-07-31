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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package net.objecthunter.larch;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.objecthunter.larch.security.helpers.LarchAuthenticationEntryPoint;

import org.apache.commons.lang3.StringUtils;
import org.openid4java.util.HttpClientFactory;
import org.openid4java.util.ProxyProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Spring-security JavaConfig class defining the security context of the larch repository
 */
@Configuration
@EnableWebMvcSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableAspectJAutoProxy
public class LarchServerSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("larchElasticSearchAuthenticationManager")
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("larchOpenIdAuthenticationProvider")
    private AuthenticationProvider openIdAuthenticationProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        configureProxy();
        http
                .exceptionHandling()
                .authenticationEntryPoint(new LarchAuthenticationEntryPoint()).and()
                .logout().logoutSuccessUrl("/")
                .and()
                .requestMatchers()
                .antMatchers("/**")
                .and()
                .anonymous()
                .authorities("ROLE_ANONYMOUS")
                .and().authorizeRequests().requestMatchers(new AntPathRequestMatcher("/oauth/authorize")).hasAnyRole(
                        "USER",
                        "ADMIN", "IDENTIFIED")
                .and().formLogin()
                .loginPage("/login-page")
                .loginProcessingUrl("/form-login")
                .and().httpBasic()
                .and()
                .authenticationProvider(openIdAuthenticationProvider)
                .openidLogin()
                .loginPage("/login-page")
                .loginProcessingUrl("/openid-login")
                .attributeExchange("https://www.google.com/.*")
                .attribute("email")
                .type("http://axschema.org/contact/email")
                .required(true)
                .and()
                .attribute("firstname")
                .type("http://axschema.org/namePerson/first")
                .required(true)
                .and()
                .attribute("lastname")
                .type("http://axschema.org/namePerson/last")
                .required(true)
                .and()
                .and()
                .attributeExchange(".*yahoo.com.*")
                .attribute("email")
                .type("http://axschema.org/contact/email")
                .required(true)
                .and()
                .attribute("fullname")
                .type("http://axschema.org/namePerson")
                .required(true)
                .and()
                .and()
                .attributeExchange(".*myopenid.com.*")
                .attribute("email")
                .type("http://schema.openid.net/contact/email")
                .required(true)
                .and()
                .attribute("fullname")
                .type("http://schema.openid.net/namePerson")
                .required(true);

        http.csrf().requireCsrfProtectionMatcher(new LarchCsrfRequestMatcher());
        if (!Boolean.valueOf(env.getProperty("larch.security.csrf.enabled", "true"))) {
            http.csrf().disable();
        }
    }

    /**
     * Configure Proxy.
     */
    private void configureProxy() {
        if (StringUtils.isNotBlank(env.getProperty("larch.proxy.name"))) {
            ProxyProperties proxyProps = new ProxyProperties();
            proxyProps.setProxyHostName(env.getProperty("larch.proxy.name"));
            if (StringUtils.isNotBlank(env.getProperty("larch.proxy.port"))) {
                proxyProps.setProxyPort(Integer.parseInt(env.getProperty("larch.proxy.port")));
            }
            HttpClientFactory.setProxyProperties(proxyProps);
        }
    }

    private class LarchCsrfRequestMatcher implements RequestMatcher {

        private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");

        @Override
        public boolean matches(HttpServletRequest request) {
            if (request.getContentType() == null) {
                return false;
            }
            if (request.getServletPath().equals("/j_spring_openid_security_check")) {
                return false;
            }
            // protect HTML forms from Cross Site forgeries using Sessions
            if (request.getContentType().equalsIgnoreCase("multipart/form-data")
                    || request.getContentType().equalsIgnoreCase("application/x-www-form-urlencoded")) {
                return true;
            }
            // everything else must authenticate and does not need a CSRF protection
            return false;
        }
    }

}
