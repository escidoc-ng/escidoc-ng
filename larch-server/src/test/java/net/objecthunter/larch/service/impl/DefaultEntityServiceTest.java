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

package net.objecthunter.larch.service.impl;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.service.EntityValidatorService;
import net.objecthunter.larch.service.ExportService;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;
import net.objecthunter.larch.service.backend.BackendEntityService;
import net.objecthunter.larch.service.backend.BackendVersionService;
import net.objecthunter.larch.test.util.Fixtures;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultEntityServiceTest {

    private DefaultEntityService entityService;

    private BackendEntityService mockEntitiesService;

    private BackendBlobstoreService mockBlobstoreService;

    private ExportService mockExportService;

    private EntityValidatorService mockEntityValidatorService;

    private BackendVersionService mockVersionService;

    @Before
    public void setup() {
        entityService = new DefaultEntityService();
        mockEntitiesService = createMock(BackendEntityService.class);
        mockBlobstoreService = createMock(BackendBlobstoreService.class);
        mockExportService = createMock(ExportService.class);
        mockVersionService = createMock(BackendVersionService.class);
        mockEntityValidatorService = createMock(DefaultEntityValidatorService.class);
        ReflectionTestUtils.setField(entityService, "mapper", new ObjectMapper());
        ReflectionTestUtils.setField(entityService, "backendEntityService", mockEntitiesService);
        ReflectionTestUtils.setField(entityService, "exportService", mockExportService);
        ReflectionTestUtils.setField(entityService, "backendBlobstoreService", mockBlobstoreService);
        ReflectionTestUtils.setField(entityService, "backendVersionService", mockVersionService);
        ReflectionTestUtils.setField(entityService, "defaultEntityValidatorService", mockEntityValidatorService);
    }

    //@Test
    public void testCreate() throws Exception {
        Entity e = Fixtures.createEntity();

        expect(mockEntitiesService.exists(e.getId())).andReturn(false);
        expect(mockEntitiesService.create(e)).andReturn(e.getId());

        replay(mockEntitiesService, mockExportService, mockBlobstoreService);
        this.entityService.create(e);
        verify(mockEntitiesService, mockExportService, mockBlobstoreService);
    }

    //@Test
    public void testUpdate() throws Exception {
        Entity e = Fixtures.createEntity();

        mockEntitiesService.update(e);
        expectLastCall();
        expect(mockEntitiesService.retrieve(e.getId())).andReturn(e);
        expect(mockEntitiesService.fetchChildren(e.getId())).andReturn(new ArrayList<String>()).times(1);

        replay(mockEntitiesService, mockExportService, mockBlobstoreService);
        this.entityService.update(e);
        verify(mockEntitiesService, mockExportService, mockBlobstoreService);
    }

    @Test
    public void testRetrieve() throws Exception {
        Entity e = Fixtures.createEntity();

        expect(mockEntitiesService.retrieve(e.getId())).andReturn(e);
        expect(mockEntitiesService.fetchChildren(e.getId())).andReturn(new ArrayList<String>()).times(1);

        replay(mockEntitiesService, mockExportService, mockBlobstoreService);
        this.entityService.retrieve(e.getId());
        verify(mockEntitiesService, mockExportService, mockBlobstoreService);
    }


    @Test
    public void testRetrieve1() throws Exception {
        Entity e = Fixtures.createEntity();

        expect(mockEntitiesService.retrieve(e.getId())).andReturn(e);
        expect(mockEntitiesService.fetchChildren(e.getId())).andReturn(new ArrayList<String>()).times(1);
        replay(mockEntitiesService, mockExportService, mockBlobstoreService);
        this.entityService.retrieve(e.getId());
        verify(mockEntitiesService, mockExportService, mockBlobstoreService);
    }

    @Test
    public void testCreateBinary() throws Exception {
        Entity e = Fixtures.createEntity();
        Binary b = Fixtures.createBinary();
        b.setName("BINARY_CREATE");

        expect(mockEntitiesService.retrieve(e.getId())).andReturn(e);
        expect(mockEntitiesService.fetchChildren(e.getId())).andReturn(new ArrayList<String>()).times(1);
        expect(mockBlobstoreService.create(anyObject(InputStream.class))).andReturn("/path/to/bin");
        mockEntitiesService.update(e);
        expectLastCall();

        replay(mockEntitiesService, mockExportService, mockBlobstoreService);
        this.entityService.createBinary(e.getId(), b.getName(), "application/octet-stream",
                new ByteArrayInputStream(
                        new byte[3]));
        verify(mockEntitiesService, mockExportService, mockBlobstoreService);
    }

    //@Test
    public void testPatch() throws Exception {
        Entity e = Fixtures.createEntity();

        expect(mockEntitiesService.retrieve(e.getId())).andReturn(e).times(2);
        expect(mockEntitiesService.fetchChildren(e.getId())).andReturn(new ArrayList<String>()).times(2);
        mockEntitiesService.update(e);
        expectLastCall();

        replay(mockEntitiesService, mockExportService, mockBlobstoreService);
        this.entityService.patch(e.getId(), new ObjectMapper()
                .readTree("{\"label\": \"label update\"}"));
        verify(mockEntitiesService, mockExportService, mockBlobstoreService);

    }

    @Test
    public void testCreateRelation() throws Exception {
        Entity e = Fixtures.createEntity();

        expect(mockEntitiesService.retrieve(e.getId())).andReturn(e);
        expect(mockEntitiesService.fetchChildren(e.getId())).andReturn(new ArrayList<String>()).times(1);
        mockEntitiesService.update(e);
        expectLastCall();

        replay(mockEntitiesService, mockExportService, mockBlobstoreService);
        this.entityService.createRelation(e.getId(), "<http://example.com/hasType>", "test");
        verify(mockEntitiesService, mockExportService, mockBlobstoreService);

    }
}
