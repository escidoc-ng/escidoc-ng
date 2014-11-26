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

package net.objecthunter.larch.service.backend.sftp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.SftpClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.io.IOException;

public abstract class AbstractSftpService {

    private static final Logger log = LoggerFactory.getLogger(AbstractSftpService.class);

    protected SftpClient sftp;

    protected SshClient ssh;

    protected String username;

    protected String passwd;

    protected String host;

    protected int port;

    protected long timeout = 10000;

    protected String rootPath;

    protected String oldVersionRootPath;

    protected boolean connected;

    protected ClientSession currentSession;

    @Autowired
    protected Environment env;

    @Autowired
    protected ObjectMapper mapper;

    @PostConstruct
    public void init() throws Exception {
        this.username = env.getRequiredProperty("sftp.user");
        this.passwd = env.getRequiredProperty("sftp.passwd");
        this.host = env.getRequiredProperty("sftp.host");
        this.port = Integer.parseInt(env.getRequiredProperty("sftp.port"));
        this.rootPath = env.getRequiredProperty("sftp.basepath");
        this.oldVersionRootPath = env.getRequiredProperty("sftp.oldversion.basepath");
        log.info("username: " + this.username);
        log.info("host: " + this.host);
        log.info("port: " + this.port);
        log.info("rootPath: " + this.rootPath);
        log.info("oldVersionRootPath: " + this.oldVersionRootPath);
    }

    protected void ensureConnected() throws IOException {
        if (connected) {
            return;
        }
        ssh = SshClient.setUpDefaultClient();
        ssh.start();
        try {
            ConnectFuture conn = ssh.connect(username, host, port);
            if (!conn.await(timeout)) {
                throw new IOException("Unable to connect to " + host + ":" + port);
            }
            currentSession = conn.getSession();
            currentSession.addPasswordIdentity(this.passwd);
            AuthFuture authFuture = currentSession.auth();
            if (!authFuture.await(timeout)) {
                throw new IOException("Timeout during authentication");
            }
            if (!authFuture.isSuccess()) {
                throw new IOException("Authentication at SFTP Backend failed");
            }
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        sftp = currentSession.createSftpClient();

        try {
            SftpClient.Attributes attrs = sftp.stat("/");
        } catch (IOException e) {
            this.log.error("Unable to stat root directory. SFTP client says: ", e);
            throw e;
        }

        /* ensure that the root directory exists */
        try {
            SftpClient.Attributes attrs = sftp.stat(rootPath);
        } catch (IOException e) {
            this.log.warn("Trying to create non existant sftp directory " + rootPath);
            try {
                sftp.mkdir(rootPath);
            } catch (IOException inner) {
                this.log.error("Unable to create root directory", e);
                throw e;
            }
        }

        /* ensure that the old version directory exists */
        try {
            SftpClient.Attributes attrs = sftp.stat(oldVersionRootPath);
        } catch (IOException e) {
            this.log.warn("Trying to create non existent sftp directory " + oldVersionRootPath);
            try {
                sftp.mkdir(oldVersionRootPath);
            } catch (IOException inner) {
                this.log.error("Unable to create old version directory", e);
                throw e;
            }
        }
        connected = true;
    }

    protected void ensureSubDirExists(String parent, String subdir) throws IOException {
        final String path = subdir;
        try {
            final SftpClient.Handle dirHandle = sftp.openDir(path);
            log.info(" " + dirHandle.id);
        } catch (IOException e) {
            this.log.warn("Creating sub directory " + path);
            sftp.mkdir(path);
        }
    }

}
