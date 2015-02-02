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

package net.objecthunter.larch.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class allows to run a container managed background task which captures the output of an arbitrary
 * {@link java.io.InputStream} and prints it out using the slf4j framework used in Java. This is for example used to
 * capture the output of a WeedFs process run in the background
 */
public class InputStreamLoggerTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger("weedfs");

    private final BufferedReader reader;

    public InputStreamLoggerTask(InputStream src) {
        this.reader = new BufferedReader(new InputStreamReader(src));
    }

    @Override
    public void run() {
        String line = null;
        while (true) {
            try {
                if (this.reader.ready() && (line = this.reader.readLine()) != null) {
                    log.info(line);
                } else {
                    Thread.sleep(200);
                }
            } catch (IOException | InterruptedException e) {
                log.error("PANIC! Unable to read from WeedFs output", e);
            }
        }
    }

}
