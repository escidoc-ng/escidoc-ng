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

package de.escidocng;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder.ClientBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.openid.OpenIDAuthenticationFilter;

import de.escidocng.security.helpers.EscidocngOauthRegexRequestMatcher;

@Configuration
public class OAuth2ServerConfiguration {

    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.resourceId("escidocng");
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                    .requestMatchers()
                    .regexMatchers("/((?!login|oauth).)*")
                    .requestMatchers(new EscidocngOauthRegexRequestMatcher("/((?!login|oauth).)*", null))
                    .and()
                    .anonymous()
                    .authorities("ROLE_ANONYMOUS")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/none").hasRole("")
                    .and().addFilter(new OpenIDAuthenticationFilter());
        }

    }

    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends
            AuthorizationServerConfigurerAdapter {

        @Autowired
        private TokenStore tokenStore;

        @Autowired
        private Environment env;

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            String clientsConf = env.getProperty("escidocng.oauth.clients");
            if (clientsConf != null) {
                String[] clientConfs = clientsConf.split("\\s*\\|\\s*");
                if (clientsConf != null) {
                    ClientBuilder clientBuilder = null;
                    for (int i = 0; i < clientConfs.length; i++) {
                        if (clientConfs[i] != null) {
                            String[] clientConf = clientConfs[i].split("\\s*,\\s*");
                            if (clientConf != null && clientConf.length == 2) {
                                String redirectUris =
                                        env.getProperty("escidocng.oauth.redirectUris." + clientConf[0]);
                                String[] redirectUrisArr = new String[0];
                                if (redirectUris != null) {
                                    redirectUrisArr = redirectUris.split("\\s*\\|\\s*");
                                }
                                if (clientBuilder == null) {
                                    clientBuilder = clients
                                            .inMemory()
                                            .withClient(clientConf[0]);
                                } else {
                                    clientBuilder = clientBuilder.and().withClient(clientConf[0]);
                                }
                                clientBuilder = clientBuilder.resourceIds("escidocng")
                                        .authorizedGrantTypes("authorization_code", "implicit")
                                        .secret(clientConf[1])
                                        .authorities("ROLE_ADMIN")
                                        .scopes("read", "write")
                                        .autoApprove(true)
                                        .redirectUris(redirectUrisArr);
                            }
                        }
                    }
                }
            }
        }

        @Bean
        public TokenStore tokenStore() {
            return new InMemoryTokenStore();
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints)
                throws Exception {
            endpoints.tokenStore(tokenStore);
        }

    }

}
