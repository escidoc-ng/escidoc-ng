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

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for various file system operations
 */
public abstract class FileSystemUtil {

    private static final Logger log = LoggerFactory.getLogger(FileSystemUtil.class);

    /**
     * Check if a directory on the file system exists and create it if it does not
     * 
     * @param dir the directory to check and create
     * @throws IOException
     */
    public static void checkAndCreate(File dir) throws IOException {
        if (!dir.exists()) {
            log.info("Creating non existing data directory {}", dir.getAbsolutePath());
            if (!dir.mkdirs()) {
                throw new IOException(dir.getAbsolutePath() + " could not be created");
            }
        }
        if (!dir.isDirectory()) {
            throw new IOException(dir.getAbsolutePath() + " does exist, and is not a directory");
        }
    }

}
