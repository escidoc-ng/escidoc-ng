/* 
 * Copyright 2014 Michael Hoppe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package net.objecthunter.larch.service.backend.sftp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.state.FilesystemBlobstoreState;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * Implementation of a {@link net.objecthunter.larch.service.backend.BackendBlobstoreService} on a SFtp file system.
 * The service gets initialized with sftp-login + path parameters
 */
public class SftpBlobstoreService implements BackendBlobstoreService {

    private static final Logger log = LoggerFactory.getLogger(SftpBlobstoreService.class);

    @Autowired
    private Environment env;

    @Autowired
    private ObjectMapper mapper;

    private String directory;

    private String oldVersionDirectory;

    private Session session;

    @PostConstruct
    public void init() throws Exception {
        directory = env.getProperty("sftp.path");
        if (directory.startsWith("/")) {
            directory = directory.substring(1);
        }
        if (directory.endsWith("/")) {
            directory = directory.substring(0,directory.length() - 1);
        }
        oldVersionDirectory = env.getProperty("sftp.oldversion.path");
        if (oldVersionDirectory.startsWith("/")) {
            oldVersionDirectory = oldVersionDirectory.substring(1);
        }
        if (oldVersionDirectory.endsWith("/")) {
            oldVersionDirectory = oldVersionDirectory.substring(0,oldVersionDirectory.length() - 1);
        }

        openSession();
        checkAndCreateDirectory(directory);
        checkAndCreateDirectory(oldVersionDirectory);
    }

    @Override
    public String create(InputStream src) throws IOException {
        ChannelSftp channel = null;
        try {
            String foldername = RandomStringUtils.randomAlphabetic(2);
            String filename = null;
            checkAndCreateDirectory(directory + "/" + foldername);
            do {
                /* create a new random file name */
                filename = RandomStringUtils.randomAlphabetic(16);
            } while (fileExists(directory + "/" + foldername + "/" + filename));
            channel = getChannel();
            channel.put(src, directory + "/" + foldername + "/" + filename);
            return foldername + "/" + filename;
        } catch (SftpException e) {
            throw new IOException(e.getMessage());
        } finally {
            closeChannel(channel);
        }
    }

    @Override
    public InputStream retrieve(String path) throws IOException {
        ChannelSftp channel = null;
        try {
            channel = getChannel();
            return channel.get(directory + "/" + path);
        } catch (SftpException e) {
            throw new NotFoundException(e.getMessage());
        } finally {
            closeChannel(channel);
        }
    }

    @Override
    public void delete(String path) throws IOException {
        ChannelSftp channel = null;
        try {
            channel = getChannel();
            channel.rm(directory + "/" + path);
        } catch (SftpException e) {
            throw new NotFoundException(e.getMessage());
        } finally {
            closeChannel(channel);
        }
    }

    @Override
    public void update(String path, InputStream src) throws IOException {
        ChannelSftp channel = null;
        try {
            retrieve(path);
            String destination = directory + "/" + path;
            channel = getChannel();
            channel.put(src, destination);
        } catch (IOException e) {
            throw new NotFoundException(e.getMessage());
        } catch (SftpException e) {
            throw new IOException(e.getMessage());
        } finally {
            closeChannel(channel);
        }
    }

    @Override
    public FilesystemBlobstoreState status() throws IOException {
        FilesystemBlobstoreState state = new FilesystemBlobstoreState();
        //TODO: get status
//        state.setPath(this.directory.getAbsolutePath());
//        state.setTotalSpace(this.directory.getTotalSpace());
//        state.setFreeSpace(this.directory.getFreeSpace());
//        state.setUsableSpace(this.directory.getUsableSpace());
        return state;
    }

    @Override
    public String createOldVersionBlob(Entity oldVersion) throws IOException {
        ChannelSftp channel = null;
        try {
            String foldername = RandomStringUtils.randomAlphabetic(2);
            String filename = null;
            checkAndCreateDirectory(oldVersionDirectory + "/" + foldername);
            do {
                /* create a new random file name */
                filename = RandomStringUtils.randomAlphabetic(16);
            } while (fileExists(oldVersionDirectory + "/" + foldername + "/" + filename));
            channel = getChannel();
            channel.put(new ByteArrayInputStream(mapper.writeValueAsBytes(oldVersion)), oldVersionDirectory + "/" + foldername + "/" + filename);
            return foldername + "/" + filename;
        } catch (SftpException e) {
            throw new IOException(e.getMessage());
        } finally {
            closeChannel(channel);
        }
    }

    @Override
    public InputStream retrieveOldVersionBlob(String path) throws IOException {
        ChannelSftp channel = null;
        try {
            channel = getChannel();
            InputStream in = channel.get(oldVersionDirectory + "/" + path);
            return in;
        } catch (SftpException e) {
            throw new NotFoundException(e.getMessage());
        } finally {
            closeChannel(channel);
        }
    }

    private ChannelSftp getChannel() throws IOException {
        try {
            checkSession();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            return (ChannelSftp) channel;
        } catch (JSchException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    private void checkSession() throws IOException {
        if (session == null) {
            openSession();
        } else if (!session.isConnected()) {
            session = null;
            openSession();
        }
    }
    
    private void openSession() throws IOException {
        try {
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch ssh = new JSch();
            session =
                    ssh.getSession(env.getProperty("sftp.login"), env.getProperty("sftp.host"), Integer.parseInt(env
                            .getProperty(
                                    "sftp.port", "22")));
            session.setConfig(config);
            session.setPassword(env.getProperty("sftp.password"));
            session.connect();
        } catch (JSchException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    private void closeChannel(ChannelSftp channel) throws IOException {
        if (channel != null) {
            channel.disconnect();
            channel.exit();
        }
    }
    
    private void checkAndCreateDirectory(String path) throws IOException {
        ChannelSftp channel = null;
        try {
            channel = getChannel();
            String[] folders = path.split("/");
            for (String folder : folders) {
                if (folder.length() > 0) {
                    try {
                        channel.cd(folder);
                    }
                    catch (SftpException e) {
                        channel.mkdir(folder);
                        channel.cd(folder);
                    }
                }
            }
            closeChannel(channel);
            return;
        } catch (SftpException e) {
            throw new IOException(e.getMessage());
        }
    }

    private boolean fileExists(String path) throws IOException {
        ChannelSftp channel = null;
        try {
            channel = getChannel();
            channel.get(path);
            return true;
        } catch (SftpException e) {
            return false;
        } finally {
            closeChannel(channel);
        }
    }
}
