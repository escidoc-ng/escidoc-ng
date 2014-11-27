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


package net.objecthunter.larch.service.backend.zip;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.source.UrlSource;
import net.objecthunter.larch.service.backend.BackendArchiveInformationPackageService;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;
import net.objecthunter.larch.service.backend.BackendEntityService;

import org.apache.commons.io.IOUtils;
import org.crsh.console.jline.internal.Log;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A service to create ZIP AIPs from an {@link net.objecthunter.larch.model.Entity} object
 */
public class ZIPArchiveInformationPackageService implements BackendArchiveInformationPackageService {

    @Autowired
    private BackendBlobstoreService blobstoreService;

    @Autowired
    private BackendEntityService entityService;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public void write(final Entity e, final OutputStream sink) throws IOException {
        final ZipOutputStream zipSink = new ZipOutputStream(sink);
        this.writeEntity("", e, zipSink);
        zipSink.finish();
        zipSink.flush();
    }

    private void writeEntity(final String prefix, final Entity e, final ZipOutputStream zipSink) throws IOException {
        /* write the binaries to the package */
        if (e.getBinaries()!= null) {
            for (final Binary bin : e.getBinaries().values()) {

                bin.setSource(new UrlSource(URI.create(prefix + "binaries/" + bin.getName() + "/" + bin.getFilename()), false));

                /* save the binary content */
                zipSink.putNextEntry(new ZipEntry(prefix + "binaries/" + bin.getName() + "/" + bin.getFilename()));
                InputStream in = null;
                try {
                    in = this.blobstoreService.retrieve(bin.getPath());
                    IOUtils.copy(in, zipSink);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            Log.warn("Problem closing input-stream " + bin.getPath());
                        }
                    }
                }
                zipSink.closeEntry();

                // update the path to point in the zip file
                bin.setPath(prefix + "binaries/" + bin.getName() + "/" + bin.getFilename());

                /* write the metadatas to the package */
                if (bin.getMetadata()!= null) {
                    for (final Metadata md : bin.getMetadata().values()) {
                        writeMetadata(prefix + "binaries/" + bin.getName() + "/metadata/", md, zipSink);
                    }
                }
            }
        }
        /* write the metadatas to the package */
        if (e.getMetadata()!= null) {
            for (final Metadata md : e.getMetadata().values()) {
                writeMetadata(prefix + "metadata/", md, zipSink);
            }
        }

        /* write the entity json to the package */
        zipSink.putNextEntry(new ZipEntry(prefix + "entity_" + e.getId() + ".json"));
        IOUtils.write(this.mapper.writeValueAsString(e), zipSink);
        zipSink.closeEntry();

        /* recurse for all child entities */
        for (final String childId : this.entityService.fetchChildren(e.getId())) {
            this.writeEntity(prefix + "child_" + childId + "/", this.entityService.retrieve(childId), zipSink);
        }
    }
    
    private void writeMetadata(final String prefix, final Metadata metadata, final ZipOutputStream zipSink) throws IOException {
        metadata.setSource(new UrlSource(URI.create(prefix + metadata.getName() + "/" + metadata.getFilename()), false));

        /* save the metadata content */
        zipSink.putNextEntry(new ZipEntry(prefix + metadata.getName() + "/" + metadata.getFilename()));
        InputStream in = null;
        try {
            in = this.blobstoreService.retrieve(metadata.getPath());
            IOUtils.copy(in, zipSink);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.warn("Problem closing input-stream " + metadata.getPath());
                }
            }
        }
        zipSink.closeEntry();

        // update the path to point in the zip file
        metadata.setPath(prefix + metadata.getName() + "/" + metadata.getFilename());
    }
    
}
