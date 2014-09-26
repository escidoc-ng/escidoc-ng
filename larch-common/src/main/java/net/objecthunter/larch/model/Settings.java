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
 * WITHOUT WARRANTIES OR CONDITIONS OF ROLE_ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package net.objecthunter.larch.model;

import net.objecthunter.larch.model.state.BlobstoreState;
import net.objecthunter.larch.model.state.IndexState;
import net.objecthunter.larch.model.state.LarchState;

public class Settings {

    private BlobstoreState blobstoreState;

    private IndexState indexState;

    private LarchState larchState;

    private Describe describe;

    private String larchClusterName;

    private String larchVersion;

    private boolean larchExportEnabled;

    private String larchExportPath;

    private boolean larchCsrfProtectionEnabled;

    private boolean larchMessagingEnabled;

    private String larchMessagingBrokerUri;

    private String larchMessagingBrokerPath;

    private boolean larchMailEnabled;

    private String larchMailFrom;

    private String larchMailSmtpHost;

    private int larchMailSmtpPort;

    private String larchMailSmtpUser;

    private String larchMailSmtpPass;

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

    private String larchLogPath;

    private String larchLogFile;

    private int larchServerPort;

    private boolean tomcatAccessLogEnabled;

    private boolean jsonPrettyPrintEnabled;

    private String thymeleafPrefix;

    private String thymeleafSuffix;

    private String thymeleafMode;

    private String thymeleafEncoding;

    private boolean thymeleafCacheEnabled;

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

    public String getLarchClusterName() {
        return larchClusterName;
    }

    public void setLarchClusterName(String larchClusterName) {
        this.larchClusterName = larchClusterName;
    }

    public String getLarchVersion() {
        return larchVersion;
    }

    public void setLarchVersion(String larchVersion) {
        this.larchVersion = larchVersion;
    }

    public boolean isLarchExportEnabled() {
        return larchExportEnabled;
    }

    public void setLarchExportEnabled(boolean larchExportEnabled) {
        this.larchExportEnabled = larchExportEnabled;
    }

    public String getLarchExportPath() {
        return larchExportPath;
    }

    public void setLarchExportPath(String larchExportPath) {
        this.larchExportPath = larchExportPath;
    }

    public boolean isLarchCsrfProtectionEnabled() {
        return larchCsrfProtectionEnabled;
    }

    public void setLarchCsrfProtectionEnabled(boolean larchCsrfProtectionEnabled) {
        this.larchCsrfProtectionEnabled = larchCsrfProtectionEnabled;
    }

    public boolean isLarchMessagingEnabled() {
        return larchMessagingEnabled;
    }

    public void setLarchMessagingEnabled(boolean larchMessagingEnabled) {
        this.larchMessagingEnabled = larchMessagingEnabled;
    }

    public String getLarchMessagingBrokerUri() {
        return larchMessagingBrokerUri;
    }

    public void setLarchMessagingBrokerUri(String larchMessagingBrokerUri) {
        this.larchMessagingBrokerUri = larchMessagingBrokerUri;
    }

    public String getLarchMessagingBrokerPath() {
        return larchMessagingBrokerPath;
    }

    public void setLarchMessagingBrokerPath(String larchMessagingBrokerPath) {
        this.larchMessagingBrokerPath = larchMessagingBrokerPath;
    }

    public boolean isLarchMailEnabled() {
        return larchMailEnabled;
    }

    public void setLarchMailEnabled(boolean larchMailEnabled) {
        this.larchMailEnabled = larchMailEnabled;
    }

    public String getLarchMailFrom() {
        return larchMailFrom;
    }

    public void setLarchMailFrom(String larchMailFrom) {
        this.larchMailFrom = larchMailFrom;
    }

    public String getLarchMailSmtpHost() {
        return larchMailSmtpHost;
    }

    public void setLarchMailSmtpHost(String larchMailSmtpHost) {
        this.larchMailSmtpHost = larchMailSmtpHost;
    }

    public int getLarchMailSmtpPort() {
        return larchMailSmtpPort;
    }

    public void setLarchMailSmtpPort(int larchMailSmtpPort) {
        this.larchMailSmtpPort = larchMailSmtpPort;
    }

    public String getLarchMailSmtpUser() {
        return larchMailSmtpUser;
    }

    public void setLarchMailSmtpUser(String larchMailSmtpUser) {
        this.larchMailSmtpUser = larchMailSmtpUser;
    }

    public String getLarchMailSmtpPass() {
        return larchMailSmtpPass;
    }

    public void setLarchMailSmtpPass(String larchMailSmtpPass) {
        this.larchMailSmtpPass = larchMailSmtpPass;
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

    public String getLarchLogPath() {
        return larchLogPath;
    }

    public void setLarchLogPath(String larchLogPath) {
        this.larchLogPath = larchLogPath;
    }

    public String getLarchLogFile() {
        return larchLogFile;
    }

    public void setLarchLogFile(String larchLogFile) {
        this.larchLogFile = larchLogFile;
    }

    public int getLarchServerPort() {
        return larchServerPort;
    }

    public void setLarchServerPort(int larchServerPort) {
        this.larchServerPort = larchServerPort;
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

    public String getThymeleafPrefix() {
        return thymeleafPrefix;
    }

    public void setThymeleafPrefix(String thymeleafPrefix) {
        this.thymeleafPrefix = thymeleafPrefix;
    }

    public String getThymeleafSuffix() {
        return thymeleafSuffix;
    }

    public void setThymeleafSuffix(String thymeleafSuffix) {
        this.thymeleafSuffix = thymeleafSuffix;
    }

    public String getThymeleafMode() {
        return thymeleafMode;
    }

    public void setThymeleafMode(String thymeleafMode) {
        this.thymeleafMode = thymeleafMode;
    }

    public String getThymeleafEncoding() {
        return thymeleafEncoding;
    }

    public void setThymeleafEncoding(String thymeleafEncoding) {
        this.thymeleafEncoding = thymeleafEncoding;
    }

    public boolean isThymeleafCacheEnabled() {
        return thymeleafCacheEnabled;
    }

    public void setThymeleafCacheEnabled(boolean thymeleafCacheEnabled) {
        this.thymeleafCacheEnabled = thymeleafCacheEnabled;
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

    public LarchState getLarchState() {
        return larchState;
    }

    public void setLarchState(LarchState larchState) {
        this.larchState = larchState;
    }
}
