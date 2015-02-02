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

package de.escidocng.model;

import de.escidocng.model.state.BlobstoreState;
import de.escidocng.model.state.IndexState;
import de.escidocng.model.state.EscidocngState;

public class Settings {

    private BlobstoreState blobstoreState;

    private IndexState indexState;

    private EscidocngState escidocngState;

    private Describe describe;

    private String escidocngClusterName;

    private String escidocngVersion;

    private boolean escidocngExportEnabled;

    private String escidocngExportPath;

    private boolean escidocngCsrfProtectionEnabled;

    private boolean escidocngMessagingEnabled;

    private String escidocngMessagingBrokerUri;

    private String escidocngMessagingBrokerPath;

    private boolean escidocngMailEnabled;

    private String escidocngMailFrom;

    private String escidocngMailSmtpHost;

    private int escidocngMailSmtpPort;

    private String escidocngMailSmtpUser;

    private String escidocngMailSmtpPass;

    private String elasticSearchClusterName;

    private String elasticSearchLogPath;

    private String elasticSearchDataPath;

    private String elasticSearchBootstrapMlockAll;

    private String elasticSearchBindHost;

    private int elasticSearchExpectedNodes;

    private int elasticSearchHttpPort;

    private boolean elasticSearchHttpEnabled;

    private String elasticSearchGatewayType;

    private String elasticSearchConfigPath;

    private String springActiveProfile;

    private String springShowBanner;

    private String escidocngLogPath;

    private String escidocngLogFile;

    private int escidocngServerPort;

    private boolean tomcatAccessLogEnabled;

    private boolean jsonPrettyPrintEnabled;

    private boolean springJmxEnabled;

    private boolean springEndpointAutoconfigEnabled;

    private boolean springEndpointBeansEnabled;

    private boolean springEndpointConfigPropsEnabled;

    private boolean springEndpointDumpEnabled;

    private boolean springEndpointEnvEnabled;

    private boolean springEndpointHealthEnabled;

    private boolean springEndpointInfoEnabled;

    private boolean springEndpointMetricsEnabled;

    private boolean springEndpointShutdownEnabled;

    private boolean springEndpointTraceEnabled;

    private boolean springEndpointJolokiaEnabled;

    private boolean springEndpointJMXEnabled;

    private boolean springShellEnabled;

    private String springShellPathPatterns;

    public String getElasticSearchDataPath() {
        return elasticSearchDataPath;
    }

    public void setElasticSearchDataPath(String elasticSearchDataPath) {
        this.elasticSearchDataPath = elasticSearchDataPath;
    }

    public String getEscidocngClusterName() {
        return escidocngClusterName;
    }

    public void setEscidocngClusterName(String escidocngClusterName) {
        this.escidocngClusterName = escidocngClusterName;
    }

    public String getEscidocngVersion() {
        return escidocngVersion;
    }

    public void setEscidocngVersion(String escidocngVersion) {
        this.escidocngVersion = escidocngVersion;
    }

    public boolean isEscidocngExportEnabled() {
        return escidocngExportEnabled;
    }

    public void setEscidocngExportEnabled(boolean escidocngExportEnabled) {
        this.escidocngExportEnabled = escidocngExportEnabled;
    }

    public String getEscidocngExportPath() {
        return escidocngExportPath;
    }

    public void setEscidocngExportPath(String escidocngExportPath) {
        this.escidocngExportPath = escidocngExportPath;
    }

    public boolean isEscidocngCsrfProtectionEnabled() {
        return escidocngCsrfProtectionEnabled;
    }

    public void setEscidocngCsrfProtectionEnabled(boolean escidocngCsrfProtectionEnabled) {
        this.escidocngCsrfProtectionEnabled = escidocngCsrfProtectionEnabled;
    }

    public boolean isEscidocngMessagingEnabled() {
        return escidocngMessagingEnabled;
    }

    public void setEscidocngMessagingEnabled(boolean escidocngMessagingEnabled) {
        this.escidocngMessagingEnabled = escidocngMessagingEnabled;
    }

    public String getEscidocngMessagingBrokerUri() {
        return escidocngMessagingBrokerUri;
    }

    public void setEscidocngMessagingBrokerUri(String escidocngMessagingBrokerUri) {
        this.escidocngMessagingBrokerUri = escidocngMessagingBrokerUri;
    }

    public String getEscidocngMessagingBrokerPath() {
        return escidocngMessagingBrokerPath;
    }

    public void setEscidocngMessagingBrokerPath(String escidocngMessagingBrokerPath) {
        this.escidocngMessagingBrokerPath = escidocngMessagingBrokerPath;
    }

    public boolean isEscidocngMailEnabled() {
        return escidocngMailEnabled;
    }

    public void setEscidocngMailEnabled(boolean escidocngMailEnabled) {
        this.escidocngMailEnabled = escidocngMailEnabled;
    }

    public String getEscidocngMailFrom() {
        return escidocngMailFrom;
    }

