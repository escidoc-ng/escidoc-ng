package net.objecthunter.larch.integration;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthNone;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Component
public class SftpServer {

    @Autowired
    private Environment env;

    private SshServer ssh;
    private int port;

    @PostConstruct
    public void start() {
        port = Integer.parseInt(this.env.getRequiredProperty("sftp.port"));
        ssh = SshServer.setUpDefaultServer();
        ssh.setPort(port);
        ssh.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));

        List<NamedFactory<UserAuth>> userAuthFactories = new ArrayList<NamedFactory<UserAuth>>();
        userAuthFactories.add(new UserAuthNone.Factory());
        ssh.setUserAuthFactories(userAuthFactories);

        ssh.setCommandFactory(new ScpCommandFactory());

        List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
        namedFactoryList.add(new SftpSubsystem.Factory());
        ssh.setSubsystemFactories(namedFactoryList);

        try {
            ssh.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        ssh.stop(true);
    }
}
