/* 
 * Copyright 2014 Frank Asseg
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

package net.objecthunter.larch.service.backend.weedfs;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import net.objecthunter.larch.helpers.InputStreamLoggerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 * Helper class for starting and monitoring a WeedFs volume server process
 */
public class WeedFsVolume {

    private static final Logger log = LoggerFactory.getLogger(WeedFsVolume.class);

    @Autowired
    Environment env;

    private Process volumeProcess;

    private InputStreamLoggerTask loggerTask;

    @PostConstruct
    public void runVolume() {
        /* check if the master dir exists and create if neccessary */
        final File dir = new File(env.getProperty("weedfs.volume.dir"));
        if (!dir.exists()) {
            log.info("creating WeedFS volume directory at " + dir.getAbsolutePath());
            if (!dir.mkdir()) {
                throw new IllegalArgumentException(
                        "Unable to create volume directory. Please check the configuration");
            }
        }
        if (!dir.canRead() || !dir.canWrite()) {
            log.error("Unable to create volume directory. The application was not initialiazed correctly");
            throw new IllegalArgumentException("Unable to use volume directory. Please check the configuration");
        }
        try {
            /* start weedfs volume server */
            String[] args = new String[] {
                env.getProperty("weedfs.binary"),
                "volume",
                "-ip=" + env.getProperty("weedfs.volume.public"),
                "-publicIp=" + env.getProperty("weedfs.volume.public"),
                "-dir=" + env.getProperty("weedfs.volume.dir"),
                "-mserver=" + env.getProperty("weedfs.master.host") + ":" + env.getProperty("weedfs.master.port"),
                "-port=" + env.getProperty("weedfs.volume.port")
            };
            log.info("Starting weedfs volume with command '" + String.join(" ", args) + "'");
            volumeProcess = new ProcessBuilder(args)
                    .redirectErrorStream(true)
                    .redirectInput(ProcessBuilder.Redirect.PIPE)
                    .start();

            final Executor executor = Executors.newSingleThreadExecutor();
            if (!volumeProcess.isAlive()) {
                throw new IOException("WeedFS volume could not be started! Exitcode " + volumeProcess.exitValue());
            } else {
                log.info("WeedFs volume is running");
                executor.execute(new InputStreamLoggerTask(volumeProcess.getInputStream()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAvailable() {
        final File binary = new File(env.getProperty("weedfs.binary"));
        return binary.exists() && binary.canExecute();
    }

    public boolean isAlive() {
        return (volumeProcess != null) && volumeProcess.isAlive();
    }

    @PreDestroy
    public void shutdown() {
        log.info("shutting down WeedFS volume");
        if (this.volumeProcess != null && this.volumeProcess.isAlive()) {
            this.volumeProcess.destroy();
        }
    }
}
