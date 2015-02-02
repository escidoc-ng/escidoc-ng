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

package net.objecthunter.larch.model.source;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class is intended for passing the InputStream of a {@link net.objecthunter.larch.model.Binary}'s
 * actual data. 
 */
public class InputStreamSource implements Source {

    private final InputStream inputStream;

    private final boolean internal;

    /**
     * Create a new empty InputStreamSource
     */
    private InputStreamSource() {
        this.inputStream = null;
        this.internal = false;
    }

    /**
     * Create an InputStreamSource with a given InputStream
     * 
     * @param inputStream the inputStream
     * @param internal whether the source is internal or external
     */
    public InputStreamSource(InputStream inputStream, boolean internal) {
        this.inputStream = inputStream;
        this.internal = internal;
    }

    /**
     * Create an InputStreamSource with a given InputStream
     * 
     * @param bytes the bytes of the file
     */
    public InputStreamSource(InputStream inputStream) {
        this.inputStream = inputStream;
        this.internal = false;
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
        return inputStream;
    }

}
