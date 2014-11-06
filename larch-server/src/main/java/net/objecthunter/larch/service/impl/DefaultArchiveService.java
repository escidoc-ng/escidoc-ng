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

import net.objecthunter.larch.model.Archive;
import net.objecthunter.larch.service.ArchiveService;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.backend.BackendArchiveBlobService;
import net.objecthunter.larch.service.backend.BackendArchiveIndexService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;

public class DefaultArchiveService implements ArchiveService {

    @Autowired
    private BackendArchiveBlobService archiveBlobStore;

    @Autowired
    private BackendArchiveIndexService archiveIndex;

    @Autowired
    private EntityService entityService;

    @Override
    public void archive(final String entityId, final int version) throws IOException {
        final String path = archiveBlobStore.saveOrUpdate(entityService.retrieve(entityId, version));
        Archive a = new Archive();
        a.setEntityId(entityId);
        a.setEntityVersion(version);
        a.setCreatedDate(ZonedDateTime.now());
        a.setLastModifedDate(ZonedDateTime.now());
        a.setPath(path);
        archiveIndex.saveOrUpdate(a);
    }

    @Override
    public boolean isArchived(final String entityId, final int version) throws IOException {
        return archiveIndex.exists(entityId, version);
    }

    @Override
    public InputStream retrieveData(final String entityId, final int version) throws IOException {
        final Archive a = archiveIndex.retrieve(entityId, version);
        return archiveBlobStore.retrieve(a.getPath());
    }

    @Override
    public void delete(final String entityId, final int version) throws IOException {
        final Archive a = archiveIndex.retrieve(entityId, version);
        archiveIndex.delete(entityId, version);
        archiveBlobStore.delete(a.getPath());
    }

    @Override
    public long sizeof(String entityId, int version) throws IOException {
        Archive a = archiveIndex.retrieve(entityId, version);
        return archiveBlobStore.sizeOf(a.getPath());
    }

    @Override
    public Archive retrieve(String entityId, int version) throws IOException {
        return archiveIndex.retrieve(entityId, version);
    }

    @Override
    public List<Archive> list(int offset, int count) throws IOException {
        return this.archiveIndex.list(offset, count);
    }
}
