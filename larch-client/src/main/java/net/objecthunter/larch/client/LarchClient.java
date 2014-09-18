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

package net.objecthunter.larch.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Describe;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.state.LarchState;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Client implementation for the Larch server
 */
public class LarchClient {

    private static final Logger log = LoggerFactory.getLogger(LarchClient.class);

    private final URI larchUri;

    private ObjectMapper mapper = new ObjectMapper();

    private ThreadLocal<Executor> executor;

    public LarchClient(URI larchUri, String username, String password) {
        if (larchUri.toASCIIString().endsWith("/")) {
            this.larchUri = URI.create(larchUri.toASCIIString().substring(0, larchUri.toASCIIString().length() -1));
        }else {
            this.larchUri = larchUri;
        }
        final HttpHost larch = new HttpHost(larchUri.getHost(), larchUri.getPort());
        this.executor = new ThreadLocal<Executor>() {
            @Override
            protected Executor initialValue() {
                return Executor.newInstance()
                        .auth(larch, username, password)
                        .authPreemptive(larch);
            }
        };
    }

    /**
     * Retrieve a {@link net.objecthunter.larch.model.state.LarchState} response from the repository containing
     * detailed state information
     * 
     * @return a LarchState POJO
     * @throws IOException
     */
    public LarchState retrieveState() throws IOException {
        final HttpResponse resp = this.execute(Request.Get(larchUri + "/state")
                .useExpectContinue())
                .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Unable to retrieve LarchState response from the repository");
        }
        return mapper.readValue(resp.getEntity().getContent(), LarchState.class);
    }

    /**
     * Retrieve a {@link net.objecthunter.larch.model.Describe} response from the repository containing general state
     * information
     * 
     * @return a Describe POJO
     * @throws IOException
     */
    public Describe retrieveDescribe() throws IOException {
        final HttpResponse resp = this.execute(Request.Get(larchUri + "/describe")
                .useExpectContinue())
                .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Unable to retrieve Describe response from the repository");
        }
        return mapper.readValue(resp.getEntity().getContent(), Describe.class);
    }

    /**
     * Retrieve a {@link net.objecthunter.larch.model.Metadata} of an Entity from the repository
     * 
     * @param entityId the entity's id
     * @param metadataName the meta data set's name
     * @return the Metadata as a POJO
     * @throws IOException if an error occurred while fetching the meta data
     */
    public Metadata retrieveMetadata(String entityId, String metadataName) throws IOException {
        final HttpResponse resp =
                this.execute(Request.Get(larchUri + "/entity/" + entityId + "/metadata/" + metadataName)
                        .useExpectContinue())
                        .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to fetch meta data\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to fetch meta data " + metadataName + " from entity " + entityId);
        }
        return mapper.readValue(resp.getEntity().getContent(), Metadata.class);
    }

    /**
     * Retrieve a {@link net.objecthunter.larch.model.Metadata} of a Binary from the repository
     * 
     * @param entityId the entity's id
     * @param metadataName the meta data set's name
     * @return the Metadata as a POJO
     * @throws IOException if an error occurred while fetching the meta data
     */
    public Metadata retrieveBinaryMetadata(String entityId, String binaryName, String metadataName)
            throws IOException {
        final HttpResponse resp =
                this.execute(
                        Request.Get(
                                larchUri + "/entity/" + entityId + "/binary/" + binaryName + "/metadata/" +
                                        metadataName)
                                .useExpectContinue())
                        .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to fetch meta data\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to fetch meta data " + metadataName + " from binary " + binaryName +
                    " of entity " + entityId);
        }
        return mapper.readValue(resp.getEntity().getContent(), Metadata.class);
    }

    /**
     * Add {@link net.objecthunter.larch.model.Metadata} to an existing entity
     * 
     * @param entityId the entity's id
     * @param md the Metadata object
     * @throws IOException
     */
    public void postMetadata(String entityId, Metadata md) throws IOException {
        final HttpResponse resp = this.execute(Request.Post(larchUri + "/entity/" + entityId + "/metadata")
                .useExpectContinue()
                .bodyString(mapper.writeValueAsString(md), ContentType.APPLICATION_JSON))
                .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 201) {
            log.error("Unable to add meta data\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to add meta data " + md.getName() + " from entity " + entityId);
        }
    }

    /**
     * Add {@link net.objecthunter.larch.model.Metadata} to an existing binary
     * 
     * @param entityId the entity's id
     * @param md the Metadata object
     * @throws IOException
     */
    public void postBinaryMetadata(String entityId, String binaryName, Metadata md) throws IOException {
        final HttpResponse resp =
                this.execute(Request.Post(larchUri + "/entity/" + entityId + "/binary/" + binaryName + "/metadata")
                        .useExpectContinue()
                        .bodyString(mapper.writeValueAsString(md), ContentType.APPLICATION_JSON))
                        .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 201) {
            log.error("Unable to add meta data to binary\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to add meta data " + md.getName() + " to binary " + binaryName +
                    " of entity " + entityId);
        }
    }

    /**
     * Delete the {@link net.objecthunter.larch.model.Metadata} of a {@link net.objecthunter.larch.model.Entity}
     * 
     * @param entityId the entity's id
     * @param metadataName the meta data set's name
     * @throws IOException
     */
    public void deleteMetadata(String entityId, String metadataName) throws IOException {
        final HttpResponse resp =
                this.execute(Request.Delete(larchUri + "/entity/" + entityId + "/metadata/" + metadataName)
                        .useExpectContinue())
                        .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to remove meta data from entity\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to remove meta data " + metadataName + "  of entity " + entityId);
        }

    }

    /**
     * Delete the {@link net.objecthunter.larch.model.Metadata} of a {@link net.objecthunter.larch.model.Binary}
     * 
     * @param entityId the entity's id
     * @param binaryName the binary's name
     * @param metadataName the meta data set's name
     * @throws IOException
     */
    public void deleteBinaryMetadata(String entityId, String binaryName, String metadataName) throws IOException {
        final HttpResponse resp =
                this.execute(
                        Request.Delete(
                                larchUri + "/entity/" + entityId + "/binary/" + binaryName + "/metadata/" +
                                        metadataName)
                                .useExpectContinue())
                        .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to remove meta data from binary\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to remove meta data " + metadataName + " from binary " + binaryName +
                    " of entity " + entityId);
        }

    }

    /**
     * Retrieve {@link net.objecthunter.larch.model.Binary} from the repository
     * 
     * @param entityId the entity's id
     * @param binaryName the binary's name
     * @return the Binary as a POJO
     * @throws IOException if an error occurred while fetching from the repository
     */
    public Binary retrieveBinary(String entityId, String binaryName) throws IOException {
        final HttpResponse resp =
                this.execute(Request.Get(larchUri + "/entity/" + entityId + "/binary/" + binaryName)
                        .useExpectContinue())
                        .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to fetch binary\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to fetch binary" + binaryName + " from entity " + entityId);
        }
        return mapper.readValue(resp.getEntity().getContent(), Binary.class);
    }

    /**
     * Add a {@link net.objecthunter.larch.model.Binary} to an existing entity
     * 
     * @param entityId the entity's id
     * @param bin the binary object to add
     * @throws IOException
     */
    public void postBinary(String entityId, Binary bin) throws IOException {
        final HttpResponse resp = this.execute(Request.Post(larchUri + "/entity/" + entityId + "/binary")
                .useExpectContinue()
                .bodyString(mapper.writeValueAsString(bin), ContentType.APPLICATION_JSON))
                .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 201) {
            log.error("Unable to add binary. Server says:\n", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to add binary " + bin.getName() + " to entity " + entityId);
        }
    }

    public void postBinary(String entityId, String name, String mimeType, InputStream src) throws IOException {
        final HttpResponse resp = this.execute(Request.Post(larchUri + "/entity/" + entityId + "/binary?name=" + name
                + "&mimetype=" + mimeType)
                .useExpectContinue()
                .bodyStream(src))
                .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 201) {
            log.error("Unable to add binary. Server says:\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to add binary " + name + " to entity " + entityId);
        }
    }

    /**
     * Delete a {@link net.objecthunter.larch.model.Binary} in the repository
     * 
     * @param entityId the entity's id
     * @param binaryName the binary's name
     * @throws IOException
     */
    public void deleteBinary(String entityId, String binaryName) throws IOException {
        final HttpResponse resp =
                this.execute(Request.Delete(larchUri + "/entity/" + entityId + "/binary/" + binaryName)
                        .useExpectContinue())
                        .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to delete binary. Server says:\n", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to delete binary " + binaryName + " of entity " + entityId);
        }
    }

    /**
     * Fetch the actual binary content from the repository
     * 
     * @param entityId the Id of the entity
     * @param binaryName the name of the binary to fetch
     * @return an InputStream containing the binary's data
     * @throws IOException
     */
    public InputStream retrieveBinaryContent(String entityId, String binaryName) throws IOException {
        final HttpResponse resp =
                this.execute(Request.Get(larchUri + "/entity/" + entityId + "/binary/" + binaryName + "/content")
                        .useExpectContinue())
                        .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to fetch binary data\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to fetch binary data " + binaryName + " from entity " + entityId);
        }
        return resp.getEntity().getContent();
    }

    /**
     * Update an {@link net.objecthunter.larch.model.Entity} in the larch repository
     * 
     * @param e the updated entity object to be written to the repository
     * @throws IOException if an error occurred during update
     */
    public void updateEntity(Entity e) throws IOException {
        if (e.getId() == null || e.getId().isEmpty()) {
            throw new IOException("ID of the entity can not be empty when updating");
        }
        final HttpResponse resp = this.execute(Request.Put(larchUri + "/entity/" + e.getId())
                .useExpectContinue()
                .bodyString(mapper.writeValueAsString(e), ContentType.APPLICATION_JSON))
                .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to update entity\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to update entity " + e.getId());
        }
    }

    /**
     * Delete an entity in the larch repository
     * 
     * @param id the id of the entity to delete
     * @throws IOException
     */
    public void deleteEntity(String id) throws IOException {
        final HttpResponse resp = this.execute(Request.Delete(larchUri + "/entity/" + id)
                .useExpectContinue())
                .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to delete Entity\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to delete entity " + id);
        }
    }

    /**
     * Post an {@link net.objecthunter.larch.model.Entity} to the Larch server
     * 
     * @param e The entity to ingest
     * @return the entity's id
     * @throws IOException if an error occurred while ingesting
     */
    public String postEntity(Entity e) throws IOException {
        final HttpResponse resp = this.execute(Request.Post(larchUri + "/entity")
                .useExpectContinue()
                .bodyString(mapper.writeValueAsString(e), ContentType.APPLICATION_JSON))
                .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 201) {
            log.error("Unable to post entity to Larch at {}\n{}", larchUri, EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to create Entity " + e.getId());
        }
        return EntityUtils.toString(resp.getEntity());
    }

    /**
     * Retrieve an entity using a HTTP GET from the Larch server
     * 
     * @param id the Id of the entity
     * @return An entity object
     */
    public Entity retrieveEntity(String id) throws IOException {
        final HttpResponse resp = this.execute(Request.Get(larchUri + "/entity/" + id)
                .useExpectContinue()
                .addHeader("Accept", "application/json"))
                .returnResponse();
        System.out.println(EntityUtils.toString(resp.getEntity()));
        return mapper.readValue(resp.getEntity().getContent(), Entity.class);
    }

    /**
     * Execute a HTTP request
     * 
     * @param req the Request object to use for the execution
     * @return The HttpResponse containing the requested information
     * @throws IOException
     */
    protected Response execute(Request req) throws IOException {
        return this.executor.get().execute(req);
    }
}
