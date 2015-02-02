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

package de.escidocng.model.state;

/**
 * A DTO for wrapping the state of a simple POSIX File System Blobstore implementation
 */
public class FilesystemBlobstoreState extends BlobstoreState {

    private String path;

    private long totalSpace;

    private long freeSpace;

    private long usableSpace;

    public FilesystemBlobstoreState() {
        super("POSIX File system blob store");
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public long getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(long freeSpace) {
        this.freeSpace = freeSpace;
    }

    public long getUsableSpace() {
        return usableSpace;
    }

    public void setUsableSpace(long usableSpace) {
        this.usableSpace = usableSpace;
    }
}
