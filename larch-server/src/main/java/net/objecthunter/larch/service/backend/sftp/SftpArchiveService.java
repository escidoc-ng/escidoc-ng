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
import net.objecthunter.larch.service.ArchiveService;
import net.objecthunter.larch.service.backend.BackendArchiveService;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.SftpClient;
import org.apache.sshd.common.SshException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SftpArchiveService extends AbstractSftpService implements BackendArchiveService {

    @Autowired
    private BackendBlobstoreService blobstoreService;

    @Value("${larch.archive.path}")
    private String archivePath;

    @Override
    public InputStream retrieve(final String entityId, final int version) throws IOException {

        ensureConnected();

        if (entityId == null || entityId.isEmpty()) {
            throw new IOException("Entity id can not be empty");
        }

        final String path = archivePath + "/" + getFilename(entityId, version);

        if (!exists(entityId, version)) {
            throw new FileNotFoundException("Unable to find " + path + " on the SFTP server");
        }

        return sftp.read(path);
    }

    @Override
    public void saveOrUpdate(final Entity e) throws IOException {

        if (e == null) {
            throw new IOException("Unable to archive null entity");
        }

        ensureConnected();

        ensureDirectoryExists(archivePath);
        final String fileName = getFilename(e.getId(), e.getVersion());
        final String path = archivePath + "/" + fileName;

        try (final OutputStream sink = sftp.write(path)) {
            this.writeEntityToZip(e, sink);
        }
    }

    protected void ensureDirectoryExists(String path) throws IOException{
        if (!exists(path)) {
            sftp.mkdir(path);
        }
    }

    private String getFilename(final String id, final int version) {
        return "aip_" + id + "_v" + version + ".zip";
    }

    @Override
    public void delete(final String entityId, final int version) throws IOException {
        final String fileName = getFilename(entityId, version);
        final String path = archivePath + "/" + fileName;
        sftp.remove(path);
    }

    @Override
    public boolean exists(final String entityId, final int version) throws IOException {
        final String fileName = getFilename(entityId, version);
        final String path = archivePath + "/" + fileName;
        return exists(path);
    }

    protected boolean exists(final String path) throws IOException {
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

    @Override
    public long sizeOf(final String entityId, final int version) throws IOException {
        final String fileName = getFilename(entityId, version);
        final String path = archivePath + "/" + fileName;
        final SftpClient.Attributes attrs = sftp.stat(path);
        if (attrs == null) {
            throw new FileNotFoundException("Unable to locate archive " + fileName);
        }
        return attrs.size;
    }

    /*
     * This method is duplicated in the {@link FileSystemArchiveService}Service in favour of the not creating the
     * spaghetti incident via polymorphism
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
        zipSink.flush();
    }

}
