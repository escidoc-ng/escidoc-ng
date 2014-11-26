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

package net.objecthunter.larch.util;

import java.io.IOException;

import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.SftpClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SftpUtil {

    private static final Logger log = LoggerFactory.getLogger(SftpUtil.class);

    private static  long timeout = 10000;

    public static SftpClient getSftpClient(String username, String password, String host, int port, String rootPath) throws Exception {
        SshClient ssh = SshClient.setUpDefaultClient();
        ssh.start();
        ClientSession currentSession = null;
        try {
            ConnectFuture conn = ssh.connect(username, host, port);
            if (!conn.await(timeout)) {
                throw new IOException("Unable to connect to " + host + ":" + port);
            }
            currentSession = conn.getSession();
            currentSession.addPasswordIdentity(password);
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
        SftpClient sftp = currentSession.createSftpClient();

        try {
            sftp.stat("/");
        } catch (IOException e) {
            log.error("Unable to stat root directory. SFTP client says: ", e);
            throw e;
        }

        /* ensure that the root directory exists */
        try {
            sftp.stat(rootPath);
        } catch (IOException e) {
            log.warn("Trying to create non existant sftp directory " + rootPath);
            try {
                sftp.mkdir(rootPath);
            } catch (IOException inner) {
                log.error("Unable to create root directory", e);
                throw e;
            }
        }
        return sftp;
    }

}
