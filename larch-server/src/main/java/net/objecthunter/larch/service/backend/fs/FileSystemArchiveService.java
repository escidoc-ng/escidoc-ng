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
package net.objecthunter.larch.service.backend.fs;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.service.backend.BackendArchiveService;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileSystemArchiveService implements BackendArchiveService {

    @Value("${larch.archive.path}")
    private String archivePath;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private BackendBlobstoreService blobstoreService;

    private File directory;

    private static final Logger log = LoggerFactory.getLogger(FileSystemArchiveService.class);


    @PostConstruct
    public void init() {
        directory = new File(archivePath);
        if (!directory.exists()) {
            log.info("creating archive path " + directory.getAbsolutePath());
            final List<File> hierarchy = new ArrayList<>();
            File current = directory;
            hierarchy.add(directory);
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
        if (!directory.canWrite()) {
            throw new IllegalArgumentException("Insufficient permissions to write to " + directory.getAbsolutePath());
        }
        if (!directory.canRead()) {
            throw new IllegalArgumentException("Insufficient permissions to read from " + directory.getAbsolutePath());
        }

    }

    @Override
    public void saveOrUpdate(final Entity e) throws IOException {
        log.info("Creating archival package");
        checkExistsAndIsReadable(directory);
        final File target = getZipFile(e.getId(), e.getVersion());

        if (target.exists() && !target.canWrite()) {
            throw new IOException("Insufficient permissions to write to " + target.getAbsolutePath());
        }

        /* save the entity by first writing to a tmp file and then moving it to the right place */
        final File tmpNew = File.createTempFile("entity", "zip");
        this.writeEntityToZip(e, new FileOutputStream(tmpNew));
        if (target.exists()) {
            final File orig = File.createTempFile("entity", "zip");
            Files.move(target.toPath(), orig.toPath(), StandardCopyOption.ATOMIC_MOVE);
            Files.move(tmpNew.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE);
            orig.delete();
            tmpNew.delete();
        } else {
            Files.move(tmpNew.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE);
            tmpNew.delete();
        }
    }

    /**
     * This method is duplicated in the {@link net.objecthunter.larch.service.backend.sftp.SftpArchiveService} in favour of the not creating the spaghetti incident
     * via polymorphism
     */
    private void writeEntityToZip(final Entity e, final OutputStream sink) throws IOException {
        final ZipOutputStream zipSink = new ZipOutputStream(sink);
            /* write the entity xml to the package */
        zipSink.putNextEntry(new ZipEntry("entity_" + e.getId() + ".json"));
        IOUtils.write(this.mapper.writeValueAsString(e), zipSink);
        zipSink.closeEntry();

            /* write the metadata to the package */
        for (final Metadata md : e.getMetadata().values()) {
            zipSink.putNextEntry(new ZipEntry("metadata_" + md.getName() + ".json"));
            IOUtils.write(this.mapper.writeValueAsString(md), zipSink);
            zipSink.closeEntry();
        }

            /* write the binaries to the package */
        for (final Binary bin : e.getBinaries().values()) {

                /* first the binary itself */
            zipSink.putNextEntry(new ZipEntry("binaries/" + bin.getName() + "/" + bin.getName() + ".json"));
            IOUtils.write(this.mapper.writeValueAsString(bin), zipSink);
            zipSink.closeEntry();

                /* save the metadata */
            for (final Metadata md : bin.getMetadata().values()) {
                zipSink.putNextEntry(new ZipEntry("binaries/" + bin.getName() + "/metadata_" + md.getName() + ".json"));
                IOUtils.write(this.mapper.writeValueAsString(md), zipSink);
                zipSink.closeEntry();
            }

                /* save the binary content */
            zipSink.putNextEntry(new ZipEntry("binaries/" + bin.getName() + "/" + bin.getFilename()));
            IOUtils.copy(this.blobstoreService.retrieve(bin.getPath()), zipSink);
            zipSink.closeEntry();
        }
        zipSink.finish();
        zipSink.close();
    }

    @Override
    public InputStream retrieve(final String entityId, final int version) throws IOException {
        log.debug("retrieving archival package fo entity " + entityId);
        final File zip = getZipFile(entityId, version);
        this.checkExistsAndIsReadable(zip);
        return new FileInputStream(zip);
    }

    private File getZipFile(String id, int version) {
        return new File(directory, "aip_" + id + "_v" + version + ".zip");
    }

    private void checkExistsAndIsReadable(File zip) throws IOException {
        if (!zip.exists()) {
            throw new FileNotFoundException("The zip file " + zip.getAbsolutePath() + " could not be found");
        }
        if (!zip.canRead()) {
            throw new IOException("Unable to read AIP " + zip.getAbsolutePath());
        }
    }

    private void checkExistsAndIsWritable(File zip) throws IOException {
        if (!zip.exists()) {
            throw new FileNotFoundException("The zip file " + zip.getAbsolutePath() + " could not be found");
        }
        if (!zip.canRead()) {
            throw new IOException("Unable to read AIP " + zip.getAbsolutePath());
        }
    }

    @Override
    public void delete(final String entityId, final int version) throws IOException {
        log.info("Deleting archival package");
        final File aip = getZipFile(entityId, version);
        checkExistsAndIsWritable(aip);
        aip.delete();
    }

    @Override
    public boolean exists(final String entityId, final int version) throws IOException {
        return getZipFile(entityId, version).exists();
    }

    @Override
    public long sizeOf(String entityId, int version) throws IOException {
        final File aip =  getZipFile(entityId,version);
        if (!aip.exists()) {
            throw new FileNotFoundException("No archive for entity " + entityId + " with version " + version + " could be found");
        }
        if (!aip.canRead()) {
            throw new IOException("Insufficient permissions to read from archive " + aip.getAbsolutePath());
        }
        return aip.length();

    }
}
