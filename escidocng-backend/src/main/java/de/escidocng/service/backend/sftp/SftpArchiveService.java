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

package de.escidocng.service.backend.sftp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.UUID;

import org.apache.sshd.client.SftpClient;
import org.apache.sshd.common.SshException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import de.escidocng.model.Entity;
import de.escidocng.service.backend.BackendArchiveBlobService;
import de.escidocng.service.backend.BackendArchiveInformationPackageService;
import de.escidocng.service.backend.BackendBlobstoreService;
import de.escidocng.util.SftpUtil;

public class SftpArchiveService implements BackendArchiveBlobService {

    @Autowired
    private BackendBlobstoreService blobstoreService;

    @Autowired
    private BackendArchiveInformationPackageService aipService;

    @Autowired
    protected Environment env;

    @Value("${archive.sftp.path}")
    private String archivePath;

    private SftpClient sftp;

    @Override
    public InputStream retrieve(final String path) throws IOException {
        return getSftpClient().read(path);
    }

    @Override
    public String saveOrUpdate(final Entity e) throws IOException {
        if (e == null) {
            throw new IOException("Unable to archive null entity");
        }
        ensureDirectoryExists(archivePath);
        final String fileName = "aip_" + UUID.randomUUID() + ".zip";
        final String path = archivePath + "/" + fileName;

        try (final OutputStream sink = getSftpClient().write(path)) {
            this.aipService.write(e, sink);
        }
        return path;
    }

    @Override
    public void delete(final String path) throws IOException {
        getSftpClient().remove(path);
    }

    @Override
    public long sizeOf(final String path) throws IOException {
        final SftpClient.Attributes attrs = getSftpClient().stat(path);
        if (attrs == null) {
            throw new FileNotFoundException("Unable to locate archive " + path);
        }
        return attrs.size;
    }

    private void ensureDirectoryExists(String path) throws IOException {
        if (!exists(path)) {
            getSftpClient().mkdir(path);
        }
    }

    private boolean exists(final String path) throws IOException {
        final SftpClient.Handle handle = getSftpClient().open(path, EnumSet.of(SftpClient.OpenMode.Read));
        try {
            getSftpClient().stat(handle);
            return true;
        } catch (SshException e) {
            // ugly flow control by exception handling, but there is no check for existence method in the sftp client
            // it seems, and this is all I could get to work
            if (e.getMessage().startsWith("SFTP error (2)")) {
                return false;
            }
            throw new IOException(e);
        }
    }

    private SftpClient getSftpClient() throws IOException {
        if (sftp == null) {
            try {
                sftp =
                        SftpUtil.getSftpClient(env.getRequiredProperty("archive.sftp.user"),
                                env.getRequiredProperty("archive.sftp.passwd"),
                                env.getRequiredProperty("archive.sftp.host"),
                                Integer.parseInt(env.getRequiredProperty("archive.sftp.port")),
                                env.getRequiredProperty("archive.sftp.basepath"));
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
        }
        return sftp;
    }
}
