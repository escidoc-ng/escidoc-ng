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
package de.escidocng.service.backend.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.escidocng.model.Entity;
import de.escidocng.service.backend.BackendArchiveBlobService;
import de.escidocng.service.backend.BackendArchiveInformationPackageService;
import de.escidocng.service.backend.BackendBlobstoreService;

public class FileSystemArchiveService implements BackendArchiveBlobService {

    @Value("${archive.fs.path}")
    private String archivePath;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private BackendBlobstoreService blobstoreService;

    @Autowired
    private BackendArchiveInformationPackageService aipService;

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
        try (final OutputStream sink = new FileOutputStream(tmpNew)){
            this.aipService.write(e, sink);
        }

        if (target.exists()) {
            final File orig = File.createTempFile("entity", "zip");
            Files.move(target.toPath(), orig.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.move(tmpNew.toPath(), target.toPath());
            orig.delete();
            tmpNew.delete();
        } else {
            Files.move(tmpNew.toPath(), target.toPath());
            tmpNew.delete();
        }
        return target.getAbsolutePath();
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
