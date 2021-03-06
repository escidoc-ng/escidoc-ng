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


package de.escidocng.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import de.escidocng.model.Settings;
import de.escidocng.model.security.annotation.Permission;
import de.escidocng.model.security.annotation.PreAuth;
import de.escidocng.model.security.role.Role.RoleName;
import de.escidocng.service.RepositoryService;
import de.escidocng.service.backend.BackendBlobstoreService;
import de.escidocng.service.backend.BackendEntityService;

/**
 * Web Controller responsible for Settings views.
 */
@RequestMapping("/settings")
@Controller
public class SettingsController extends AbstractEscidocngController {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private BackendBlobstoreService blobstoreService;

    @Autowired
    private BackendEntityService entityService;

    @Autowired
    private Environment environment;

    /**
     * Controller method for retrieval of a JSON representation of the 
     * {@link de.escidocng.model.Settings}.
     * Settings contain all configuration-parameters of the system.
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(permissions = {
            @Permission(rolename = RoleName.ROLE_ADMIN) })
    public Settings retrieve() throws IOException {
        return this.createSettings();
    }

    /**
     * Create Settings-Object.
     * 
     * @return Settings settings
     * @throws IOException
     */
    private Settings createSettings() throws IOException {
        final Settings settings = new Settings();
        settings.setEscidocngState(this.repositoryService.status());
        settings.setDescribe(this.repositoryService.describe());
        settings.setIndexState(this.entityService.status());
        settings.setBlobstoreState(this.blobstoreService.status());
        settings.setEscidocngClusterName(environment.getProperty("escidocng.cluster.name"));
        settings.setEscidocngVersion(environment.getProperty("escidocng.version"));
        settings.setEscidocngExportEnabled(
                Boolean.parseBoolean(environment.getProperty("escidocng.export.auto")));
        settings.setEscidocngExportPath(environment.getProperty("escidocng.export.path"));
        settings.setEscidocngCsrfProtectionEnabled(
                Boolean.parseBoolean(environment.getProperty("escidocng.security.csrf.enabled")));
        settings.setEscidocngMessagingEnabled(
                Boolean.parseBoolean(environment.getProperty("escidocng.messaging.enabled")));
        settings.setEscidocngMessagingBrokerUri(environment.getProperty("escidocng.messaging.broker.uri"));
        settings.setEscidocngMessagingBrokerPath(environment.getProperty("escidocng.messaging.path.data"));
        settings.setEscidocngMailEnabled(
                Boolean.parseBoolean(environment.getProperty("escidocng.mail.enabled")));
        settings.setEscidocngMailFrom(environment.getProperty("escidocng.mail.from"));
        settings.setEscidocngMailSmtpHost(environment.getProperty("escidocng.mail.smtp.host"));
        settings.setEscidocngMailSmtpPort(
                Integer.parseInt(environment.getProperty("escidocng.mail.smtp.port")));
        settings.setEscidocngMailSmtpUser(environment.getProperty("escidocng.mail.smtp.user"));
        settings.setEscidocngMailSmtpPass(environment.getProperty("escidocng.mail.smtp.pass"));
        settings.setElasticSearchClusterName(environment.getProperty("elasticsearch.escidocng.cluster.name"));
        settings.setElasticSearchLogPath(environment.getProperty("elasticsearch.path.logs"));
        settings.setElasticSearchDataPath(environment.getProperty("elasticsearch.path.data"));
        settings.setElasticSearchBootstrapMlockAll(
                environment.getProperty("elasticsearch.bootstrap.mlockall"));
        settings.setElasticSearchBindHost(
                environment.getProperty("elasticsearch.network.bind.host"));
        settings.setElasticSearchExpectedNodes(
                Integer.parseInt(environment.getProperty("elasticsearch.gateway.expected_nodes")));
        settings.setElasticSearchHttpPort(
                Integer.parseInt(environment.getProperty("elasticsearch.http.port")));
        settings.setElasticSearchHttpEnabled(
                Boolean.parseBoolean(environment.getProperty("elasticsearch.http.enabled")));
        settings.setElasticSearchGatewayType(environment.getProperty("elasticsearch.gateway.type"));
        settings.setElasticSearchConfigPath(environment.getProperty("elasticsearch.config.path"));
        settings.setSpringActiveProfile(environment.getProperty("spring.profiles.active"));
        settings.setSpringShowBanner(environment.getProperty("spring.main.show-banner"));
        settings.setEscidocngLogPath(environment.getProperty("logging.path"));
        settings.setEscidocngLogFile(environment.getProperty("logging.file"));
        settings.setEscidocngServerPort(Integer.parseInt(environment.getProperty("server.port")));
        settings.setTomcatAccessLogEnabled(
                Boolean.parseBoolean(environment.getProperty("server.tomcat.access-log-enabled")));
        settings.setJsonPrettyPrintEnabled(
                Boolean.parseBoolean(environment.getProperty("http.mappers.json-pretty-print")));
        settings.setSpringJmxEnabled(
                Boolean.parseBoolean(environment.getProperty("spring.jmx.enabled")));
        settings.setSpringEndpointAutoconfigEnabled(
                Boolean.parseBoolean(environment.getProperty("endpoints.autoconfig.enabled")));
        settings.setSpringEndpointBeansEnabled(
                Boolean.parseBoolean(environment.getProperty("endpoints.beans.enabled")));
        settings.setSpringEndpointConfigPropsEnabled(
                Boolean.parseBoolean(environment.getProperty("endpoints.configprops.enabled")));
        settings.setSpringEndpointDumpEnabled(
                Boolean.parseBoolean(environment.getProperty("endpoints.dump.enabled")));
        settings.setSpringEndpointEnvEnabled(
                Boolean.parseBoolean(environment.getProperty("endpoints.env.enabled")));
        settings.setSpringEndpointHealthEnabled(Boolean.parseBoolean(environment
                .getProperty("endpoints.health.enabled")));
        settings.setSpringEndpointInfoEnabled(Boolean.parseBoolean(environment.getProperty("endpoints.info.enabled")));
        settings.setSpringEndpointMetricsEnabled(Boolean.parseBoolean(environment
                .getProperty("endpoints.metrics.enabled")));
        settings.setSpringEndpointShutdownEnabled(Boolean.parseBoolean(environment
                .getProperty("endpoints.shutdown.enabled")));
        settings.setSpringEndpointTraceEnabled(Boolean.parseBoolean(environment
                .getProperty("endpoints.trace.enabled")));
        settings.setSpringEndpointJolokiaEnabled(Boolean.parseBoolean(environment
                .getProperty("endpoints.jolokia.enabled")));
        settings.setSpringEndpointJMXEnabled(Boolean.parseBoolean(environment.getProperty("endpoints.jmx.enabled")));
        settings.setSpringShellEnabled(Boolean.parseBoolean(environment.getProperty("shell.ssh.enabled")));
        settings.setSpringShellPathPatterns(environment.getProperty("shell.commandPathPatterns"));
        return settings;
    }
}
