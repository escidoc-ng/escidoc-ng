package net.objecthunter.larch.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.state.BlobstoreState;
import net.objecthunter.larch.service.backend.sftp.SftpBlobstoreService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class SftpIT extends AbstractSftpIT {
    @Autowired
    private SftpBlobstoreService sftpBlobstoreService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void testCreate() throws Exception {
        final InputStream src = this.getClass().getClassLoader().getResourceAsStream("fixtures/image_1.png");
        assertNotNull(src);
        String path = sftpBlobstoreService.create(src);
        assertNotNull(path);
    }

    @Test
    public void testCreateAndRetrieve() throws Exception {
        final String data = "MyDataWithÄ";
        final InputStream src = new ByteArrayInputStream(data.getBytes());
        assertNotNull(src);
        String path = sftpBlobstoreService.create(src);
        assertNotNull(path);
        final InputStream copy = sftpBlobstoreService.retrieve(path);
        assertEquals(data, IOUtils.toString(copy));
    }

    @Test
    public void testCreateUpdateAndRetrieve() throws Exception {
        final String data1 = "MyData";
        final String data2 = "MyChangedData";
        final InputStream src = new ByteArrayInputStream(data1.getBytes());
        assertNotNull(src);
        String path = sftpBlobstoreService.create(src);
        assertNotNull(path);
        sftpBlobstoreService.update(path, new ByteArrayInputStream((data2.getBytes())));
        final InputStream copy = sftpBlobstoreService.retrieve(path);
        assertEquals(data2, IOUtils.toString(copy));
    }


    @Test(expected = IOException.class)
    public void testCreateDeleteAndRetrieve() throws Exception {
        final String data1 = "MyData";
        final InputStream src = new ByteArrayInputStream(data1.getBytes());
        assertNotNull(src);
        String path = sftpBlobstoreService.create(src);
        assertNotNull(path);
        sftpBlobstoreService.delete(path);
        final InputStream copy = sftpBlobstoreService.retrieve(path);
    }

    @Test
    public void testCreateOldVersionBlob() throws Exception {
        Entity e = new Entity();
        e.setId(UUID.randomUUID().toString());
        e.setLabel(RandomStringUtils.randomAlphabetic(64));
        e.setState(Entity.EntityState.PENDING);
        e.setVersion(1);
        final String path = sftpBlobstoreService.createOldVersionBlob(e);
        assertNotNull(path);
    }

    @Test
    public void testCreateAndRetrieveOldVersionBlob() throws Exception {
        Entity e = new Entity();
        e.setId(UUID.randomUUID().toString());
        e.setLabel(RandomStringUtils.randomAlphabetic(64));
        e.setState(Entity.EntityState.PENDING);
        e.setVersion(1);
        final String path = sftpBlobstoreService.createOldVersionBlob(e);
        assertNotNull(path);
        Entity fetched = mapper.readValue(sftpBlobstoreService.retrieveOldVersionBlob(path), Entity.class);
        assertEquals(e.getId(), fetched.getId());
        assertEquals(e.getLabel(), fetched.getLabel());
        assertEquals(e.getState(), fetched.getState());
        assertEquals(e.getVersion(), fetched.getVersion());
    }

    @Test
    public void testRetrieveState() throws Exception {
        String xml = mapper.writeValueAsString(sftpBlobstoreService.status());
    }
}
