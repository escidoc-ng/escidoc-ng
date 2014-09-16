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

package net.objecthunter.larch.controller;

import java.io.IOException;

import net.objecthunter.larch.annotations.Permission;
import net.objecthunter.larch.annotations.PreAuth;
import net.objecthunter.larch.model.Settings;
import net.objecthunter.larch.model.security.Role;
import net.objecthunter.larch.service.RepositoryService;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;
import net.objecthunter.larch.service.backend.BackendEntityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Web Controller responsible for Settings views
 */
@RequestMapping("/settings")
@Controller
public class SettingsController extends AbstractLarchController {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private BackendBlobstoreService blobstoreService;

    @Autowired
    private BackendEntityService entityService;

    @Autowired
    private Environment environment;

    /**
     * Retrieve the settings overview page from the repository
     * 
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, produces = "text/html")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ModelAndView retrieveHtml() throws IOException {
        return new ModelAndView("settings", new ModelMap("settings", this.retrieve()));
    }

    /**
     * Retrieve the settings response
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @PreAuth(permissions = {
            @Permission(role = Role.ADMIN) })
    public Settings retrieve() throws IOException {
        return this.createSettings();
    }

    private Settings createSettings() throws IOException {
        final Settings settings = new Settings();
        settings.setLarchState(this.repositoryService.status());
        settings.setDescribe(this.repositoryService.describe());
        settings.setIndexState(this.entityService.status());
        settings.setBlobstoreState(this.blobstoreService.status());
        settings.setLarchClusterName(environment.getProperty("larch.cluster.name"));
        settings.setLarchVersion(environment.getProperty("larch.version"));
        settings.setLarchExportEnabled(
                Boolean.parseBoolean(environment.getProperty("larch.export.auto")));
        settings.setLarchExportPath(environment.getProperty("larch.export.path"));
        settings.setLarchCsrfProtectionEnabled(
                Boolean.parseBoolean(environment.getProperty("larch.security.csrf.enabled")));
        settings.setLarchMessagingEnabled(
                Boolean.parseBoolean(environment.getProperty("larch.messaging.enabled")));
        settings.setLarchMessagingBrokerUri(environment.getProperty("larch.messaging.broker.uri"));
        settings.setLarchMessagingBrokerPath(environment.getProperty("larch.messaging.path.data"));
        settings.setLarchMailEnabled(
                Boolean.parseBoolean(environment.getProperty("larch.mail.enabled")));
        settings.setLarchMailFrom(environment.getProperty("larch.mail.from"));
        settings.setLarchMailSmtpHost(environment.getProperty("larch.mail.smtp.host"));
        settings.setLarchMailSmtpPort(
                Integer.parseInt(environment.getProperty("larch.mail.smtp.port")));
        settings.setLarchMailSmtpUser(environment.getProperty("larch.mail.smtp.user"));
        settings.setLarchMailSmtpPass(environment.getProperty("larch.mail.smtp.pass"));
        settings.setElasticSearchClusterName(environment.getProperty("elasticsearch.cluster.name"));
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
        settings.setLarchLogPath(environment.getProperty("logging.path"));
        settings.setLarchLogFile(environment.getProperty("logging.file"));
        settings.setLarchServerPort(Integer.parseInt(environment.getProperty("server.port")));
        settings.setTomcatAccessLogEnabled(
                Boolean.parseBoolean(environment.getProperty("server.tomcat.access-log-enabled")));
        settings.setJsonPrettyPrintEnabled(
                Boolean.parseBoolean(environment.getProperty("http.mappers.json-pretty-print")));
        settings.setThymeleafPrefix(environment.getProperty("spring.thymeleaf.prefix"));
        settings.setThymeleafSuffix(environment.getProperty("spring.thymeleaf.suffix"));
        settings.setThymeleafMode(environment.getProperty("spring.thymeleaf.mode"));
        settings.setThymeleafEncoding(environment.getProperty("spring.thymeleaf.encoding"));
        settings.setThymeleafCacheEnabled(
                Boolean.parseBoolean(environment.getProperty("spring.thymeleaf.cache")));
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
