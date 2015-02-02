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

package de.escidocng.service.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;

import de.escidocng.test.util.Fixtures;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.escidocng.model.Entity;
import de.escidocng.service.backend.BackendBlobstoreService;
import de.escidocng.service.impl.DefaultExportService;

public class DefaultExportServiceTest {

    private DefaultExportService exportService;

    private BackendBlobstoreService mockBlobstoreService;

    @Before
    public void setup() {
        exportService = new DefaultExportService();
        mockBlobstoreService = createMock(BackendBlobstoreService.class);
        ReflectionTestUtils.setField(exportService, "mapper", new ObjectMapper());
        ReflectionTestUtils.setField(exportService, "directory", new File(System.getProperty("java.io.tmpdir")));
    }

    @Test
    public void testExport() throws Exception {
        Entity e = Fixtures.createEntity();

        replay(mockBlobstoreService);
        this.exportService.export(e);
        verify(mockBlobstoreService);

    }
}
