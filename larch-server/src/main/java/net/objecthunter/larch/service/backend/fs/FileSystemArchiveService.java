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
import net.objecthunter.larch.model.source.UrlSource;
import net.objecthunter.larch.service.backend.BackendArchiveBlobService;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileSystemArchiveService implements BackendArchiveBlobService {

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
    public String saveOrUpdate(final Entity e) throws IOException {
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
        return target.getAbsolutePath();
    }

    /**
     * This method is duplicated in the {@link net.objecthunter.larch.service.backend.sftp.SftpArchiveService} in favour of the not creating the spaghetti incident
     * via polymorphism
     */
    private void writeEntityToZip(final Entity e, final OutputStream sink) throws IOException {
        final ZipOutputStream zipSink = new ZipOutputStream(sink);


        /* write the binaries to the package */
        for (final Binary bin : e.getBinaries().values()) {

            // Update the location to point into the current directory
            bin.setSource(new UrlSource(URI.create("./binaries/" + bin.getName() + "/" + bin.getFilename())));

            /* save the binary content */
            zipSink.putNextEntry(new ZipEntry("binaries/" + bin.getName() + "/" + bin.getFilename()));
            IOUtils.copy(this.blobstoreService.retrieve(bin.getPath()), zipSink);
            zipSink.closeEntry();
        }

        /* write the entity json to the package */
        zipSink.putNextEntry(new ZipEntry("entity_" + e.getId() + ".json"));
        IOUtils.write(this.mapper.writeValueAsString(e), zipSink);
        zipSink.closeEntry();

        zipSink.finish();
        zipSink.close();
    }

    @Override
    public InputStream retrieve(final String path) throws IOException {
        final File zip = new File(path);
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
    public void delete(final String path) throws IOException {
        log.info("Deleting archival package");
        final File aip = new File(path);
        checkExistsAndIsWritable(aip);
        aip.delete();
    }

    @Override
    public long sizeOf(final String path) throws IOException {
        final File aip =  new File(path);
        if (!aip.exists()) {
            throw new FileNotFoundException("No archive " + path);
        }
        if (!aip.canRead()) {
            throw new IOException("Insufficient permissions to read from archive " + aip.getAbsolutePath());
        }
        return aip.length();

    }
}
