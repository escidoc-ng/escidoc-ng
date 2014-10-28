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
package net.objecthunter.larch.service.impl;

import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.service.ArchiveService;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileSystemArchiveService implements ArchiveService {

    @Value("${larch.archive.path}")
    private String archivePath;

    @Autowired
    private Marshaller marshaller;

    @Autowired
    private Unmarshaller unmarshaller;

    @Autowired
    private BackendBlobstoreService blobstoreService;

    private File directory;

    private static final Logger log = LoggerFactory.getLogger(FileSystemArchiveService.class);

    @PostConstruct
    public void init() {
        if (archivePath == null || archivePath.isEmpty()) {
            throw new IllegalArgumentException("The archive path can not be empty");
        }
        directory = new File(archivePath);
        if (!directory.exists()) {
            log.info("creating archive path " + directory.getAbsolutePath());
            final List<File> hierarchy = new ArrayList<>();
            File current = directory;
            while (current.getParentFile() != null) {
                hierarchy.add(current.getParentFile());
                current = current.getParentFile();
            }
            Collections.reverse(hierarchy);
            for (final File dir : hierarchy) {
                if (!dir.exists()) {
                    log.info("Creating directory " + dir.getAbsolutePath());
                    dir.mkdir();
                }
            }
        }

    }

    @Override
    public void create(final Entity e) throws IOException {
        log.info("Creating archival package");
        final String fileName = "aip_" + e.getId() + ".zip";
        final File target = new File(directory, fileName);
        final ZipOutputStream sink = new ZipOutputStream(new FileOutputStream(target));
        try {
            /* write the entity xml to the package */
            sink.putNextEntry(new ZipEntry("entity_" + e.getId() + ".xml"));
            this.marshaller.marshal(e, sink);
            sink.closeEntry();

            /* write the metadata to the package */
            for (Metadata md : e.getMetadata().values()) {
                sink.putNextEntry(new ZipEntry("metadata_" + md.getName() + ".xml"));
                this.marshaller.marshal(md, sink);
                sink.closeEntry();
            }

            /* write the binaries to the package */
            for (Binary bin : e.getBinaries().values()) {

                /* first the binary itself */
                sink.putNextEntry(new ZipEntry("binaries/" + bin.getName() + "/" + bin.getName() + ".xml"));
                this.marshaller.marshal(bin, sink);
                sink.closeEntry();

                /* save the metadata */
                for (Metadata md : bin.getMetadata().values()) {
                    sink.putNextEntry(new ZipEntry("binaries/" + bin.getName() + "/metadata_" + md.getName() + ".xml"));
                    this.marshaller.marshal(md, sink);
                    sink.closeEntry();
                }

                /* save the binary content */
                sink.putNextEntry(new ZipEntry("binaries/" + bin.getName() + "/" + bin.getFilename()));
                IOUtils.copy(this.blobstoreService.retrieve(bin.getPath()), sink);
                sink.closeEntry();
            }
        } catch (JAXBException ex) {
            log.error("Unable to marshal entity " + e.getId(), ex);
            throw new IOException(ex);
        }

    }

    @Override
    public Entity retrieve(final Entity e) throws IOException {
        try {
            final File zip = new File(directory, "aip_" + e.getId() + ".zip");
            if (!zip.exists()) {
                throw new FileNotFoundException("The zip file " + zip.getAbsolutePath() + " could not be found");
            }
            final ZipInputStream src = new ZipInputStream(new FileInputStream(zip));

            ZipEntry entry;
            Entity entity = null;
            final List<Metadata> entityMetadata = new ArrayList<>();
            final List<Binary> binaries = new ArrayList<>();
            final Map<String, Map<String, Metadata>> binaryMetadata = new HashMap<String, Map<String, Metadata>>();

            while ((entry = src.getNextEntry()) != null) {
                final String entryName = entry.getName();
                if (entryName.equals("entity_" + e.getId() + ".xml")) {
                    entity = (Entity) this.unmarshaller.unmarshal(src);
                }
                if (entryName.startsWith("binaries/") && entryName.endsWith(".xml")) {
                    final String binName = entryName.substring(9, entryName.indexOf('/',9));
                    if (entryName.contains("/metadata_")) {
                        /* read binary metadata */
                        Metadata md = (Metadata) this.unmarshaller.unmarshal(src);
                        if (binaryMetadata.containsKey(binName)) {
                            binaryMetadata.put(binName, new HashMap<>());
                        }
                        binaryMetadata.get(binName).put(md.getName(), md);
                    }else {
                        /* read the binary xml */
                        Binary bin = (Binary) this.unmarshaller.unmarshal(src);
                        binaries.add(bin);
                    }
                }
                if (entryName.startsWith("metadata_")) {
                    entityMetadata.add((Metadata) this.unmarshaller.unmarshal(src));
                }
            }
            src.close();

            for (final Metadata md : entityMetadata) {
                entity.getMetadata().put(md.getName(), md);
                for (Binary bin: binaries) {
                    if (binaryMetadata.containsKey(bin.getName())) {
                        for (Metadata binMd : binaryMetadata.get(bin.getName()).values()) {
                            bin.getMetadata().put(binMd.getName(), binMd);
                        }
                    }
                    entity.getBinaries().put(bin.getName(), bin);
                }
            }
        } catch (JAXBException ex) {
            log.error("Unable to read entity from archive", ex);
            throw new IOException(ex);
        }
    }

    @Override
    public void update(final Entity e) throws IOException {
        log.info("updating archival package");

    }

    @Override
    public void delete(final String entityId) throws IOException {
        log.info("Deleting archival package");
    }
}
