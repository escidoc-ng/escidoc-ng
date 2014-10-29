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

import net.objecthunter.larch.service.ArchiveService;
import net.objecthunter.larch.service.EntityService;
import net.objecthunter.larch.service.backend.BackendArchiveService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;

public class DefaultArchiveService implements ArchiveService {

    @Autowired
    private BackendArchiveService archive;

    @Autowired
    private EntityService entityService;

    @Override
    public void archive(final String entityId, final int version) throws IOException {
        archive.saveOrUpdate(entityService.retrieve(entityId, version));
    }

    @Override
    public boolean isArchived(final String entityId, final int version) throws IOException {
        return archive.exists(entityId, version);
    }

    @Override
    public InputStream retrieve(final String entityId, final int version) throws IOException {
        return archive.retrieve(entityId, version);
    }

    @Override
    public void delete(final String entityId, final int version) throws IOException {
        archive.delete(entityId, version);
    }
}
