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
package de.escidocng.service.backend.sftp;

import org.apache.sshd.client.SftpClient;

import java.io.IOException;
import java.io.InputStream;

public class SftpInputStream extends InputStream {
    private final SftpClient.Handle fileHandle;
    private final byte[] buffer = new byte[4096];
    private int available = 0;
    private final SftpClient sftp;
    private int bytesRead = 0;
    private int offset;

    public SftpInputStream(SftpClient sftp, SftpClient.Handle fileHandle) {
        this.fileHandle = fileHandle;
        this.sftp = sftp;
    }

    @Override
    public int read() throws IOException {
        if (available == 0) {
            int num;
            if ((num = sftp.read(fileHandle, bytesRead, buffer, 0, 4096)) < 0) {
                return -1;
            }
            bytesRead += num;
            available = num;
            offset = 0;
        }
        available--;
        return buffer[offset++];
    }
}
