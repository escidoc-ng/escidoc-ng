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

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.jms.Queue;
import javax.servlet.MultipartConfigElement;

import net.sf.json.xml.XMLSerializer;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.Aspects;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

import de.escidocng.security.helpers.EscidocngOpenIdAuthenticationProvider;
import de.escidocng.security.helpers.EscidocngSecurityInterceptor;
import de.escidocng.service.AuthorizationService;
import de.escidocng.service.ContentModelService;
import de.escidocng.service.CredentialsService;
import de.escidocng.service.EntityService;
import de.escidocng.service.EntityValidatorService;
import de.escidocng.service.ExportService;
import de.escidocng.service.MailService;
import de.escidocng.service.MessagingService;
import de.escidocng.service.RepositoryService;
import de.escidocng.service.SchemaService;
import de.escidocng.service.backend.BackendArchiveBlobService;
import de.escidocng.service.backend.BackendArchiveIndexService;
import de.escidocng.service.backend.BackendArchiveInformationPackageService;
import de.escidocng.service.backend.BackendAuditService;
import de.escidocng.service.backend.BackendContentModelService;
import de.escidocng.service.backend.BackendEntityService;
import de.escidocng.service.backend.BackendSchemaService;
import de.escidocng.service.backend.BackendVersionService;
import de.escidocng.service.backend.elasticsearch.ElasticSearchArchiveIndexService;
import de.escidocng.service.backend.elasticsearch.ElasticSearchAuditService;
import de.escidocng.service.backend.elasticsearch.ElasticSearchContentModelService;
import de.escidocng.service.backend.elasticsearch.ElasticSearchCredentialsService;
import de.escidocng.service.backend.elasticsearch.ElasticSearchEntityService;
import de.escidocng.service.backend.elasticsearch.ElasticSearchNode;
import de.escidocng.service.backend.elasticsearch.ElasticSearchSchemaService;
import de.escidocng.service.backend.elasticsearch.ElasticSearchVersionService;
import de.escidocng.service.backend.fs.FileSystemArchiveService;
import de.escidocng.service.backend.fs.FileSystemBlobstoreService;
import de.escidocng.service.backend.sftp.SftpArchiveService;
import de.escidocng.service.backend.weedfs.WeedFSBlobstoreService;
import de.escidocng.service.backend.weedfs.WeedFsMaster;
import de.escidocng.service.backend.weedfs.WeedFsVolume;
import de.escidocng.service.backend.zip.ZIPArchiveInformationPackageService;
import de.escidocng.service.impl.DefaultArchiveService;
import de.escidocng.service.impl.DefaultAuthorizationService;
import de.escidocng.service.impl.DefaultContentModelService;
import de.escidocng.service.impl.DefaultCredentialsService;
import de.escidocng.service.impl.DefaultEntityService;
import de.escidocng.service.impl.DefaultEntityValidatorService;
import de.escidocng.service.impl.DefaultExportService;
import de.escidocng.service.impl.DefaultMailService;
import de.escidocng.service.impl.DefaultMessagingService;
import de.escidocng.service.impl.DefaultRepositoryService;
import de.escidocng.service.impl.DefaultSchemaService;
import de.escidocng.util.FileSystemUtil;
import de.escidocng.util.EscidocngExceptionHandler;

/**
 * General JavaConfig class for the escidocng repository containing all the necessary beans for a escidocng repository
 */
