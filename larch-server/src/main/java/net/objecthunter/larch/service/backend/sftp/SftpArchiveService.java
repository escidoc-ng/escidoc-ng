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

package net.objecthunter.larch.service.backend.sftp;

import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.source.UrlSource;
import net.objecthunter.larch.service.backend.BackendArchiveBlobService;
import net.objecthunter.larch.service.backend.BackendArchiveInformationPackageService;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;
import org.apache.commons.io.IOUtils;
import org.apache.sshd.client.SftpClient;
import org.apache.sshd.common.SshException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.EnumSet;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SftpArchiveService extends AbstractSftpService implements BackendArchiveBlobService {

    @Autowired
    private BackendBlobstoreService blobstoreService;

    @Autowired
    private BackendArchiveInformationPackageService aipService;

    @Value("${larch.archive.path}")
    private String archivePath;

    @Override
    public InputStream retrieve(final String path) throws IOException {

        ensureConnected();

        return sftp.read(path);
    }

    @Override
    public String saveOrUpdate(final Entity e) throws IOException {

        if (e == null) {
            throw new IOException("Unable to archive null entity");
        }

        ensureConnected();

        ensureDirectoryExists(archivePath);
        final String fileName = "aip_" + UUID.randomUUID() + ".zip";
        final String path = archivePath + "/" + fileName;

        try (final OutputStream sink = sftp.write(path)) {
            this.aipService.write(e, sink);
        }
        return path;
    }

    protected void ensureDirectoryExists(String path) throws IOException {
        if (!exists(path)) {
            sftp.mkdir(path);
        }
    }

    @Override
    public void delete(final String path) throws IOException {
        sftp.remove(path);
    }

    @Override
    public long sizeOf(final String path) throws IOException {
        final SftpClient.Attributes attrs = sftp.stat(path);
        if (attrs == null) {
            throw new FileNotFoundException("Unable to locate archive " + path);
        }
        return attrs.size;
    }

    private boolean exists(final String path) throws IOException {
        final SftpClient.Handle handle = sftp.open(path, EnumSet.of(SftpClient.OpenMode.Read));
        try {
            SftpClient.Attributes attrs = sftp.stat(handle);
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

}
