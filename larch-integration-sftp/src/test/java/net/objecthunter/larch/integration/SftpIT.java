package net.objecthunter.larch.integration;

import net.objecthunter.larch.service.backend.sftp.SftpBlobstoreService;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class SftpIT extends AbstractSftpIT {
    @Autowired
    private SftpBlobstoreService sftpBlobstoreService;

    @Test
    public void testCreate() throws Exception {
        final InputStream src = this.getClass().getClassLoader().getResourceAsStream("fixtures/image_1.png");
        assertNotNull(src);
        String path = sftpBlobstoreService.create(src);
        assertNotNull(path);
    }

    @Test
    public void testCreateAndRetrieve() throws Exception {
        final InputStream src = new ByteArrayInputStream("MyDataWithÄ".getBytes());
        assertNotNull(src);
        String path = sftpBlobstoreService.create(src);
        assertNotNull(path);
        final InputStream copy = sftpBlobstoreService.retrieve(path);
        assertEquals(IOUtils.toString(src), IOUtils.toString(copy));
    }
}
