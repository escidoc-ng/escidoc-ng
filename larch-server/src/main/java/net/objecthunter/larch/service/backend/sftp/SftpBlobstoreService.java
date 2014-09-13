package net.objecthunter.larch.service.backend.sftp;

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.state.BlobstoreState;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.SftpClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.UUID;

@Component
public class SftpBlobstoreService implements BackendBlobstoreService {

    private static final Logger log = LoggerFactory.getLogger(SftpBlobstoreService.class);

    private SftpClient sftp;
    private SshClient ssh;
    private String username;
    private String passwd;
    private String host;
    private int port;
    private long timeout = 10000;
    private String rootPath;

    @Autowired
    private Environment env;

    private ClientSession currentSession;

    @PostConstruct
    public void init() throws Exception {
        this.username = env.getRequiredProperty("sftp.user");
        this.passwd = env.getRequiredProperty("sftp.passwd");
        this.host = env.getRequiredProperty("sftp.host");
        this.port = Integer.parseInt(env.getRequiredProperty("sftp.port"));
        this.rootPath = env.getRequiredProperty("sftp.basepath");
    }

    private void ensureConnected() throws IOException {
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

        /* ensure that the root directory exists */
        try {
            SftpClient.Attributes attrs = sftp.stat("/");
        } catch (IOException e) {
            this.log.error("Unable to stat directory '/'. SFTP client says: ", e);
            throw e;
        }
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
    }

    private void ensureSubDirExists(String subdir) throws IOException {
        try {
            sftp.stat(rootPath + "/" + subdir);
        } catch (IOException e) {
            final String path = rootPath + "/" + subdir;
            this.log.warn("Creating sub directory " + path);
            sftp.mkdir(path);
        }
    }


    @Override
    public String create(InputStream src) throws IOException {

        if (src == null) {
            throw new IOException("Unable to write NULL inputstream");
        }

        try {
            ensureConnected();

            SftpClient.Attributes attrs = sftp.stat(rootPath);
            final String subdir = RandomStringUtils.randomAlphabetic(2);
            final String fileName = UUID.randomUUID().toString();
            final String path = rootPath + "/" + subdir + "/" + fileName;

            ensureSubDirExists(subdir);

            /* get a file handle */
            final SftpClient.Handle fileHandle = sftp.open(path, EnumSet.of(SftpClient.OpenMode.Create, SftpClient.OpenMode.Write));

            /* write the data in a loop via the handle */
            final byte buf[] = new byte[4096];
            int bytesRead;
            int bytesWritten = 0;
            while ((bytesRead = src.read(buf)) > 0) {
                sftp.write(fileHandle, bytesWritten, buf, 0, bytesRead);
                bytesWritten += bytesRead;
            }
            return subdir + "/" + fileName;
        } finally {
            IOUtils.closeQuietly(src);
        }
    }


    @Override
    public InputStream retrieve(String path) throws IOException {

        ensureConnected();

        if (path == null || path.isEmpty()) {
            throw new IOException("Path can not be null or empty");
        }

        SftpClient.Attributes attrs = sftp.stat(rootPath + "/" + path);
        SftpClient.Handle fileHandle = sftp.open(rootPath + "/" + path, EnumSet.of(SftpClient.OpenMode.Read));
        return new SftpInputStream(sftp, fileHandle);
    }

    @Override
    public void delete(String path) throws IOException {

    }

    @Override
    public void update(String path, InputStream src) throws IOException {
        if (src == null) {
            throw new IOException("Unable to write NULL inputstream");
        }

        try {
            ensureConnected();

            SftpClient.Attributes attrs = sftp.stat(rootPath);
            final String subdir = RandomStringUtils.randomAlphabetic(2);
            final String fileName = UUID.randomUUID().toString();

            path = rootPath + "/" + path;

            /* get a file handle */
            final SftpClient.Handle fileHandle = sftp.open(path, EnumSet.of(SftpClient.OpenMode.Write));

            /* write the data in a loop via the handle */
            final byte buf[] = new byte[4096];
            int bytesRead;
            int bytesWritten = 0;
            while ((bytesRead = src.read(buf)) > 0) {
                sftp.write(fileHandle, bytesWritten, buf, 0, bytesRead);
                bytesWritten += bytesRead;
            }
        } finally {
            IOUtils.closeQuietly(src);
        }
    }

    @Override
    public BlobstoreState status() throws IOException {
        return null;
    }

    @Override
    public String createOldVersionBlob(Entity oldVersion) throws IOException {
        return null;
    }

    @Override
    public InputStream retrieveOldVersionBlob(String path) throws IOException {
        return null;
    }
}
