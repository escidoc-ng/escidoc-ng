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


package net.objecthunter.larch;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.jms.Queue;
import javax.servlet.MultipartConfigElement;

import net.objecthunter.larch.security.helpers.LarchOpenIdAuthenticationProvider;
import net.objecthunter.larch.security.helpers.LarchSecurityInterceptor;
import net.objecthunter.larch.service.AuthorizationService;
import net.objecthunter.larch.service.ContentModelService;
import net.objecthunter.larch.service.CredentialsService;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.EntityValidatorService;
import net.objecthunter.larch.service.ExportService;
import net.objecthunter.larch.service.MailService;
import net.objecthunter.larch.service.MessagingService;
import net.objecthunter.larch.service.RepositoryService;
import net.objecthunter.larch.service.SchemaService;
import net.objecthunter.larch.service.backend.BackendArchiveBlobService;
import net.objecthunter.larch.service.backend.BackendArchiveIndexService;
import net.objecthunter.larch.service.backend.BackendArchiveInformationPackageService;
import net.objecthunter.larch.service.backend.BackendAuditService;
import net.objecthunter.larch.service.backend.BackendContentModelService;
import net.objecthunter.larch.service.backend.BackendEntityService;
import net.objecthunter.larch.service.backend.BackendSchemaService;
import net.objecthunter.larch.service.backend.BackendVersionService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchArchiveIndexService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchAuditService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchContentModelService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchCredentialsService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchNode;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchSchemaService;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchVersionService;
import net.objecthunter.larch.service.backend.fs.FileSystemArchiveService;
import net.objecthunter.larch.service.backend.fs.FileSystemBlobstoreService;
import net.objecthunter.larch.service.backend.sftp.SftpArchiveService;
import net.objecthunter.larch.service.backend.weedfs.WeedFSBlobstoreService;
import net.objecthunter.larch.service.backend.weedfs.WeedFsMaster;
import net.objecthunter.larch.service.backend.weedfs.WeedFsVolume;
import net.objecthunter.larch.service.backend.zip.ZIPArchiveInformationPackageService;
import net.objecthunter.larch.service.impl.DefaultArchiveService;
import net.objecthunter.larch.service.impl.DefaultAuthorizationService;
import net.objecthunter.larch.service.impl.DefaultContentModelService;
import net.objecthunter.larch.service.impl.DefaultCredentialsService;
import net.objecthunter.larch.service.impl.DefaultEntityService;
import net.objecthunter.larch.service.impl.DefaultEntityValidatorService;
import net.objecthunter.larch.service.impl.DefaultExportService;
import net.objecthunter.larch.service.impl.DefaultMailService;
import net.objecthunter.larch.service.impl.DefaultMessagingService;
import net.objecthunter.larch.service.impl.DefaultRepositoryService;
import net.objecthunter.larch.service.impl.DefaultSchemaService;
import net.objecthunter.larch.util.FileSystemUtil;
import net.objecthunter.larch.util.LarchExceptionHandler;
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

/**
 * General JavaConfig class for the larch repository containing all the necessary beans for a larch repository
 */