@Configuration
@ComponentScan(basePackages = "de.escidocng.controller")
@EnableAutoConfiguration
@EnableAspectJAutoProxy
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class EscidocngServerConfiguration {

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
     * Get a {@link de.escidocng.service.impl.DefaultEntityService} Spring bean
     *
     * @return the {@link de.escidocng.service.impl.DefaultEntityService} implementation
     */
    @Bean
    public EntityService entityService() {
        return new DefaultEntityService();
    }

    /**
     * Get a {@link de.escidocng.service.impl.DefaultContentModelService} Spring bean
     *
     * @return the {@link de.escidocng.service.impl.DefaultContentModelService} implementation
     */
    @Bean
    public ContentModelService contentModelService() {
        return new DefaultContentModelService();
    }

    /**
     * Get a {@link de.escidocng.service.impl.DefaultCredentialsService} Spring bean
     *
     * @return the {@link de.escidocng.service.impl.DefaultCredentialsService} implementation
     */
    @Bean
    public CredentialsService credentialsService() {
        return new DefaultCredentialsService();
    }

    /**
     * Get a {@link de.escidocng.service.impl.DefaultSchemaService} Spring bean
     *
     * @return the {@link de.escidocng.service.impl.DefaultSchemaService} implementation
     */
    @Bean
    public SchemaService schemaService() {
        return new DefaultSchemaService();
    }

    /**
     * Get the {@link de.escidocng.service.impl.DefaultRepositoryService} Spring bean
     *
     * @return the {@link de.escidocng.service.impl.DefaultRepositoryService} implementation
     */
    @Bean
    public RepositoryService repositoryService() {
        return new DefaultRepositoryService();
    }

    @Bean
    public MailService mailService() {
        return new DefaultMailService();
    }

    /**
     * Get the {@link de.escidocng.service.ExportService} Spring bean
     *
     * @return a {@link de.escidocng.service.ExportService} implementation to be used by the repository
     */
    @Bean
    public ExportService exportService() {
        return new DefaultExportService();
    }

    /**
     * Get a {@link de.escidocng.service.impl.DefaultEntityValidatorService} Spring bean
     *
     * @return the {@link de.escidocng.service.impl.DefaultEntityValidatorService} implementation
     */
    @Bean
    public EntityValidatorService entityValidatorService() {
        return new DefaultEntityValidatorService();
    }

    /**
     * Get a ElasticSearch {@link org.elasticsearch.client.Client} Spring bean
     *
     * @return the {@link org.elasticsearch.client.Client} implementation
     */
    @Bean
    public Client elasticSearchClient() {
        return this.elasticSearchNode().getClient();
    }

    /**
     * Get a {@link de.escidocng.service.backend.BackendAuditService} Spring bean
     *
     * @return the {@link de.escidocng.service.backend.BackendAuditService} implementation
     */
    @Bean
    public BackendAuditService backendAuditService() {
        return new ElasticSearchAuditService();
    }

    /**
     * Get a {@link de.escidocng.service.backend.BackendSchemaService} Spring bean
     *
     * @return the {@link de.escidocng.service.backend.BackendSchemaService} implementation
     */
    @Bean
    public BackendSchemaService backendSchemaService() {
        return new ElasticSearchSchemaService();
    }

    /**
     * Get a {@link de.escidocng.service.backend.BackendEntityService} implementation Spring bean
     *
     * @return a {@link de.escidocng.service.backend.elasticsearch.ElasticSearchEntityService}
     *         implementation
     */
    @Bean
    public BackendEntityService elasticSearchIndexService() {
        return new ElasticSearchEntityService();
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
     * Get a {@link de.escidocng.service.backend.BackendVersionService} Spring bean
     *
     * @return a BackendVersionService implementation
     */
    @Bean
    public BackendVersionService backendVersionService() {
        return new ElasticSearchVersionService();
    }

    /**
     * Get a {@link de.escidocng.service.backend.BackendContentModelService} Spring bean
     *
     * @return a BackendContentModelService implementation
     */
    @Bean
    public BackendContentModelService backendContentService() {
        return new ElasticSearchContentModelService();
    }

    /**
     * Get a {@link de.escidocng.security.helpers.EscidocngOpenIdAuthenticationProvider} Spring bean
     *
     * @return a AuthenticationProvider implementation
     */
    @Bean
    public EscidocngOpenIdAuthenticationProvider escidocngOpenIdAuthenticationProvider() {
        return new EscidocngOpenIdAuthenticationProvider();
    }

    /**
     * Get {@link de.escidocng.service.backend.elasticsearch.ElasticSearchNode} Spring bean responsible for
     * starting and stopping the ElasticSearch services
     *
     * @return the {@link de.escidocng.service.backend.elasticsearch.ElasticSearchNode} object
     */
    @Bean
    public ElasticSearchNode elasticSearchNode() {
        return new ElasticSearchNode();
    }

    /**
     * Get a {@link de.escidocng.service.backend.fs.FileSystemBlobstoreService} implementation for usage as
     * a {@link de.escidocng.service.backend.BackendBlobstoreService} in the repository
     *
     * @return the {@link de.escidocng.service.backend.fs.FileSystemBlobstoreService} implementation
     */
    @Bean
    @Profile("blobstore-fs")
    public FileSystemBlobstoreService fileSystemBlobstoreService() {
        return new FileSystemBlobstoreService();
    }

    /**
     * Get a {@link de.escidocng.service.backend.weedfs.WeedFsMaster} object responsible for starting and
     * stopping the Weed FS master node
     *
     * @return the {@link de.escidocng.service.backend.weedfs.WeedFsMaster} object
     */
    @Bean
    @Profile("blobstore-weedfs")
    @Order(40)
    public WeedFsMaster weedFsMaster() {
        return new WeedFsMaster();
    }

    /**
     * Get a {@link de.escidocng.service.backend.weedfs.WeedFsVolume} object responsible for starting and
     * stopping a Weed FS volume node
     *
     * @return the {@link de.escidocng.service.backend.weedfs.WeedFsVolume} object
     */
    @Bean
    @Profile("blobstore-weedfs")
    @Order(50)
    public WeedFsVolume weedfsVolume() {
        return new WeedFsVolume();
    }

    /**
     * Get a {@link de.escidocng.service.backend.weedfs.WeedFSBlobstoreService} implementation as the
     * {@link de.escidocng.service.backend.BackendBlobstoreService} fro the repository
     *
     * @return the {@link de.escidocng.service.backend.weedfs.WeedFSBlobstoreService} object
     */
    @Bean
    @Profile("blobstore-weedfs")
    public WeedFSBlobstoreService weedFSBlobstoreService() {
        return new WeedFSBlobstoreService();
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
     * A commons-multipart {@link javax.servlet.MultipartConfigElement} for resolving files in a HTTP multipart
     * request
     *
     * @return a {@link javax.servlet.MultipartConfigElement} object used by Spring MVC
     */
    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        return factory.createMultipartConfig();
    }

    /**
     * An {@link net.sf.json.xml.XMLSerializer} for converting XML into JSON
     *
     * @return a {@link net.sf.json.xml.XMLSerializer} object used by Spring MVC
     */
    @Bean
    XMLSerializer xmlSerializer() {
        XMLSerializer serializer = new XMLSerializer();
        serializer.setRemoveNamespacePrefixFromElements(true);
        serializer.setForceTopLevelObject(true);
        serializer.setSkipNamespaces(true);
        return serializer;
    }

    /**
     * Get a {@link de.escidocng.security.helpers.EscidocngSecurityInterceptor} implementation for use by the
     * repository
     *
     * @return a {@link de.escidocng.security.helpers.EscidocngSecurityInterceptor} implementation
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 7)
    public EscidocngSecurityInterceptor escidocngSecurityInterceptor() {
        return Aspects.aspectOf(EscidocngSecurityInterceptor.class);
    }

    /**
     * The Spring-security JavaConfig class containing the relevan AuthZ/AuthN definitions
     *
     * @return the {@link EscidocngServerSecurityConfiguration} used
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 8)
    public EscidocngServerSecurityConfiguration escidocngServerSecurityConfiguration() {
        return new EscidocngServerSecurityConfiguration();
    }

    /**
     * Get a {@link de.escidocng.service.backend.BackendCredentialsService} implementation for use by the
     * repository
     *
     * @return a {@link de.escidocng.service.backend.BackendCredentialsService} implementation
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 9)
    public ElasticSearchCredentialsService escidocngElasticSearchAuthenticationManager() {
        return new ElasticSearchCredentialsService();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 10)
    public AuthorizationService authorizationService() {
        return new DefaultAuthorizationService();
    }

    /**
     * Get a {@link de.escidocng.util.EscidocngExceptionHandler} implementation for use by the repository
     *
     * @return a {@link de.escidocng.util.EscidocngExceptionHandler} implementation
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 11)
    public EscidocngExceptionHandler escidocngExceptionHandler() {
        return new EscidocngExceptionHandler();
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        final JmsTemplate templ = new JmsTemplate(activeMQConnectionFactory());
        templ.setReceiveTimeout(500);
        templ.setDefaultDestination(jmsQueue());
        return templ;
    }

    @Bean
    public BrokerService brokerService() throws Exception {
        final File dir =
                new File(env.getProperty("escidocng.messaging.path.data", System.getProperty("java.io.tmpdir")
                        + "/escidocng-jms-data"));
        FileSystemUtil.checkAndCreate(dir);
        final BrokerService broker = new BrokerService();
        try {
            broker.addConnector(brokerUri());
            broker.getPersistenceAdapter().setDirectory(dir);
            broker.start();
            return broker;
        } catch (IOException e) {
            // broker is already bound, we can ignore it...
            return null;
        }
    }

    @Bean
    public Queue jmsQueue() {
        return new ActiveMQQueue("escidocng");
    }

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory() {
        return new ActiveMQConnectionFactory(brokerUri());
    }

    @Bean
    public String brokerUri() {
        return env.getProperty("escidocng.messaging.broker.uri", "vm://localhost");
    }

    @Bean
    public MessagingService messagingService() {
        return new DefaultMessagingService();
    }

    /**
     * Get a {@link de.escidocng.service.backend.fs.FileSystemArchiveService} implementation for usage as
     * a {@link de.escidocng.service.backend.BackendArchiveBlobService} in the repository
     *
     * @return the {@link de.escidocng.service.backend.fs.FileSystemArchiveService} implementation
     */
    @Bean
    @Profile("archive-fs")
    public BackendArchiveBlobService fileSystemArchiveService() {
        return new FileSystemArchiveService();
    }

    /**
     * Get a {@link de.escidocng.service.backend.sftp.SftpArchiveService} implementation as the
     * {@link de.escidocng.service.backend.BackendArchiveBlobService} for the repository
     *
     * @return the {@link de.escidocng.service.backend.sftp.SftpArchiveService} object
     */
    @Bean
    @Profile("archive-sftp")
    public BackendArchiveBlobService sftpArchiveService() {
        return new SftpArchiveService();
    }

    @Bean
    public BackendArchiveIndexService backendArchiveIndexService() {
        return new ElasticSearchArchiveIndexService();
    }

    @Bean
    public DefaultArchiveService defaultArchiveService() {
        return new DefaultArchiveService();
    }

    @Bean
    public BackendArchiveInformationPackageService backendArchiveInformationPackageService() {
        return new ZIPArchiveInformationPackageService();
    }
}
