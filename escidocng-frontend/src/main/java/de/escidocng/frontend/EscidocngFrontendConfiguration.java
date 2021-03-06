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
package de.escidocng.frontend;

import javax.annotation.PostConstruct;
import javax.servlet.MultipartConfigElement;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

import de.escidocng.frontend.util.HttpHelper;
import de.escidocng.frontend.util.EscidocngExceptionHandler;

/**
 * General JavaConfig class for the escidocng frontend containing all the necessary beans for a escidocng frontend
 */
@Configuration
@ComponentScan(basePackages = "de.escidocng.frontend.controller")
@EnableAutoConfiguration
public class EscidocngFrontendConfiguration {

    @Autowired
    public Environment env;

    @PostConstruct
    public void init() {
        if (StringUtils.isNotBlank(env.getProperty("escidocng.proxy.name"))) {
            System.setProperty("http.proxyHost", env.getProperty("escidocng.proxy.name"));
            System.setProperty("https.proxyHost", env.getProperty("escidocng.proxy.name"));
            if (StringUtils.isNotBlank(env.getProperty("escidocng.proxy.port"))) {
                System.setProperty("http.proxyPort", env.getProperty("escidocng.proxy.port"));
                System.setProperty("https.proxyPort", env.getProperty("escidocng.proxy.port"));
            }
            if (StringUtils.isNotBlank(env.getProperty("escidocng.proxy.none"))) {
                System.setProperty("http.nonProxyHosts", env.getProperty("escidocng.proxy.none"));
                System.setProperty("https.nonProxyHosts", env.getProperty("escidocng.proxy.none"));
            }
        }
    }

    /**
     * Get a {@link org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping} Spring bean
     *
     * @return a RequestMappingHandlerMapping implementation
     */
    @Bean
    public RequestMappingHandlerMapping handlerMapping() {
        return new RequestMappingHandlerMapping();
    }

    /**
     * Get a Jackson {@link com.fasterxml.jackson.databind.ObjectMapper} Spring bean for JSON
     * serialization/deserialization
     *
     * @return the {@link com.fasterxml.jackson.databind.ObjectMapper} implementation used by various services
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JSR310Module());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * Get the {@link com.fasterxml.jackson.databind.SerializationConfig} Spring bean for the Jackson
     * {@link com.fasterxml.jackson.databind.ObjectMapper}
     *
     * @return the {@link com.fasterxml.jackson.databind.SerializationConfig} that should be used by the Jackson
     *         mapper
     */
    @Bean
    public SerializationConfig serializationConfig() {
        return objectMapper().getSerializationConfig();
    }

    /**
     * A commons-multipart {@link org.springframework.web.multipart.commons.CommonsMultipartResolver} for resolving
     * files in a HTTP multipart request
     *
     * @return a {@link org.springframework.web.multipart.commons.CommonsMultipartResolver} object used by Spring MVC
     */
    @Bean
    public CommonsMultipartResolver multipartResolver() {
        return new CommonsMultipartResolver();
    }

    /**
     * A commons-multipart {@link javax.servlet.MultipartConfigElement} for resolving
     * files in a HTTP multipart request
     *
     * @return a {@link javax.servlet.MultipartConfigElement} object used by Spring MVC
     */
    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        return factory.createMultipartConfig();
    }

    /**
     * A HttpHelper {@link de.escidocng.frontend.util.HttpHelper} for requesting urls
     *
     * @return a {@link de.escidocng.frontend.util.HttpHelper} object
     */
    @Bean
    public HttpHelper httpHelper() {
        return new HttpHelper();
    }

    /**
     * Get a {@link de.escidocng.frontend.util.EscidocngExceptionHandler} implementation for use by the repository
     *
     * @return a {@link de.escidocng.frontend.util.EscidocngExceptionHandler} implementation
     */
    @Bean
    public EscidocngExceptionHandler escidocngExceptionHandler() {
        return new EscidocngExceptionHandler();
    }

}
