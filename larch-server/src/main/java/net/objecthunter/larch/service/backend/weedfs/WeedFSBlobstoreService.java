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

package net.objecthunter.larch.service.backend.weedfs;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.state.WeedFsBlobstoreState;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementation of a {@link net.objecthunter.larch.service.backend.BackendBlobstoreService} built on top of the
 * distributed file system WeedFS
 */
public class WeedFSBlobstoreService implements BackendBlobstoreService {

    private static final Logger log = LoggerFactory.getLogger(WeedFSBlobstoreService.class);

    @Autowired
    private Environment env;

    @Autowired
    private ObjectMapper mapper;

    private String weedfsUrl;

    @PostConstruct
    public void init() {
        this.weedfsUrl =
                "http://" + env.getProperty("blobstore.weedfs.master.host") + ":" + env.getProperty("blobstore.weedfs.master.port");
    }

    @Override
    public String create(InputStream src) throws IOException {
        final JsonNode json = retrieveFid();
        final String fid = json.get("fid").textValue();
        log.debug("WeedFS returned fid {} for file creation", fid);

        // secondly post the file contents to the assigned volumeserver using the fid
        final String volumeUrl = "http://" + json.get("url").textValue() + "/";
        final HttpResponse resp =
                Request
                        .Post(volumeUrl + fid).body(
                                MultipartEntityBuilder.create().addBinaryBody("data", src).build())
                        .execute().returnResponse();
        if (resp.getStatusLine().getStatusCode() != 201) {
            throw new IOException("WeedFS returned HTTP " + resp.getStatusLine().getStatusCode() + "\n"
                    + EntityUtils.toString(resp.getEntity()));
        }
        log.debug("WeedFS wrote {} bytes", mapper.readTree(resp.getEntity().getContent()).get("size").asInt());
        return fid;
    }

    private JsonNode retrieveFid() throws IOException {
        HttpResponse resp = Request.Get(weedfsUrl + "/dir/assign").execute().returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            throw new IOException("WeedFS returned:\n" + EntityUtils.toString(resp.getEntity()));
        }
        return mapper.readTree(resp.getEntity().getContent());
    }

    @Override
    public InputStream retrieve(String fid) throws IOException {
        final HttpResponse resp = Request.Get(lookupVolumeUrl(fid)).execute().returnResponse();
        if (resp.getStatusLine().getStatusCode() == 404) {
            throw new NotFoundException(fid + " could not be found in WeedFS");
        }
        if (resp.getStatusLine().getStatusCode() != 200) {
            throw new IOException("WeedFS returned HTTP " + resp.getStatusLine().getStatusCode() + "\n"
                    + EntityUtils.toString(resp.getEntity()));
        }
        return resp.getEntity().getContent();
    }

    @Override
    public void delete(String fid) throws IOException {
        log.debug("deleting blob " + fid);
        final HttpResponse resp = Request.Delete(lookupVolumeUrl(fid)).execute().returnResponse();
        if (resp.getStatusLine().getStatusCode() != 202) {
            throw new IOException("WeedFS returned HTTP " + resp.getStatusLine().getStatusCode() + "\n"
                    + EntityUtils.toString(resp.getEntity()));
        }
    }

    @Override
    public void update(String fid, InputStream src) throws IOException {
        final HttpResponse resp =
                Request
                        .Post(lookupVolumeUrl(fid)).body(
                                MultipartEntityBuilder.create().addBinaryBody("path", src).build())
                        .execute().returnResponse();
        if (resp.getStatusLine().getStatusCode() != 201) {
            throw new IOException("WeedFS returned:\n" + EntityUtils.toString(resp.getEntity()));
        }
        log.debug("WeedFS updated {} bytes", mapper.readTree(resp.getEntity().getContent()).get("size").asInt());
    }

    @Override
    public WeedFsBlobstoreState status() throws IOException {
        final HttpResponse resp = Request.Get(this.weedfsUrl + "/dir/status").execute().returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            throw new IOException("WeedFS returned HTTP " + resp.getStatusLine().getStatusCode() + "\n"
                    + EntityUtils.toString(resp.getEntity()));
        }
        final JsonNode node = mapper.readTree(resp.getEntity().getContent());
        final JsonNode topology = node.get("Topology");
        final WeedFsBlobstoreState state = new WeedFsBlobstoreState();
        state.setFree(topology.get("Free").asLong());
        state.setMax(topology.get("Max").asLong());
        state.setVersion(node.get("Version").textValue());
        return state;
    }

    @Override
    public String createOldVersionBlob(Entity oldVersion) throws IOException {
        final JsonNode json = retrieveFid();
        final String fid = json.get("fid").textValue();
        log.debug("WeedFS returned fid {} for file creation", fid);

        // secondly post the file contents to the assigned volumeserver using the fid
        final String volumeUrl = "http://" + json.get("url").textValue() + "/";
        final HttpResponse resp =
                Request
                        .Post(volumeUrl + fid)
                        .body(
                                MultipartEntityBuilder.create().addBinaryBody("data",
                                        mapper.writeValueAsBytes(
                                                oldVersion)).build())
                        .execute().returnResponse();
        if (resp.getStatusLine().getStatusCode() != 201) {
            throw new IOException("WeedFS returned HTTP " + resp.getStatusLine().getStatusCode() + "\n"
                    + EntityUtils.toString(resp.getEntity()));
        }
        log.debug("WeedFS wrote {} bytes", mapper.readTree(resp.getEntity().getContent()).get("size").asInt());
        return fid;
    }

    @Override
    public InputStream retrieveOldVersionBlob(String fid) throws IOException {
        return this.retrieve(fid);
    }

    private String lookupVolumeUrl(String fid) throws IOException {
        final int separator = fid.indexOf(',');
        final String volumeId = fid.substring(0, fid.indexOf(','));
        final String id = fid.substring(separator + 1);
        final HttpResponse resp =
                Request.Get(this.weedfsUrl + "/dir/lookup?volumeId=" + volumeId).execute().returnResponse();
        if (resp.getStatusLine().getStatusCode() != 200) {
            throw new IOException("WeedFS returned HTTP " + resp.getStatusLine().getStatusCode() + "\n"
                    + EntityUtils.toString(resp.getEntity()));
        }
        final JsonNode json = mapper.readTree(resp.getEntity().getContent());
        return "http://" + json.get("locations").get(0).get("url").textValue() + "/" + fid;
    }

}
