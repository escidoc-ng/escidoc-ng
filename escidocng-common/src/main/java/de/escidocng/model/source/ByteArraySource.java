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

package de.escidocng.model.source;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class is intended for passing the bytes of a {@link de.escidocng.model.Binary}'s
 * actual data. If for example a {@link de.escidocng.model.Binary} is ingested a ByteArraySource object is used to
 * hold the actual content as byte[]
 */
public class ByteArraySource implements Source {

    private final byte[] bytes;

    private final boolean internal;

    /**
     * Create a new empty ByteArraySource
     */
    private ByteArraySource() {
        this.bytes = null;
        this.internal = false;
    }

    /**
     * Create an ByteArraySource with a given byte[]
     * 
     * @param bytes the bytes of the file
     * @param internal whether the source is internal or external
     */
    public ByteArraySource(byte[] bytes, boolean internal) {
        this.bytes = bytes;
        this.internal = internal;
    }

    /**
     * Create an ByteArraySource with a given byte[]
     * 
     * @param bytes the bytes of the file
     */
    public ByteArraySource(byte[] bytes) {
        this.bytes = bytes;
        this.internal = false;
    }

    /**
     * get the bytes of the source
     * 
     * @return the bytes
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Check if the source is internal to the repository
     * 
     * @return false if the source is not internal, true is the source is inside the repository
     */
    public boolean isInternal() {
        return internal;
    }

    /**
     * Open and retrieve an InputStream from the source
     * 
     * @return An InputStream
     * @throws IOException
     */
    @JsonIgnore
    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(bytes);
    }

}