    public void setEscidocngMailFrom(String escidocngMailFrom) {
        this.escidocngMailFrom = escidocngMailFrom;
    }

    public String getEscidocngMailSmtpHost() {
        return escidocngMailSmtpHost;
    }

    public void setEscidocngMailSmtpHost(String escidocngMailSmtpHost) {
        this.escidocngMailSmtpHost = escidocngMailSmtpHost;
    }

    public int getEscidocngMailSmtpPort() {
        return escidocngMailSmtpPort;
    }

    public void setEscidocngMailSmtpPort(int escidocngMailSmtpPort) {
        this.escidocngMailSmtpPort = escidocngMailSmtpPort;
    }

    public String getEscidocngMailSmtpUser() {
        return escidocngMailSmtpUser;
    }

    public void setEscidocngMailSmtpUser(String escidocngMailSmtpUser) {
        this.escidocngMailSmtpUser = escidocngMailSmtpUser;
    }

    public String getEscidocngMailSmtpPass() {
        return escidocngMailSmtpPass;
    }

    public void setEscidocngMailSmtpPass(String escidocngMailSmtpPass) {
        this.escidocngMailSmtpPass = escidocngMailSmtpPass;
    }

    public String getElasticSearchClusterName() {
        return elasticSearchClusterName;
    }

    public void setElasticSearchClusterName(String elasticSearchClusterName) {
        this.elasticSearchClusterName = elasticSearchClusterName;
    }

    public String getElasticSearchLogPath() {
        return elasticSearchLogPath;
    }

    public void setElasticSearchLogPath(String elasticSearchLogPath) {
        this.elasticSearchLogPath = elasticSearchLogPath;
    }

    public String getElasticSearchBootstrapMlockAll() {
        return elasticSearchBootstrapMlockAll;
    }

    public void setElasticSearchBootstrapMlockAll(String elasticSearchBootstrapMlockAll) {
        this.elasticSearchBootstrapMlockAll = elasticSearchBootstrapMlockAll;
    }

    public String getElasticSearchBindHost() {
        return elasticSearchBindHost;
    }

    public void setElasticSearchBindHost(String elasticSearchBindHost) {
        this.elasticSearchBindHost = elasticSearchBindHost;
    }

    public int getElasticSearchExpectedNodes() {
        return elasticSearchExpectedNodes;
    }

    public void setElasticSearchExpectedNodes(int elasticSearchExpectedNodes) {
        this.elasticSearchExpectedNodes = elasticSearchExpectedNodes;
    }

    public int getElasticSearchHttpPort() {
        return elasticSearchHttpPort;
    }

    public void setElasticSearchHttpPort(int elasticSearchHttpPort) {
        this.elasticSearchHttpPort = elasticSearchHttpPort;
    }

    public boolean isElasticSearchHttpEnabled() {
        return elasticSearchHttpEnabled;
    }

    public void setElasticSearchHttpEnabled(boolean elasticSearchHttpEnabled) {
        this.elasticSearchHttpEnabled = elasticSearchHttpEnabled;
    }

    public String getElasticSearchGatewayType() {
        return elasticSearchGatewayType;
    }

    public void setElasticSearchGatewayType(String elasticSearchGatewayType) {
        this.elasticSearchGatewayType = elasticSearchGatewayType;
    }

    public String getElasticSearchConfigPath() {
        return elasticSearchConfigPath;
    }

    public void setElasticSearchConfigPath(String elasticSearchConfigPath) {
        this.elasticSearchConfigPath = elasticSearchConfigPath;
    }

    public String getSpringActiveProfile() {
        return springActiveProfile;
    }

    public void setSpringActiveProfile(String springActiveProfile) {
        this.springActiveProfile = springActiveProfile;
    }

    public String getSpringShowBanner() {
        return springShowBanner;
    }

    public void setSpringShowBanner(String springShowMainBanner) {
        this.springShowBanner = springShowMainBanner;
    }

    public String getEscidocngLogPath() {
        return escidocngLogPath;
    }

    public void setEscidocngLogPath(String escidocngLogPath) {
        this.escidocngLogPath = escidocngLogPath;
    }

    public String getEscidocngLogFile() {
        return escidocngLogFile;
    }

    public void setEscidocngLogFile(String escidocngLogFile) {
        this.escidocngLogFile = escidocngLogFile;
    }

    public int getEscidocngServerPort() {
        return escidocngServerPort;
    }

    public void setEscidocngServerPort(int escidocngServerPort) {
        this.escidocngServerPort = escidocngServerPort;
    }

    public boolean isTomcatAccessLogEnabled() {
        return tomcatAccessLogEnabled;
    }

    public void setTomcatAccessLogEnabled(boolean tomcatAccessLogEnabled) {
        this.tomcatAccessLogEnabled = tomcatAccessLogEnabled;
    }

    public boolean isJsonPrettyPrintEnabled() {
        return jsonPrettyPrintEnabled;
    }

