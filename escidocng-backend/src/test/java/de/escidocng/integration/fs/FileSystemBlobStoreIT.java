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

package de.escidocng.integration.fs;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.escidocng.exceptions.NotFoundException;
import de.escidocng.model.state.FilesystemBlobstoreState;
import de.escidocng.service.backend.fs.FileSystemBlobstoreService;

public class FileSystemBlobStoreIT extends AbstractFSEscidocngIT {

    @Autowired
    private FileSystemBlobstoreService blobstoreService;

    private static final Charset cs = Charset.forName("UTF-8");

    @Test
    public void testCreateAndRetrieve() throws Exception {
        String data = "mysimpledatawithÄ";
        String path = blobstoreService.create(new ByteArrayInputStream(data.getBytes(cs)));
        try (InputStream src = blobstoreService.retrieve(path)) {
            assertEquals(data, IOUtils.toString(src, cs));
        }
    }

    @Test
    public void testUpdateAndRetrieve() throws Exception {
        String data = "mysimpledatawithö";
        String path = blobstoreService.create(new ByteArrayInputStream(data.getBytes(cs)));
        String update = "mysimpledatawithßandé";
        blobstoreService.update(path, new ByteArrayInputStream(update.getBytes(cs)));
        try (InputStream src = blobstoreService.retrieve(path)) {
            assertEquals(update, IOUtils.toString(src, cs));
        }
    }

    @Test(expected = NotFoundException.class)
    public void testCreateAndDelete() throws Exception {
        String data = "mysimpledatawithÄ";
        String path = blobstoreService.create(new ByteArrayInputStream(data.getBytes(cs)));
        blobstoreService.delete(path);
        try (InputStream src = blobstoreService.retrieve(path)) {
            src.close();
        }
    }

    @Test
    public void testgetStatus() throws Exception {
        FilesystemBlobstoreState state = blobstoreService.status();
    }
}
