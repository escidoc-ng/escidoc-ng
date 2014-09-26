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

/**
 * DTO class containing general repository state information
 */
public class Describe {

    private String larchVersion;

    private String larchHost;

    private String larchClusterName;

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
     * Get the larch cluster name
     * 
     * @return the cluster name
     */
    public String getLarchClusterName() {
        return larchClusterName;
    }

    /**
     * Set the larch cluster name
     * 
     * @param larchClusterName the name to set
     */
    public void setLarchClusterName(String larchClusterName) {
        this.larchClusterName = larchClusterName;
    }

    /**
     * Get the current host name
     * 
     * @return the host name
     */
    public String getLarchHost() {
        return larchHost;
    }

    /**
     * Set the current host name
     * 
     * @param larchHost the host name to set
     */
    public void setLarchHost(String larchHost) {
        this.larchHost = larchHost;
    }

    /**
     * Get the larch version
     * 
     * @return the larch version
     */
    public String getLarchVersion() {
        return larchVersion;
    }

    /**
     * Set the larch version
     * 
     * @param larchVersion the version to set
     */
    public void setLarchVersion(String larchVersion) {
        this.larchVersion = larchVersion;
    }
}
