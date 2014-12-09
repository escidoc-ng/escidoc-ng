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

package net.objecthunter.larch.service.backend.fs;

import static net.objecthunter.larch.util.FileSystemUtil.checkAndCreate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.state.FilesystemBlobstoreState;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementation of a {@link net.objecthunter.larch.service.backend.BackendBlobstoreService} on a Posix file system.
 * The service gets initialized using two user set directories for saving the content of the repository
 */
public class FileSystemBlobstoreService implements BackendBlobstoreService {

    private static final Logger log = LoggerFactory.getLogger(FileSystemBlobstoreService.class);

    @Autowired
    private Environment env;

    @Autowired
    private ObjectMapper mapper;

    private File directory;

    private File oldVersionDirectory;

    @PostConstruct
    public void init() throws IOException {
        this.directory = new File(env.getProperty("blobstore.fs.path"));
        this.oldVersionDirectory = new File(env.getProperty("blobstore.fs.oldversion.path"));
        checkAndCreate(this.directory);
        checkAndCreate(this.oldVersionDirectory);
    }

    @Override
    public String create(InputStream src) throws IOException {
        final File folder = new File(this.directory, RandomStringUtils.randomAlphabetic(2));
        checkAndCreate(folder);
        File data;
        do {
            /* create a new random file name */
            data = new File(folder, RandomStringUtils.randomAlphabetic(16));
        } while (data.exists());
        log.debug("creating Blob at {}", data.getAbsolutePath());
        try(final FileOutputStream sink = new FileOutputStream(data)) {
            IOUtils.copy(src, sink);
        }
        return folder.getName() + "/" + data.getName();
    }

    @Override
    public InputStream retrieve(String path) throws IOException {
        try {
            return new FileInputStream(new File(directory, path));
        } catch (FileNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @Override
    public void delete(String path) throws IOException {
        final File f = new File(directory, path);
        if (!f.delete()) {
            throw new IOException("Unable to delete file " + f.getAbsolutePath());
        }
    }

    @Override
    public void update(String path, InputStream src) throws IOException {
        final File data = new File(directory, path);
        if (!data.exists()) {
            throw new NotFoundException(data.getAbsolutePath() + " can not be updated sine it does not exist");
        }
        try (final OutputStream sink = new FileOutputStream(data)) {
            IOUtils.copy(src, sink);
        }
    }

    @Override
    public FilesystemBlobstoreState status() throws IOException {
        FilesystemBlobstoreState state = new FilesystemBlobstoreState();
        state.setPath(this.directory.getAbsolutePath());
        state.setTotalSpace(this.directory.getTotalSpace());
        state.setFreeSpace(this.directory.getFreeSpace());
        state.setUsableSpace(this.directory.getUsableSpace());
        return state;
    }

    @Override
    public String createOldVersionBlob(Entity oldVersion) throws IOException {
        final File folder = new File(this.oldVersionDirectory, RandomStringUtils.randomAlphabetic(2));
        checkAndCreate(folder);
        File data;
        do {
            /* create a new random file name */
            data = new File(folder, RandomStringUtils.randomAlphabetic(16));
        } while (data.exists());
        try (final OutputStream sink = new FileOutputStream(data)) {
            mapper.writeValue(sink, oldVersion);
            return folder.getName() + "/" + data.getName();
        }
    }

    @Override
    public InputStream retrieveOldVersionBlob(String path) throws IOException {
        return new FileInputStream(new File(oldVersionDirectory, path));
    }
}
