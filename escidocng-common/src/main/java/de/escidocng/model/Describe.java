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

/**
 * DTO class containing general repository state information
 */
public class Describe {

    private String escidocngVersion;

    private String escidocngHost;

    private String escidocngClusterName;

    private String esNodeName;

    private String esVersion;

    private String esMasterNodeName;

    private String esMasterNodeAddress;

    private int esNumDataNodes;

    private long esNumIndexedRecords;

    /**
     * Get the number of indexed Documents in ElasticSearch
     * 
     * @return the number of records
     */
    public long getEsNumIndexedRecords() {
        return esNumIndexedRecords;
    }

    /**
     * Set the number of records in ElasticSearch
     * 
     * @param esNumIndexedRecords the number of records to set
     */
    public void setEsNumIndexedRecords(long esNumIndexedRecords) {
        this.esNumIndexedRecords = esNumIndexedRecords;
    }

    /**
     * Get the ElasticSearch master node address
     * 
     * @return the master node address
     */
    public String getEsMasterNodeAddress() {
        return esMasterNodeAddress;
    }

    /**
     * Set the ElasticSearch master node address
     * 
     * @param esMasterNodeAddress the address to set
     */
    public void setEsMasterNodeAddress(String esMasterNodeAddress) {
        this.esMasterNodeAddress = esMasterNodeAddress;
    }

    /**
     * Get the number of ElasticSearch data nodes
     * 
     * @return the number of data nodes
     */
    public int getEsNumDataNodes() {
        return esNumDataNodes;
    }

    /**
     * Set the number of ElasticSearch data nodes
     * 
     * @param esNumDataNodes the number of data nodes to set
     */
    public void setEsNumDataNodes(int esNumDataNodes) {
        this.esNumDataNodes = esNumDataNodes;
    }

    /**
     * Get the current node's name
     * 
     * @return the name of the current node
     */
    public String getEsNodeName() {
        return esNodeName;
    }

    /**
     * Set the current node's name
     * 
     * @param esNodeName the name to set
     */
    public void setEsNodeName(String esNodeName) {
        this.esNodeName = esNodeName;
    }

    /**
     * Get the ElasticSearch version
     * 
     * @return the version of ElasticSearch
     */
    public String getEsVersion() {
        return esVersion;
    }

    /**
     * Set the ElasticSearch version
     * 
     * @param esVersion the version to set
     */
    public void setEsVersion(String esVersion) {
        this.esVersion = esVersion;
    }

    /**
     * Get the ElasticSearch master node's name
     * 
     * @return the name of the master node
     */
    public String getEsMasterNodeName() {
        return esMasterNodeName;
    }

    /**
     * Set the ElasticSearch master node's name
     * 
     * @param esMasterNodeName the name to set
     */
    public void setEsMasterNodeName(String esMasterNodeName) {
        this.esMasterNodeName = esMasterNodeName;
    }

    /**
     * Get the escidocng cluster name
     * 
     * @return the cluster name
     */
    public String getEscidocngClusterName() {
        return escidocngClusterName;
    }

    /**
     * Set the escidocng cluster name
     * 
     * @param escidocngClusterName the name to set
     */
    public void setEscidocngClusterName(String escidocngClusterName) {
        this.escidocngClusterName = escidocngClusterName;
    }

    /**
     * Get the current host name
     * 
     * @return the host name
     */
    public String getEscidocngHost() {
        return escidocngHost;
    }

    /**
     * Set the current host name
     * 
     * @param escidocngHost the host name to set
     */
    public void setEscidocngHost(String escidocngHost) {
        this.escidocngHost = escidocngHost;
    }

    /**
     * Get the escidocng version
     * 
     * @return the escidocng version
     */
    public String getEscidocngVersion() {
        return escidocngVersion;
    }

    /**
     * Set the escidocng version
     * 
     * @param escidocngVersion the version to set
     */
    public void setEscidocngVersion(String escidocngVersion) {
        this.escidocngVersion = escidocngVersion;
    }
}
