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


package de.escidocng.integration.fs.entity;

import static de.escidocng.test.util.Fixtures.createRandomDCMetadata;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import de.escidocng.integration.fs.AbstractFSEscidocngIT;
import de.escidocng.test.util.Fixtures;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.escidocng.model.Binary;
import de.escidocng.model.Entity;
import de.escidocng.model.Metadata;
import de.escidocng.model.ContentModel.FixedContentModel;
import de.escidocng.model.Entity.EntityState;
import de.escidocng.model.source.ByteArraySource;

public class MetadataControllerIT extends AbstractFSEscidocngIT {

    private static final Logger log = LoggerFactory.getLogger(MetadataControllerIT.class);

    /**
     * Test creating Entity with Metadata indexed inline.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateMetadataIndexInline() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, true);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertTrue(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertTrue(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Test creating Entity with Metadata not indexed inline.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateMetadataNotIndexInline() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, false);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertFalse(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Test updating non-indexed-inline Entity with Metadata indexed inline.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateMetadataIndexInline() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, false);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            m.setIndexInline(true);
        }
        entity = updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertTrue(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertFalse(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Test updating non-indexed-inline Entity with different Metadata indexed inline.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateMetadataIndexInline1() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, false);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            m.setFilename("dc1.xml");
            m.setSource(new ByteArraySource(IOUtils.toByteArray(Fixtures.class.getClassLoader().getResource("fixtures/dc1.xml").openStream())));
            m.setIndexInline(true);
        }
        entity = updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertTrue(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertFalse(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Test updating non-indexed-inline Entity with Binary Metadata indexed inline.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateMetadataIndexInline2() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, false);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                m.setIndexInline(true);
            }
        }
        entity = updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertTrue(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Test updating non-indexed-inline Entity with different Binary Metadata indexed inline.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateMetadataIndexInline3() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, false);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                m.setIndexInline(true);
            }
        }
        entity = updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertTrue(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Test updating indexed-inline Entity with Metadata not indexed inline.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateMetadataIndexInline4() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, true);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            m.setIndexInline(false);
        }
        entity = updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertTrue(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Test updating indexed-inline Entity with different Metadata not indexed inline.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateMetadataIndexInline5() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, true);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            m.setFilename("dc1.xml");
            m.setSource(new ByteArraySource(IOUtils.toByteArray(Fixtures.class.getClassLoader().getResource("fixtures/dc1.xml").openStream())));
            m.setIndexInline(false);
        }
        entity = updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertTrue(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Test updating indexed-inline Entity with Binary Metadata not indexed inline.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateMetadataIndexInline6() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, true);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                m.setIndexInline(false);
            }
        }
        entity = updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertTrue(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertFalse(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Test updating indexed-inline Entity with different Binary Metadata not indexed inline.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateMetadataIndexInline7() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, true);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                m.setFilename("dc1.xml");
                m.setSource(new ByteArraySource(IOUtils.toByteArray(Fixtures.class.getClassLoader().getResource("fixtures/dc1.xml").openStream())));
                m.setIndexInline(false);
            }
        }
        entity = updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertTrue(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertFalse(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Test adding inline-indexed Metadata to an entity with non-inline-indexed Metadata as Multipart-Request.
     * 
     * @throws Exception
     */
    @Test
    public void testAddMetadataIndexInline() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, false);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        entity = addMetadataMultipart(entity, "indexInlineTest", IGNORE, true, 201);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        boolean mdFound = false;
        for (Metadata m : entity.getMetadata()) {
            if (m.getName().equals("indexInlineTest")) {
                mdFound = true;
                assertTrue(m.isIndexInline());
            } else {
                assertFalse(m.isIndexInline());
            }
        }
        assertTrue(mdFound);
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertFalse(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Test adding inline-indexed Metadata to an entity with non-inline-indexed Metadata as Stream-Request.
     * 
     * @throws Exception
     */
    @Test
    public void testAddMetadataIndexInline1() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, false);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        entity = addMetadataStream(entity, "indexInlineTest", IGNORE, true, 201);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        boolean mdFound = false;
        for (Metadata m : entity.getMetadata()) {
            if (m.getName().equals("indexInlineTest")) {
                mdFound = true;
                assertTrue(m.isIndexInline());
            } else {
                assertFalse(m.isIndexInline());
            }
        }
        assertTrue(mdFound);
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertFalse(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Test adding inline-indexed Binary Metadata to an entity with non-inline-indexed Metadata as Multipart-Request.
     * 
     * @throws Exception
     */
    @Test
    public void testAddMetadataIndexInline2() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, false);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Binary b : entity.getBinaries()) {
            entity = addBinaryMetadataMultipart(entity, b.getName(), "indexInlineTest", IGNORE, true, 201);
        }
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            boolean mdFound = false;
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                if (m.getName().equals("indexInlineTest")) {
                    mdFound = true;
                    assertTrue(m.isIndexInline());
                } else {
                    assertFalse(m.isIndexInline());
                }
            }
            assertTrue(mdFound);
        }
        checkMetadata(entity);
    }

    /**
     * Test adding inline-indexed Binary Metadata to an entity with non-inline-indexed Metadata as Stream-Request.
     * 
     * @throws Exception
     */
    @Test
    public void testAddMetadataIndexInline3() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, false);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Binary b : entity.getBinaries()) {
            entity = addBinaryMetadataStream(entity, b.getName(), "indexInlineTest", IGNORE, true, 201);
        }
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            boolean mdFound = false;
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                if (m.getName().equals("indexInlineTest")) {
                    mdFound = true;
                    assertTrue(m.isIndexInline());
                } else {
                    assertFalse(m.isIndexInline());
                }
            }
            assertTrue(mdFound);
        }
        checkMetadata(entity);
    }

    /**
     * Test adding non-inline-indexed Metadata to an entity with non-inline-indexed Metadata as Multipart-Request.
     * 
     * @throws Exception
     */
    @Test
    public void testAddMetadataNotIndexInline() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, false);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        entity = addMetadataMultipart(entity, "indexInlineTest", IGNORE, false, 201);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertFalse(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Test adding non-inline-indexed Metadata to an entity with non-inline-indexed Metadata as Stream-Request.
     * 
     * @throws Exception
     */
    @Test
    public void testAddMetadataNotIndexInline1() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, false);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        entity = addMetadataStream(entity, "indexInlineTest", IGNORE, false, 201);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertFalse(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Test adding non-inline-indexed Binary Metadata to an entity with non-inline-indexed Metadata as Multipart-Request.
     * 
     * @throws Exception
     */
    @Test
    public void testAddMetadataNotIndexInline2() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, false);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Binary b : entity.getBinaries()) {
            entity = addBinaryMetadataMultipart(entity, b.getName(), "indexInlineTest", IGNORE, false, 201);
        }
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertFalse(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Test adding non-inline-indexed Binary Metadata to an entity with non-inline-indexed Metadata as Stream-Request.
     * 
     * @throws Exception
     */
    @Test
    public void testAddMetadataNotIndexInline3() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, false);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Binary b : entity.getBinaries()) {
            entity = addBinaryMetadataStream(entity, b.getName(), "indexInlineTest", IGNORE, false, 201);
        }
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertFalse(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Test deleting inline-indexed Metadata.
     * 
     * @throws Exception
     */
    @Test
    public void testDeleteMetadataIndexInline() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, true);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata md : entity.getMetadata()) {
            entity = removeMetadata(entity, md.getName(), 200);
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                entity = removeBinaryMetadata(entity, b.getName(), m.getName(), 200);
            }
        }
        assertEquals(new ArrayList<Metadata>(), entity.getMetadata());
        for (Binary b : entity.getBinaries()) {
            assertEquals(new ArrayList<Metadata>(), b.getMetadata());
        }
        checkMetadata(entity);
    }

    /**
     * Test deleting non-inline-indexed Metadata.
     * 
     * @throws Exception
     */
    @Test
    public void testDeleteMetadataNonIndexInline() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, false);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata md : entity.getMetadata()) {
            entity = removeMetadata(entity, md.getName(), 200);
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                entity = removeBinaryMetadata(entity, b.getName(), m.getName(), 200);
            }
        }
        assertEquals(new ArrayList<Metadata>(), entity.getMetadata());
        for (Binary b : entity.getBinaries()) {
            assertEquals(new ArrayList<Metadata>(), b.getMetadata());
        }
        checkMetadata(entity);
    }

    /**
     * Check oldVersion Metadata of index-inline metadata
     * 
     * @throws Exception
     */
    @Test
    public void testCheckOldVersionMetadataInlineIndex() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, true);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        entity.setLabel("other");
        updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata md : entity.getMetadata()) {
            md.setIndexInline(false);
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata md : b.getMetadata()) {
                md.setIndexInline(false);
            }
        }
        entity = updateEntity(entity, 200);
        assertEquals(3, entity.getVersion());
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertFalse(m.isIndexInline());
            }
        }
        entity = retrieveVersion(entity.getId(), 1, 200);
        assertEquals(1, entity.getVersion());
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertTrue(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertTrue(m.isIndexInline());
            }
        }
        checkMetadata(entity);
        entity = retrieveVersion(entity.getId(), 2, 200);
        assertEquals(2, entity.getVersion());
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertTrue(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertTrue(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Check oldVersion Metadata of not-index-inline metadata
     * 
     * @throws Exception
     */
    @Test
    public void testCheckOldVersionMetadataNotInlineIndex() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, false);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        entity.setLabel("other");
        updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata md : entity.getMetadata()) {
            md.setIndexInline(true);
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata md : b.getMetadata()) {
                md.setIndexInline(true);
            }
        }
        entity = updateEntity(entity, 200);
        assertEquals(3, entity.getVersion());
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertTrue(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertTrue(m.isIndexInline());
            }
        }
        entity = retrieveVersion(entity.getId(), 1, 200);
        assertEquals(1, entity.getVersion());
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertFalse(m.isIndexInline());
            }
        }
        checkMetadata(entity);
        entity = retrieveVersion(entity.getId(), 2, 200);
        assertEquals(2, entity.getVersion());
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata()) {
                assertFalse(m.isIndexInline());
            }
        }
        checkMetadata(entity);
    }

    /**
     * Check default indexInline of Controller-Method.
     * 
     * @throws Exception
     */
    @Test
    public void testCheckDefault() throws Exception {
        Entity entity = createEntity(EntityState.PENDING, FixedContentModel.LEVEL1.getName(), null, true);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        // add metadata
        Metadata metadata = createRandomDCMetadata(false);
        metadata.setName("indexInlineTest");
        ByteArrayInputStream bais = new ByteArrayInputStream(((ByteArraySource) metadata.getSource()).getBytes());
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(
                                entityUrl + entity.getId() + "/metadata").body(
                                MultipartEntityBuilder.create()
                                        .addTextBody("name", metadata.getName())
                                        .addTextBody("type", metadata.getType())
                                        .addBinaryBody(
                                                "data",
                                                bais, ContentType.APPLICATION_XML, metadata.getFilename())
                                        .build()));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());
        
        // add binary metadata
        for (Binary b : entity.getBinaries()) {
            metadata = createRandomDCMetadata(false);
            metadata.setName("indexInlineTest");
            bais = new ByteArrayInputStream(((ByteArraySource) metadata.getSource()).getBytes());
            resp =
                    this.executeAsAdmin(
                            Request.Post(
                                    entityUrl + entity.getId() + "/binary/" +
                                            b.getName() + "/metadata").body(
                                    MultipartEntityBuilder.create()
                                            .addTextBody("name", metadata.getName())
                                            .addTextBody("type", metadata.getType())
                                            .addBinaryBody(
                                                    "data",
                                                    bais, ContentType.APPLICATION_XML, metadata.getFilename())
                                            .build()));
            test = EntityUtils.toString(resp.getEntity());
            assertEquals(201, resp.getStatusLine().getStatusCode());
        }
        entity = retrieveEntity(entity.getId(), 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        boolean mdFound = false;
        for (Metadata m : entity.getMetadata()) {
            if (m.getName().equals("indexInlineTest")) {
                mdFound = true;
                assertFalse(m.isIndexInline());
            } else {
                assertTrue(m.isIndexInline());
            }
        }
        assertTrue(mdFound);
        for (Binary b : entity.getBinaries()) {
            assertNotNull(b.getMetadata());
            mdFound = false;
            for (Metadata m : b.getMetadata()) {
                if (m.getName().equals("indexInlineTest")) {
                    mdFound = true;
                    assertFalse(m.isIndexInline());
                } else {
                    assertTrue(m.isIndexInline());
                }
            }
            assertTrue(mdFound);
        }
        checkMetadata(entity);
    }
}
