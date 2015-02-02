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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link java.io.InputStream} implementaion which keeps track of the number of bytes read from it and is able to
 * calculate a Checksum while streaming
 */
public class SizeCalculatingDigestInputStream extends FilterInputStream {

    private static final Logger log = LoggerFactory.getLogger(SizeCalculatingDigestInputStream.class);

    protected MessageDigest digest;

    private boolean on = true;

    private long calculatedSize = 0;

    public SizeCalculatingDigestInputStream(InputStream stream, MessageDigest digest) {
        super(stream);
        setMessageDigest(digest);
    }

    public MessageDigest getMessageDigest() {
        return digest;
    }

    public void setMessageDigest(MessageDigest digest) {
        this.digest = digest;
    }

    public int read() throws IOException {
        int ch = in.read();
        if (ch > 0) {
            calculatedSize++;
            if (on) {
                digest.update((byte) ch);
            }
        }
        return ch;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int result = in.read(b, off, len);
        if (result != -1) {
            calculatedSize += result;
            if (on) {
                digest.update(b, off, result);
            }
        }
        return result;
    }

    public long getCalculatedSize() {
        return calculatedSize;
    }

    public void on(boolean on) {
        this.on = on;
    }

    public String toString() {
        return "[Digest Input Stream] " + digest.toString();
    }
}
