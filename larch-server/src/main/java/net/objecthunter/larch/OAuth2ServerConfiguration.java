/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.objecthunter.larch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
@EnableWebMvcSecurity
public class OAuth2ServerConfiguration {

    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Autowired
        private Environment env;

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.resourceId("larch");
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.requestMatchers()
                    .regexMatchers("/((?!login-page|oauth).)*")
                    // .antMatchers("/entity/**", "/metadatatype/**", "/browse/**", "/list/**",
                    // "/describe/**",
                    // "/search/**", "/state/**", "/user/**", "/confirm/**", "/credentials/**", "/group/**")
                    .and()
                    .anonymous()
                    .authorities("ROLE_ANONYMOUS")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/none").hasRole("");

            http.csrf().requireCsrfProtectionMatcher(new LarchCsrfRequestMatcher());
            if (!Boolean.valueOf(env.getProperty("larch.security.csrf.enabled", "true"))) {
                http.csrf().disable();
            }
        }

    }

    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends
            AuthorizationServerConfigurerAdapter {

        @Autowired
        private TokenStore tokenStore;

        @Autowired
        private AuthenticationEntryPoint authenticationEntryPoint;

        @Autowired
        @Qualifier("larchElasticSearchAuthenticationManager")
        private AuthenticationManager authenticationManager;

        // @Autowired
        // @Qualifier("larchOAuth2AccessDeniedHandler")
        // OAuth2AccessDeniedHandler oauthAccessDeniedHandler;

        @Bean
        AuthenticationEntryPoint getAuthenticationEntryPoint() {
            OAuth2AuthenticationEntryPoint entryPoint = new OAuth2AuthenticationEntryPoint();
            entryPoint.setRealmName("larch");
            entryPoint.setTypeName("Bearer");
            entryPoint.setExceptionRenderer(new LarchOAuth2ExceptionRenderer());
            return entryPoint;
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients
                    .inMemory().withClient("larch_user")
                    .resourceIds("larch")
                    .authorizedGrantTypes("authorization_code", "implicit")
                    .secret("secret")
                    .scopes("read", "write")
                    .autoApprove(true)
                    .redirectUris("http://localhost:8088/oauthclient/oauth?method=token")
                    .and()
                    .withClient("larch_admin")
                    .resourceIds("larch")
                    .authorizedGrantTypes("authorization_code", "implicit")
                    .secret("secret")
                    .authorities("ROLE_ADMIN")
                    .scopes("read", "write")
                    .autoApprove(true)
                    .redirectUris("http://localhost:8088/oauthclient/oauth?method=token");
        }

        @Bean
        public TokenStore tokenStore() {
            return new InMemoryTokenStore();
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints)
                throws Exception {
            // @formatter:off
            endpoints
                    .tokenStore(tokenStore).authenticationManager(authenticationManager);
            // @formatter:on
        }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
            oauthServer.realm("larch");
            oauthServer.authenticationEntryPoint(authenticationEntryPoint);
        }

    }

}
