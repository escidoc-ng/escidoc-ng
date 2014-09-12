package net.objecthunter.larch.integration;

import net.objecthunter.larch.service.backend.sftp.SftpBlobstoreService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.io.InputStream;

import static junit.framework.TestCase.assertNotNull;

public class SftpIT extends AbstractSftpIT {
    @Autowired
    private SftpBlobstoreService sftpBlobstoreService;

    @Test
    public void testCreate() throws Exception {
        String host = env.getRequiredProperty("sftp.host");
        String port = env.getRequiredProperty("sftp.port");
        System.out.println(host + ":" + port);
        final InputStream src = this.getClass().getClassLoader().getResourceAsStream("fixtures/image_1.png");
        assertNotNull(src);
        String path = sftpBlobstoreService.create(src);
        assertNotNull(path);
    }
}
