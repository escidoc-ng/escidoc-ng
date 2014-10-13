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
 * WITHOUT WARRANTIES OR CONDITIONS OF ROLE_ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.objecthunter.larch.exceptions;

import java.io.IOException;

/**
 * @author mih Exception indicating that the requested resource does not exist in the system.
 */
public class NotFoundException extends IOException {

    static final long serialVersionUID = 7818975828146090155L;

    /**
     * Constructs an {@code NotFoundException} with {@code null} as its error detail message.
     */
    public NotFoundException() {
        super();
    }

    /**
     * Constructs an {@code NotFoundException} with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code NotFoundException} with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated into this
     * exception's detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method). (A null value is
     *        permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an {@code NotFoundException} with the specified cause and a detail message of
     * {@code (cause==null ? null : cause.toString())} (which typically contains the class and detail message of
     * {@code cause}). This constructor is useful for IO exceptions that are little more than wrappers for other
     * throwables.
     *
     * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method). (A null value is
     *        permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public NotFoundException(Throwable cause) {
        super(cause);
    }
}
