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
import java.util.List;

import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Describe;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.state.LarchState;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
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
                .body(MultipartEntityBuilder.create()
                        .addTextBody("name", "test")
                        .addTextBody("mimetype", "image/png")
                        .addPart(
                                "binary",
                                new InputStreamBody(src, "image/png"))
                        .build()))
//                .bodyStream(src))
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
        System.out.println(EntityUtils.toString(resp.getEntity()));
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
        return resp.getEntity().getContent();
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
