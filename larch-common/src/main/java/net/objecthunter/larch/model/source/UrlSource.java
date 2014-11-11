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
import java.net.URI;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class is intended for passing the location information of a {@link net.objecthunter.larch.model.Binary}'s
 * actual data. If for example a {@link net.objecthunter.larch.model.Binary} is ingested a UrlSource object is used to
 * reference the actual content on disk using a {@code file://}
 */
public class UrlSource implements Source {

    private final URI uri;

    private final boolean internal;

    /**
     * Create a new empty UrlSource
     */
    private UrlSource() {
        this.uri = null;
        this.internal = false;
    }

    /**
     * Create an UrlSource form a given URI
     * 
     * @param uri the URI of the source file
     * @param internal whether the URI is larch internal or external (e.g. from the web)
     */
    public UrlSource(URI uri, boolean internal) {
        this.uri = uri;
        this.internal = internal;
    }

    /**
     * Create an UrlSource form a given URI
     * 
     * @param uri the URI of the source file
     */
    public UrlSource(URI uri) {
        this.uri = uri;
        this.internal = false;
    }

    /**
     * Create an UrlSource form a given URI
     * 
     * @param url the URL of the source file
     */
    public UrlSource(String url) {
        this.internal = false;
        this.uri = URI.create(url);
    }

    /**
     * get the URI of the source
     * 
     * @return the URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Check if a URI is internal to the larch repository
     * 
     * @return false if the URI is not internal, true is the URI is inside the repository
     */
    public boolean isInternal() {
        return internal;
    }

    /**
     * Open an retrieve an Inputstream from the URL source
     * 
     * @return An InputStream
     * @throws IOException
     */
    @JsonIgnore
    @Override
    public InputStream getInputStream() throws IOException {
        return uri.toURL().openStream();
    }
}
