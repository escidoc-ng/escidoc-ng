package net.objecthunter.larch.service.backend.sftp;

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
