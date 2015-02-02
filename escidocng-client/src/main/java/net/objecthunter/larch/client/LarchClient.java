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


package net.objecthunter.larch.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Describe;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.security.role.Right;
import net.objecthunter.larch.model.security.role.Role;
import net.objecthunter.larch.model.security.role.Role.RoleRight;
import net.objecthunter.larch.model.security.role.UserRole;
import net.objecthunter.larch.model.source.Source;
import net.objecthunter.larch.model.state.LarchState;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
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
     * Execute Http-Request as user with given username/password.
     * 
     * @param method String
     * @param uri
     * @param body String-body for PUT/POST requests
     * @param username
     * @param password
     * @return InputStream
     * @throws IOException
     */
    public Object[] executeAsUser(String method, String uri, Object body, String username,
            String password)
            throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpUriRequest request = getRequest(method, this.larchUri + uri, body);
        if (request != null) {
            byte[] encodedBytes = Base64.encodeBase64((username + ":" + password).getBytes());
            String authorization = "Basic " + new String(encodedBytes);
            request.setHeader("Authorization", authorization);
            long time = System.currentTimeMillis();
            HttpResponse resp = httpClient.execute(request);
            if (resp.getStatusLine().getStatusCode() > 400) {
                throw new IOException("Unable to execute request: " + resp.getStatusLine().getReasonPhrase());
            }
            String respStr = IOUtils.toString(resp.getEntity().getContent());
            return new Object[] {respStr, System.currentTimeMillis() - time};
        }
        return null;
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
     * Retrieve a {@link net.objecthunter.larch.model.Metadata} of an Entity from the repository
     * 
     * @param entityId the entity's id
     * @param metadataName the meta data set's name
     * @return the Metadata as a POJO
     * @throws IOException if an error occurred while fetching the meta data
     */
    public InputStream retrieveMetadataContent(String entityId, String metadataName) throws IOException {
        final HttpResponse resp =
                this.execute(Request.Get(larchUri + "/entity/" + entityId + "/metadata/" + metadataName + "/content")
                        .useExpectContinue())
                        .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to fetch meta data\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to fetch meta data " + metadataName + " from entity " + entityId);
        }
        return resp.getEntity().getContent();
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
     * Add {@link net.objecthunter.larch.model.Metadata} to an existing entity
     * 
     * @param entityId the entity's id
     * @param md the Metadata object
     * @throws IOException
     */
    public void postMetadataMultipart(String entityId, Metadata md) throws IOException {
        HttpResponse resp =
                this.execute(
                        Request.Post(larchUri + "/entity/" + entityId + "/metadata").body(MultipartEntityBuilder.create()
                                .addTextBody("name", md.getName())
                                .addTextBody("type", md.getType())
                                .addTextBody("indexInline", new Boolean(md.isIndexInline()).toString())
                                .addBinaryBody(
                                        "data",
                                        md.getSource().getInputStream(), ContentType.APPLICATION_XML, md.getFilename())
                                .build())).returnResponse();

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
     * Add {@link net.objecthunter.larch.model.Metadata} to an existing entity
     * 
     * @param entityId the entity's id
     * @param md the Metadata object
     * @throws IOException
     */
    public void postBinaryMetadataMultipart(String entityId, String binaryName, Metadata md) throws IOException {
        HttpResponse resp =
                this.execute(
                        Request.Post(larchUri + "/entity/" + entityId + "/binary/" + binaryName + "/metadata").body(MultipartEntityBuilder.create()
                                .addTextBody("name", md.getName())
                                .addTextBody("type", md.getType())
                                .addTextBody("indexInline", new Boolean(md.isIndexInline()).toString())
                                .addBinaryBody(
                                        "data",
                                        md.getSource().getInputStream(), ContentType.APPLICATION_XML, md.getFilename())
                                .build())).returnResponse();

        if (resp.getStatusLine().getStatusCode() != 201) {
            log.error("Unable to add meta data\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to add meta data " + md.getName() + " from entity " + entityId);
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

    /**
     * Add a {@link net.objecthunter.larch.model.Binary} to an existing entity
     * 
     * @param entityId the entity's id
     * @param bin the binary object to add
     * @throws IOException
     */
    public void postBinaryMultipart(String entityId, Binary bin) throws IOException {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        if (bin.getName() != null) {
            entityBuilder.addTextBody("name", bin.getName());
        }
        if (bin.getSource().getInputStream() != null) {
            entityBuilder.addBinaryBody("binary", bin.getSource().getInputStream(), ContentType.create(null), bin.getFilename());
        }
        
        final HttpResponse resp = this.execute(Request.Post(larchUri + "/entity/" + entityId + "/binary")
                .useExpectContinue()
                .body(entityBuilder.build()))
                .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 201) {
            log.error("Unable to add binary. Server says:\n", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to add binary " + bin.getName() + " to entity " + entityId);
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
     * Update an {@link net.objecthunter.larch.model.Entity} in the larch repository
     * 
     * @param e the updated entity object to be written to the repository
     * @throws IOException if an error occurred during update
     */
    public void updateEntity(String entityId, String entity) throws IOException {
        if (entityId == null || entityId.isEmpty()) {
            throw new IOException("ID of the entity can not be empty when updating");
        }
        final HttpResponse resp = this.execute(Request.Put(larchUri + "/entity/" + entityId)
                .useExpectContinue()
                .bodyString(entity, ContentType.APPLICATION_JSON))
                .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to update entity\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to update entity " + entityId);
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
     * Post an {@link net.objecthunter.larch.model.Entity} to the Larch server
     * 
     * @param e The entity to ingest
     * @return the entity's id
     * @throws IOException if an error occurred while ingesting
     */
    public String postEntity(String entity) throws IOException {
        final HttpResponse resp = this.execute(Request.Post(larchUri + "/entity")
                .useExpectContinue()
                .bodyString(entity, ContentType.APPLICATION_JSON))
                .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 201) {
            log.error("Unable to post entity to Larch at {}\n{}", larchUri, EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to create Entity");
        }
        return EntityUtils.toString(resp.getEntity());
    }

    /**
     * Post an {@link net.objecthunter.larch.model.Entity} to the Larch server
     * 
     * @param entityId the entity's id
     * @param md the Metadata object
     * @throws IOException
     */
    public String postEntityMultipart(Entity entity) throws IOException {
        Map<String, Source> savedBinaries = new HashMap<String, Source>();
        Map<String, Source> savedMetadata = new HashMap<String, Source>();
        for (Binary b : entity.getBinaries()) {
            savedBinaries.put("binary:" + b.getName(), b.getSource());
            b.setSource(null);
            for(Metadata m : b.getMetadata()) {
                savedMetadata.put("binary:" + b.getName() + "metadata:" + m.getName(), m.getSource());
                m.setSource(null);
            }
        }
        for (Metadata m : entity.getMetadata()) {
            savedMetadata.put("metadata:" + m.getName(), m.getSource());
            m.setSource(null);
        }

        String entityString = mapper.writeValueAsString(entity);
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addTextBody("entity", entityString);
        for(Entry<String, Source> e : savedBinaries.entrySet()) {
            entityBuilder.addBinaryBody(e.getKey(), e.getValue().getInputStream(), ContentType.create(ContentType.APPLICATION_OCTET_STREAM.getMimeType()), "filename");
        }
        for(Entry<String, Source> e : savedMetadata.entrySet()) {
            entityBuilder.addBinaryBody(e.getKey(), e.getValue().getInputStream(), ContentType.create(ContentType.APPLICATION_XML.getMimeType()), "filename");
        }
        
        HttpResponse resp =
                this.execute(
                        Request.Post(larchUri + "/entity/").body(entityBuilder.build())).returnResponse();

        if (resp.getStatusLine().getStatusCode() != 201) {
            log.error("Unable to create entity\n{}", EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to create entity");
        }
        return EntityUtils.toString(resp.getEntity());
    }

    /**
     * Post an {@link net.objecthunter.larch.model.Entity} to the Larch server
     * 
     * @param e The entity to ingest
     * @return the entity's id
     * @throws IOException if an error occurred while ingesting
     */
    public String postEntityToES(Entity e) throws IOException {
        File dir = new File("/home/FIZ/mih/entities/");
        File[] files = dir.listFiles();
        long durations = 0;
        for (int i = 201; i < files.length; i++) {
            BufferedReader in = null;
            String json = null;
            try {
                in = new BufferedReader(new InputStreamReader(new FileInputStream(files[i].getAbsolutePath()), "UTF-8"));
                String str = new String("");
                StringBuffer buf = new StringBuffer("");
                while ((str = in.readLine()) != null) {
                    buf.append(str).append("\n");
                }
                in.close();
                json = buf.toString();
            } catch (Exception e1) {
                System.out.println(e1);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e1) {
                        System.out.println(e1);
                    }
                }
            }
            long time = System.currentTimeMillis();
            final HttpResponse resp1 = this.execute(Request.Post("http://localhost:9200/mih/mih")
                    .useExpectContinue()
                    .bodyString(json, ContentType.APPLICATION_JSON))
                    .returnResponse();
            durations += System.currentTimeMillis() - time;
            if (i%100==0) {
                System.out.println("last 100 avg " + durations/100 + " ms");
                durations = 0;
//                try {
//                    Thread.sleep(30000);
//                } catch (InterruptedException e1) {}
            }
        }
        return null;
    }

    /**
     * Post an {@link net.objecthunter.larch.model.Entity} to the Larch server
     * 
     * @param e The entity to ingest
     * @return the entity's id
     * @throws IOException if an error occurred while ingesting
     */
    public String postJsonToES(Entity e) throws IOException {
        int size = 50;
        int depth = 10;
        List<List<String>> elements = new ArrayList<List<String>>();
        for (int i = 0; i < depth; i++) {
            elements.add(new ArrayList<String>());
        }
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < depth; i++) {
                elements.get(i).add(RandomStringUtils.randomAlphabetic(6));
            }
        }
        long durations = 0;
        int i = 0;
        for (;;) {
            JsonFactory jfactory = new JsonFactory();
            StringWriter writer = new StringWriter();
            JsonGenerator jGenerator = jfactory.createGenerator(writer);
            jGenerator.writeStartObject();
            jGenerator.writeStringField("id", RandomStringUtils.randomAlphabetic(16));
            jGenerator.writeStringField("parentId", RandomStringUtils.randomAlphabetic(16));
            jGenerator.writeStringField("contentModelId", "data");
            jGenerator.writeStringField("state", "PENDING");
            jGenerator.writeStringField("version", "1");
            jGenerator.writeFieldName("children");
            jGenerator.writeStartArray();
            jGenerator.writeString(RandomStringUtils.randomAlphabetic(16));
            jGenerator.writeString(RandomStringUtils.randomAlphabetic(16));
            jGenerator.writeString(RandomStringUtils.randomAlphabetic(16));
            jGenerator.writeString(RandomStringUtils.randomAlphabetic(16));
            jGenerator.writeEndArray();
            jGenerator.writeStringField("label", "benchtool-" + RandomStringUtils.randomAlphabetic(16));
            jGenerator.writeStringField("utcCreated", ZonedDateTime.now(ZoneOffset.UTC).toString());
            jGenerator.writeStringField("utcLastModified", ZonedDateTime.now(ZoneOffset.UTC).toString());
            jGenerator.writeStringField("tags", null);
            writeMetadata(jGenerator, 3);
            writeBinary(jGenerator, 2);
            jGenerator.writeFieldName("alternativeIdentifiers");
            jGenerator.writeStartArray();
            jGenerator.writeEndArray();
            jGenerator.writeStringField("relations", null);
            jGenerator.writeEndObject();
//            for (int j = 0; j < size; j++) {
//                for (int j2 = 0; j2 < depth; j2++) {
//                    if (j2 == depth -1) {
//                        for (int k = 0; k < size; k++) {
//                            jGenerator.writeStringField(elements.get(j2).get(k), RandomStringUtils.randomAlphabetic(16));
//                        }
//                        for (int k = 0; k < depth-1; k++) {
//                            jGenerator.writeEndObject();
//                        }
//                    } else {
//                        jGenerator.writeFieldName(elements.get(j2).get(j));
//                        jGenerator.writeStartObject();
//                    }
//                }
//            }
            jGenerator.close();
            long time = System.currentTimeMillis();
            final HttpResponse resp1 = this.execute(Request.Post("http://localhost:9200/mih/mih")
                    .useExpectContinue()
                    .bodyString(writer.toString(), ContentType.APPLICATION_JSON))
                    .returnResponse();
            durations += System.currentTimeMillis() - time;
            if (i%100==0) {
                System.out.println("last 100 avg " + durations/100 + " ms");
                durations = 0;
//                try {
//                    Thread.sleep(30000);
//                } catch (InterruptedException e1) {}
            }
            i++;
        }
    }
    
    private void writeMetadata(JsonGenerator jGenerator, int count) throws IOException {
        jGenerator.writeFieldName("metadata");
        jGenerator.writeStartObject();
        for (int j = 0; j < count; j++) {
            jGenerator.writeFieldName(RandomStringUtils.randomAlphabetic(7));
            jGenerator.writeStartObject();
            jGenerator.writeStringField("name", RandomStringUtils.randomAlphabetic(7));
            jGenerator.writeNumberField("size", 317);
            jGenerator.writeStringField("mimetype", "text/xml");
            jGenerator.writeStringField("filename", "dc.xml");
            jGenerator.writeStringField("checksum", RandomStringUtils.randomAlphabetic(35));
            jGenerator.writeStringField("checksumType", "MD5");
            jGenerator.writeStringField("path", RandomStringUtils.randomAlphabetic(16));
            jGenerator.writeFieldName("source");
            jGenerator.writeStartObject();
            jGenerator.writeStringField("type", "MD5");
            jGenerator.writeStringField("uri", "/entity/" + RandomStringUtils.randomAlphabetic(16) + "/metadata/" + RandomStringUtils.randomAlphabetic(7) + "/content");
            jGenerator.writeBooleanField("internal", true);
            jGenerator.writeEndObject();
            jGenerator.writeStringField("type", "DC");
            jGenerator.writeBooleanField("indexInline", true);
            jGenerator.writeFieldName("jsonData");
            jGenerator.writeStartObject();
            jGenerator.writeFieldName("metadata");
            jGenerator.writeStartObject();
            jGenerator.writeStringField("title", "Test Object");
            jGenerator.writeStringField("creator", "fasseg");
            jGenerator.writeStringField("subject", "Testing Groven");
            jGenerator.writeStringField("description", "Test Object to implement integration Tests");
            jGenerator.writeEndObject();
            jGenerator.writeEndObject();
            jGenerator.writeStringField("utcCreated", ZonedDateTime.now(ZoneOffset.UTC).toString());
            jGenerator.writeStringField("utcLastModified", ZonedDateTime.now(ZoneOffset.UTC).toString());
            jGenerator.writeEndObject();
        }
        jGenerator.writeEndObject();
    }

    private void writeBinary(JsonGenerator jGenerator, int count) throws IOException {
        jGenerator.writeFieldName("binaries");
        jGenerator.writeStartObject();
        for (int j = 0; j < 2; j++) {
            jGenerator.writeFieldName("binary-" + RandomStringUtils.randomAlphabetic(16));
            jGenerator.writeStartObject();
            jGenerator.writeStringField("name", "binary-" + RandomStringUtils.randomAlphabetic(16));
            jGenerator.writeNumberField("size", 81920);
            jGenerator.writeStringField("mimetype", "application/octet-stream");
            writeMetadata(jGenerator, 2);
            jGenerator.writeStringField("filename", "binary-" + RandomStringUtils.randomAlphabetic(16) + ".bin");
            jGenerator.writeStringField("checksum", RandomStringUtils.randomAlphabetic(35));
            jGenerator.writeStringField("checksumType", "MD5");
            jGenerator.writeStringField("path", RandomStringUtils.randomAlphabetic(16));
            jGenerator.writeFieldName("source");
            jGenerator.writeStartObject();
            jGenerator.writeStringField("type", "MD5");
            jGenerator.writeStringField("uri", "/entity/" + RandomStringUtils.randomAlphabetic(16) + "/binary/binary-" + RandomStringUtils.randomAlphabetic(7) + "/content");
            jGenerator.writeBooleanField("internal", true);
            jGenerator.writeEndObject();
            jGenerator.writeStringField("utcCreated", ZonedDateTime.now(ZoneOffset.UTC).toString());
            jGenerator.writeStringField("utcLastModified", ZonedDateTime.now(ZoneOffset.UTC).toString());
            jGenerator.writeEndObject();
        }
        jGenerator.writeEndObject();
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
        return mapper.readValue(resp.getEntity().getContent(), Entity.class);
    }

    /**
     * Retrieve an entity using a HTTP GET from the Larch server
     * 
     * @param id the Id of the entity
     * @return An entity object
     */
    public InputStream retrieveEntityAsStream(String id) throws IOException {
        final HttpResponse resp = this.execute(Request.Get(larchUri + "/entity/" + id)
                .useExpectContinue()
                .addHeader("Accept", "application/json"))
                .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to retrieve entity at {}\n{}", larchUri, EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to retrieve Entity");
        }
        return resp.getEntity().getContent();
    }

    /**
     * Create new user with given name + password + UserRole for given level2Ids
     * 
     * @param name the name of the user
     * @param password the password of the user
     * @param level2ids the level2Ids to assign user-rights for
     * @return name of the new user
     */
    public String createUser(String name, String password, List<String> level2Ids) throws IOException {
        if (StringUtils.isBlank(name)) {
            name = RandomStringUtils.randomAlphabetic(5);
        }
        // Create User
        HttpResponse resp =
                this.execute(
                Request.Post(larchUri + "/user")
                        .body(MultipartEntityBuilder.create()
                                .addTextBody("name", name)
                                .addTextBody("first_name", "test")
                                .addTextBody("last_name", "test")
                                .addTextBody("email", name + "@fiz.de")
                                .build()
                        ).useExpectContinue())
                        .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to create user at {}\n{}", larchUri, EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to create user");
        }
        EntityUtils.consume(resp.getEntity());
        final String token = EntityUtils.toString(resp.getEntity());
        
        //Confirm User
        resp =
                this.execute(
                Request.Post(larchUri + "/confirm/" + token)
                        .body(MultipartEntityBuilder.create()
                                .addTextBody("password", password)
                                .addTextBody("passwordRepeat", password)
                                .build()
                        ).useExpectContinue()).returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to confirm user at {}\n{}", larchUri, EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to confirm user");
        }
        
        //Set Roles
        List<Role> roles = new ArrayList<Role>();
        Role userRole = new UserRole();
        List<RoleRight> userRoleRights = new ArrayList<RoleRight>();
        for (RoleRight roleRight : userRole.allowedRights()) {
            userRoleRights.add(roleRight);
        }
        if (!userRoleRights.isEmpty()) {
            List<Right> newRights = new ArrayList<Right>();
            for (String level2Id : level2Ids) {
                newRights.add(new Right(level2Id, userRoleRights));
            }
            userRole.setRights(newRights);
        }
        roles.add(userRole);
        createRoles(name, roles);

        return name;
    }

    /**
     * Create Roles for user with given username
     * 
     * @param username the name of the user
     * @param roles the roles to set
     * @return response-string
     */
    public String createRoles(String username, List<Role> roles) throws IOException {
        if (StringUtils.isBlank(username)) {
            log.error("Username may not be null");
            throw new IOException("Username may not be null");
        }
        final HttpResponse resp = this.execute(Request.Post(larchUri + "/user/" + username + "/roles")
                .useExpectContinue()
                .bodyString(mapper.writeValueAsString(roles), ContentType.APPLICATION_JSON))
                .returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            log.error("Unable to create roles at {}\n{}", larchUri, EntityUtils.toString(resp.getEntity()));
            throw new IOException("Unable to create roles: " + resp.getStatusLine().getReasonPhrase());
        }
        return EntityUtils.toString(resp.getEntity());
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

    /**
     * Get HttpUriRequest for given parameters.
     * 
     * @param method
     * @param url
     * @param body
     * @return
     * @throws IOException
     */
    private HttpUriRequest getRequest(String method, String url, Object body) throws IOException {
        if ("POST".equals(method)) {
            HttpPost httpPost =
                    new HttpPost(url);
            setBody(httpPost, body);
            return httpPost;
        } else if ("PUT".equals(method)) {
            HttpPut httpPut =
                    new HttpPut(url);
            setBody(httpPut, body);
            return httpPut;
        } else if ("PATCH".equals(method)) {
            HttpPatch httpPatch =
                    new HttpPatch(url);
            setBody(httpPatch, body);
            return httpPatch;
        } else if ("GET".equals(method)) {
            HttpGet httpGet = new HttpGet(url);
            return httpGet;
        } else if ("DELETE".equals(method)) {
            HttpDelete httpDelete = new HttpDelete(url);
            return httpDelete;
        }
        return null;
    }

    /**
     * Sets the body of the request.
     * 
     * @param request
     * @param body
     * @return HttpEntityEnclosingRequestBase
     * @throws IOException
     */
    private void setBody(HttpEntityEnclosingRequestBase request, Object body)
            throws IOException {
        if (body != null) {
            if (body instanceof String) {
                String bodyString = (String) body;
                if (StringUtils.isNotBlank(bodyString)) {
                    request.setEntity(new StringEntity(bodyString));
                    if (isJson(bodyString)) {
                        request.setHeader("Content-type", "application/json; charset=UTF-8");
                    } else {
                        request.setHeader("Content-type", ContentType.APPLICATION_FORM_URLENCODED.toString());
                    }
                }
            } else if (body instanceof HttpEntity) {
                request.setEntity((HttpEntity) body);
            }
        }
    }

    private boolean isJson(String text) {
        boolean isJson = false;
        JsonFactory f = new JsonFactory();
        try {
            JsonParser parser = f.createParser(text);
            while (parser.nextToken() != null) {
            }
            isJson = true;
        } catch (Exception e) {
        }
        return isJson;
    }

}