    public void setJsonPrettyPrintEnabled(boolean jsonPrettyPrintEnabled) {
        this.jsonPrettyPrintEnabled = jsonPrettyPrintEnabled;
    }

    public boolean isSpringJmxEnabled() {
        return springJmxEnabled;
    }

    public void setSpringJmxEnabled(boolean springJmxEnabled) {
        this.springJmxEnabled = springJmxEnabled;
    }

    public boolean isSpringEndpointAutoconfigEnabled() {
        return springEndpointAutoconfigEnabled;
    }

    public void setSpringEndpointAutoconfigEnabled(boolean springEndpointAutoconfigEnabled) {
        this.springEndpointAutoconfigEnabled = springEndpointAutoconfigEnabled;
    }

    public boolean isSpringEndpointBeansEnabled() {
        return springEndpointBeansEnabled;
    }

    public void setSpringEndpointBeansEnabled(boolean springEndpointBeansEnabled) {
        this.springEndpointBeansEnabled = springEndpointBeansEnabled;
    }

    public boolean isSpringEndpointConfigPropsEnabled() {
        return springEndpointConfigPropsEnabled;
    }

    public void setSpringEndpointConfigPropsEnabled(boolean springEndpointConfigPropsEnabled) {
        this.springEndpointConfigPropsEnabled = springEndpointConfigPropsEnabled;
    }

    public boolean isSpringEndpointDumpEnabled() {
        return springEndpointDumpEnabled;
    }

    public void setSpringEndpointDumpEnabled(boolean springEndpointDumpEnabled) {
        this.springEndpointDumpEnabled = springEndpointDumpEnabled;
    }

    public boolean isSpringEndpointEnvEnabled() {
        return springEndpointEnvEnabled;
    }

    public void setSpringEndpointEnvEnabled(boolean springEndpointEnvEnabled) {
        this.springEndpointEnvEnabled = springEndpointEnvEnabled;
    }

    public boolean isSpringEndpointHealthEnabled() {
        return springEndpointHealthEnabled;
    }

    public void setSpringEndpointHealthEnabled(boolean springEndpointHealthEnabled) {
        this.springEndpointHealthEnabled = springEndpointHealthEnabled;
    }

    public boolean isSpringEndpointInfoEnabled() {
        return springEndpointInfoEnabled;
    }

    public void setSpringEndpointInfoEnabled(boolean springEndpointInfoEnabled) {
        this.springEndpointInfoEnabled = springEndpointInfoEnabled;
    }

    public boolean isSpringEndpointMetricsEnabled() {
        return springEndpointMetricsEnabled;
    }

    public void setSpringEndpointMetricsEnabled(boolean springEndpointMetricsEnabled) {
        this.springEndpointMetricsEnabled = springEndpointMetricsEnabled;
    }

    public boolean isSpringEndpointShutdownEnabled() {
        return springEndpointShutdownEnabled;
    }

    public void setSpringEndpointShutdownEnabled(boolean springEndpointShutdownEnabled) {
        this.springEndpointShutdownEnabled = springEndpointShutdownEnabled;
    }

    public boolean isSpringEndpointTraceEnabled() {
        return springEndpointTraceEnabled;
    }

    public void setSpringEndpointTraceEnabled(boolean springEndpointTraceEnabled) {
        this.springEndpointTraceEnabled = springEndpointTraceEnabled;
    }

    public boolean isSpringEndpointJolokiaEnabled() {
        return springEndpointJolokiaEnabled;
    }

    public void setSpringEndpointJolokiaEnabled(boolean springEndpointJolokiaEnabled) {
        this.springEndpointJolokiaEnabled = springEndpointJolokiaEnabled;
    }

    public boolean isSpringEndpointJMXEnabled() {
        return springEndpointJMXEnabled;
    }

    public void setSpringEndpointJMXEnabled(boolean springEndpointJMXEnabled) {
        this.springEndpointJMXEnabled = springEndpointJMXEnabled;
    }

    public boolean isSpringShellEnabled() {
        return springShellEnabled;
    }

    public void setSpringShellEnabled(boolean springShellEnabled) {
        this.springShellEnabled = springShellEnabled;
    }

    public String getSpringShellPathPatterns() {
        return springShellPathPatterns;
    }

    public void setSpringShellPathPatterns(String springShellPathPatterns) {
        this.springShellPathPatterns = springShellPathPatterns;
    }

    public Describe getDescribe() {
        return describe;
    }

    public void setDescribe(Describe describe) {
        this.describe = describe;
    }

    public BlobstoreState getBlobstoreState() {
        return blobstoreState;
    }

    public void setBlobstoreState(BlobstoreState blobstoreState) {
        this.blobstoreState = blobstoreState;
    }

    public IndexState getIndexState() {
        return indexState;
    }

    public void setIndexState(IndexState indexState) {
        this.indexState = indexState;
    }

    public EscidocngState getEscidocngState() {
        return escidocngState;
    }

    public void setEscidocngState(EscidocngState escidocngState) {
        this.escidocngState = escidocngState;
    }
}
