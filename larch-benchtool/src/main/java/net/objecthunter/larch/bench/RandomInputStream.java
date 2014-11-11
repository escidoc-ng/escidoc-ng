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

package net.objecthunter.larch.bench;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomInputStream extends InputStream {

    private static final Logger log = LoggerFactory.getLogger(RandomInputStream.class);

    private final long size;

    private long bytesRead;

    private int slicePos;

    public RandomInputStream(long size) {
        super();
        this.size = size;
    }

    @Override
    public int read() throws IOException {
        if (bytesRead == size) {
            System.out.println("Stream finished");
            return -1;
        }
        if (slicePos >= BenchTool.SLICE.length) {
            slicePos = BenchTool.RNG.nextInt(BenchTool.SLICE.length / 2);
        }
        bytesRead++;
        return BenchTool.SLICE[slicePos++];
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        int i;
        for (i = off; i - off < len; i++) {
            int data = read();
            if (data == -1) {
                return i - off - 1;
            } else {
                b[i - off] = (byte) data;
            }
        }
        return i - off;
    }
}
