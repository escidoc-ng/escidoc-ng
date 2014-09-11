package net.objecthunter.larch.service.backend.sftp;

import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.state.BlobstoreState;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.SftpClient;
import org.apache.sshd.client.future.ConnectFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Component
public class SftpBlobstoreService implements BackendBlobstoreService {

    private SftpClient sftp;
    private SshClient ssh;
    private String username;
    private String passwd;
    private String host;
    private int port;
    private long timeout = 10000;

    @Autowired
    private Environment env;

    private ClientSession currentSession;

    @PostConstruct
    public void init() throws Exception {
        this.username = env.getRequiredProperty("sftp.user");
        this.passwd = env.getRequiredProperty("sftp.passwd");
        this.host = env.getRequiredProperty("sftp.host");
        this.port = Integer.parseInt(env.getRequiredProperty("sftp.port"));
    }

    @Override
    public String create(InputStream src) throws IOException {
        ensureConnected();
        SftpClient.Attributes attrs = sftp.stat("/");
        System.out.println(attrs.perms);
        return null;
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
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        sftp = currentSession.createSftpClient();
    }

    @Override
    public InputStream retrieve(String path) throws IOException {
        return null;
    }

    @Override
    public void delete(String path) throws IOException {

    }

    @Override
    public void update(String path, InputStream src) throws IOException {

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
