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


package net.objecthunter.larch.integration.fs.entity;

import static net.objecthunter.larch.test.util.Fixtures.createRandomDCMetadata;
import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;

import net.objecthunter.larch.integration.fs.AbstractLarchIT;
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.source.UrlSource;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.test.util.Fixtures;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataControllerIT extends AbstractLarchIT {

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
        for (Metadata m : entity.getMetadata().values()) {
            assertTrue(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Metadata m : entity.getMetadata().values()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Metadata m : entity.getMetadata().values()) {
            m.setIndexInline(true);
        }
        entity = updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertTrue(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Metadata m : entity.getMetadata().values()) {
            m.setFilename("dc1.xml");
            m.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/dc1.xml").toURI()));
            m.setIndexInline(true);
        }
        entity = updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertTrue(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
                m.setIndexInline(true);
            }
        }
        entity = updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
                m.setIndexInline(true);
            }
        }
        entity = updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Metadata m : entity.getMetadata().values()) {
            m.setIndexInline(false);
        }
        entity = updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Metadata m : entity.getMetadata().values()) {
            m.setFilename("dc1.xml");
            m.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/dc1.xml").toURI()));
            m.setIndexInline(false);
        }
        entity = updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
                m.setIndexInline(false);
            }
        }
        entity = updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertTrue(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
                m.setFilename("dc1.xml");
                m.setSource(new UrlSource(Fixtures.class.getClassLoader().getResource("fixtures/dc1.xml").toURI()));
                m.setIndexInline(false);
            }
        }
        entity = updateEntity(entity, 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertTrue(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Metadata m : entity.getMetadata().values()) {
            if (m.getName().equals("indexInlineTest")) {
                mdFound = true;
                assertTrue(m.isIndexInline());
            } else {
                assertFalse(m.isIndexInline());
            }
        }
        assertTrue(mdFound);
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Metadata m : entity.getMetadata().values()) {
            if (m.getName().equals("indexInlineTest")) {
                mdFound = true;
                assertTrue(m.isIndexInline());
            } else {
                assertFalse(m.isIndexInline());
            }
        }
        assertTrue(mdFound);
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Binary b : entity.getBinaries().values()) {
            entity = addBinaryMetadataMultipart(entity, b.getName(), "indexInlineTest", IGNORE, true, 201);
        }
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            boolean mdFound = false;
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Binary b : entity.getBinaries().values()) {
            entity = addBinaryMetadataStream(entity, b.getName(), "indexInlineTest", IGNORE, true, 201);
        }
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            boolean mdFound = false;
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Metadata m : entity.getMetadata().values()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Metadata m : entity.getMetadata().values()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Binary b : entity.getBinaries().values()) {
            entity = addBinaryMetadataMultipart(entity, b.getName(), "indexInlineTest", IGNORE, false, 201);
        }
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Binary b : entity.getBinaries().values()) {
            entity = addBinaryMetadataStream(entity, b.getName(), "indexInlineTest", IGNORE, false, 201);
        }
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Metadata md : entity.getMetadata().values()) {
            entity = removeMetadata(entity, md.getName(), 200);
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
                entity = removeBinaryMetadata(entity, b.getName(), m.getName(), 200);
            }
        }
        assertEquals(new HashMap<String, Metadata>(), entity.getMetadata());
        for (Binary b : entity.getBinaries().values()) {
            assertEquals(new HashMap<String, Metadata>(), b.getMetadata());
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
        for (Metadata md : entity.getMetadata().values()) {
            entity = removeMetadata(entity, md.getName(), 200);
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
                entity = removeBinaryMetadata(entity, b.getName(), m.getName(), 200);
            }
        }
        assertEquals(new HashMap<String, Metadata>(), entity.getMetadata());
        for (Binary b : entity.getBinaries().values()) {
            assertEquals(new HashMap<String, Metadata>(), b.getMetadata());
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
        for (Metadata md : entity.getMetadata().values()) {
            md.setIndexInline(false);
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata md : b.getMetadata().values()) {
                md.setIndexInline(false);
            }
        }
        entity = updateEntity(entity, 200);
        assertEquals(3, entity.getVersion());
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
                assertFalse(m.isIndexInline());
            }
        }
        entity = retrieveVersion(entity.getId(), 1, 200);
        assertEquals(1, entity.getVersion());
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertTrue(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
                assertTrue(m.isIndexInline());
            }
        }
        checkMetadata(entity);
        entity = retrieveVersion(entity.getId(), 2, 200);
        assertEquals(2, entity.getVersion());
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertTrue(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        for (Metadata md : entity.getMetadata().values()) {
            md.setIndexInline(true);
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata md : b.getMetadata().values()) {
                md.setIndexInline(true);
            }
        }
        entity = updateEntity(entity, 200);
        assertEquals(3, entity.getVersion());
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertTrue(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
                assertTrue(m.isIndexInline());
            }
        }
        entity = retrieveVersion(entity.getId(), 1, 200);
        assertEquals(1, entity.getVersion());
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
                assertFalse(m.isIndexInline());
            }
        }
        checkMetadata(entity);
        entity = retrieveVersion(entity.getId(), 2, 200);
        assertEquals(2, entity.getVersion());
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        for (Metadata m : entity.getMetadata().values()) {
            assertFalse(m.isIndexInline());
        }
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            for (Metadata m : b.getMetadata().values()) {
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
        File f = new File(((UrlSource) metadata.getSource()).getUri());
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(
                                entityUrl + entity.getId() + "/metadata").body(
                                MultipartEntityBuilder.create()
                                        .addTextBody("name", metadata.getName())
                                        .addTextBody("type", metadata.getType())
                                        .addBinaryBody(
                                                "data",
                                                f, ContentType.APPLICATION_XML, f.getName())
                                        .build()));
        String test = EntityUtils.toString(resp.getEntity());
        assertEquals(201, resp.getStatusLine().getStatusCode());
        
        // add binary metadata
        for (Binary b : entity.getBinaries().values()) {
            metadata = createRandomDCMetadata(false);
            metadata.setName("indexInlineTest");
            f = new File(((UrlSource) metadata.getSource()).getUri());
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
                                                    f, ContentType.APPLICATION_XML, f.getName())
                                            .build()));
            test = EntityUtils.toString(resp.getEntity());
            assertEquals(201, resp.getStatusLine().getStatusCode());
        }
        entity = retrieveEntity(entity.getId(), 200);
        assertNotNull(entity.getMetadata());
        assertNotNull(entity.getBinaries());
        boolean mdFound = false;
        for (Metadata m : entity.getMetadata().values()) {
            if (m.getName().equals("indexInlineTest")) {
                mdFound = true;
                assertFalse(m.isIndexInline());
            } else {
                assertTrue(m.isIndexInline());
            }
        }
        assertTrue(mdFound);
        for (Binary b : entity.getBinaries().values()) {
            assertNotNull(b.getMetadata());
            mdFound = false;
            for (Metadata m : b.getMetadata().values()) {
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
