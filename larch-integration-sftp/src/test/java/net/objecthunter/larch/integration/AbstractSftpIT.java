package net.objecthunter.larch.integration;

import net.objecthunter.larch.LarchServerConfiguration;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {LarchServerConfiguration.class, SftpServerConfiguration.class})
@IntegrationTest
@WebAppConfiguration
@ActiveProfiles("sftp")
public abstract class AbstractSftpIT {
    @Autowired
    protected Environment env;


}