@Configuration
@ComponentScan(basePackages = "net.objecthunter.larch.controller")
@EnableAutoConfiguration
@EnableAspectJAutoProxy
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class LarchServerConfiguration {

    @Autowired
    public Environment env;

    @PostConstruct
    public void init() {
        if (StringUtils.isNotBlank(env.getProperty("larch.proxy.name"))) {
            System.setProperty("http.proxyHost", env.getProperty("larch.proxy.name"));
            System.setProperty("https.proxyHost", env.getProperty("larch.proxy.name"));
            if (StringUtils.isNotBlank(env.getProperty("larch.proxy.port"))) {
                System.setProperty("http.proxyPort", env.getProperty("larch.proxy.port"));
                System.setProperty("https.proxyPort", env.getProperty("larch.proxy.port"));
            }
            if (StringUtils.isNotBlank(env.getProperty("larch.proxy.none"))) {
                System.setProperty("http.nonProxyHosts", env.getProperty("larch.proxy.none"));
                System.setProperty("https.nonProxyHosts", env.getProperty("larch.proxy.none"));
            }
        }
    }

    /**
     * Get a {@link net.objecthunter.larch.service.impl.DefaultEntityService} Spring bean
     *
     * @return the {@link net.objecthunter.larch.service.impl.DefaultEntityService} implementation
     */
    @Bean
    public EntityService entityService() {
        return new DefaultEntityService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.impl.DefaultContentModelService} Spring bean
     *
     * @return the {@link net.objecthunter.larch.service.impl.DefaultContentModelService} implementation
     */
    @Bean
    public ContentModelService contentModelService() {
        return new DefaultContentModelService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.impl.DefaultCredentialsService} Spring bean
     *
     * @return the {@link net.objecthunter.larch.service.impl.DefaultCredentialsService} implementation
     */
    @Bean
    public CredentialsService credentialsService() {
        return new DefaultCredentialsService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.impl.DefaultSchemaService} Spring bean
     *
     * @return the {@link net.objecthunter.larch.service.impl.DefaultSchemaService} implementation
     */
    @Bean
    public SchemaService schemaService() {
        return new DefaultSchemaService();
    }

    /**
     * Get the {@link net.objecthunter.larch.service.impl.DefaultRepositoryService} Spring bean
     *
     * @return the {@link net.objecthunter.larch.service.impl.DefaultRepositoryService} implementation
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
     * Get the {@link net.objecthunter.larch.service.ExportService} Spring bean
     *
     * @return a {@link net.objecthunter.larch.service.ExportService} implementation to be used by the repository
     */
    @Bean
    public ExportService exportService() {
        return new DefaultExportService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.impl.DefaultEntityValidatorService} Spring bean
     *
     * @return the {@link net.objecthunter.larch.service.impl.DefaultEntityValidatorService} implementation
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
     * Get a {@link net.objecthunter.larch.service.backend.BackendAuditService} Spring bean
     *
     * @return the {@link net.objecthunter.larch.service.backend.BackendAuditService} implementation
     */
    @Bean
    public BackendAuditService backendAuditService() {
        return new ElasticSearchAuditService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.BackendSchemaService} Spring bean
     *
     * @return the {@link net.objecthunter.larch.service.backend.BackendSchemaService} implementation
     */
    @Bean
    public BackendSchemaService backendSchemaService() {
        return new ElasticSearchSchemaService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.BackendEntityService} implementation Spring bean
     *
     * @return a {@link net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService}
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
     * Get a {@link net.objecthunter.larch.service.backend.BackendVersionService} Spring bean
     *
     * @return a BackendVersionService implementation
     */
    @Bean
    public BackendVersionService backendVersionService() {
        return new ElasticSearchVersionService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.BackendContentModelService} Spring bean
     *
     * @return a BackendContentModelService implementation
     */
    @Bean
    public BackendContentModelService backendContentService() {
        return new ElasticSearchContentModelService();
    }

    /**
     * Get a {@link net.objecthunter.larch.security.helpers.LarchOpenIdAuthenticationProvider} Spring bean
     *
     * @return a AuthenticationProvider implementation
     */
    @Bean
    public LarchOpenIdAuthenticationProvider larchOpenIdAuthenticationProvider() {
        return new LarchOpenIdAuthenticationProvider();
    }

    /**
     * Get {@link net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchNode} Spring bean responsible for
     * starting and stopping the ElasticSearch services
     *
     * @return the {@link net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchNode} object
     */
    @Bean
    public ElasticSearchNode elasticSearchNode() {
        return new ElasticSearchNode();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.fs.FileSystemBlobstoreService} implementation for usage as
     * a {@link net.objecthunter.larch.service.backend.BackendBlobstoreService} in the repository
     *
     * @return the {@link net.objecthunter.larch.service.backend.fs.FileSystemBlobstoreService} implementation
     */
    @Bean
    @Profile("blobstore-fs")
    public FileSystemBlobstoreService fileSystemBlobstoreService() {
        return new FileSystemBlobstoreService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.weedfs.WeedFsMaster} object responsible for starting and
     * stopping the Weed FS master node
     *
     * @return the {@link net.objecthunter.larch.service.backend.weedfs.WeedFsMaster} object
     */
    @Bean
    @Profile("blobstore-weedfs")
    @Order(40)
    public WeedFsMaster weedFsMaster() {
        return new WeedFsMaster();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.weedfs.WeedFsVolume} object responsible for starting and
     * stopping a Weed FS volume node
     *
     * @return the {@link net.objecthunter.larch.service.backend.weedfs.WeedFsVolume} object
     */
    @Bean
    @Profile("blobstore-weedfs")
    @Order(50)
    public WeedFsVolume weedfsVolume() {
        return new WeedFsVolume();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.weedfs.WeedFSBlobstoreService} implementation as the
     * {@link net.objecthunter.larch.service.backend.BackendBlobstoreService} fro the repository
     *
     * @return the {@link net.objecthunter.larch.service.backend.weedfs.WeedFSBlobstoreService} object
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
     * Get a {@link net.objecthunter.larch.security.helpers.LarchSecurityInterceptor} implementation for use by the
     * repository
     *
     * @return a {@link net.objecthunter.larch.security.helpers.LarchSecurityInterceptor} implementation
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 7)
    public LarchSecurityInterceptor larchSecurityInterceptor() {
        return Aspects.aspectOf(LarchSecurityInterceptor.class);
    }

    /**
     * The Spring-security JavaConfig class containing the relevan AuthZ/AuthN definitions
     *
     * @return the {@link LarchServerSecurityConfiguration} used
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 8)
    public LarchServerSecurityConfiguration larchServerSecurityConfiguration() {
        return new LarchServerSecurityConfiguration();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.BackendCredentialsService} implementation for use by the
     * repository
     *
     * @return a {@link net.objecthunter.larch.service.backend.BackendCredentialsService} implementation
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 9)
    public ElasticSearchCredentialsService larchElasticSearchAuthenticationManager() {
        return new ElasticSearchCredentialsService();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 10)
    public AuthorizationService authorizationService() {
        return new DefaultAuthorizationService();
    }

    /**
     * Get a {@link net.objecthunter.larch.util.LarchExceptionHandler} implementation for use by the repository
     *
     * @return a {@link net.objecthunter.larch.util.LarchExceptionHandler} implementation
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 11)
    public LarchExceptionHandler larchExceptionHandler() {
        return new LarchExceptionHandler();
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
                new File(env.getProperty("larch.messaging.path.data", System.getProperty("java.io.tmpdir")
                        + "/larch-jms-data"));
        FileSystemUtil.checkAndCreate(dir);
        final BrokerService broker = new BrokerService();
        broker.addConnector(brokerUri());
        broker.getPersistenceAdapter().setDirectory(dir);
        broker.start();
        return broker;
    }

    @Bean
    public Queue jmsQueue() {
        return new ActiveMQQueue("larch");
    }

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory() {
        return new ActiveMQConnectionFactory(brokerUri());
    }

    @Bean
    public String brokerUri() {
        return env.getProperty("larch.messaging.broker.uri", "vm://localhost");
    }

    @Bean
    public MessagingService messagingService() {
        return new DefaultMessagingService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.fs.FileSystemArchiveService} implementation for usage as
     * a {@link net.objecthunter.larch.service.backend.BackendArchiveBlobService} in the repository
     *
     * @return the {@link net.objecthunter.larch.service.backend.fs.FileSystemArchiveService} implementation
     */
    @Bean
    @Profile("archive-fs")
    public BackendArchiveBlobService fileSystemArchiveService() {
        return new FileSystemArchiveService();
    }

    /**
     * Get a {@link net.objecthunter.larch.service.backend.sftp.SftpArchiveService} implementation as the
     * {@link net.objecthunter.larch.service.backend.BackendArchiveBlobService} for the repository
     *
     * @return the {@link net.objecthunter.larch.service.backend.sftp.SftpArchiveService} object
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
