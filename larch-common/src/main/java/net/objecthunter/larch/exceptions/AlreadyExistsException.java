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
package net.objecthunter.larch.exceptions;

import java.io.IOException;

/**
 * @author mih
 * Exception indicating that the a resource with the given primary key already exists in the system.
 */
public class AlreadyExistsException extends IOException {

    static final long serialVersionUID = 7818875828146090155L;

    /**
     * Constructs an {@code AlreadyExistsException} with {@code null} as its error detail message.
     */
    public AlreadyExistsException() {
        super();
    }

    /**
     * Constructs an {@code AlreadyExistsException} with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public AlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code AlreadyExistsException} with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated into this
     * exception's detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method). (A null value is
     *        permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public AlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an {@code AlreadyExistsException} with the specified cause and a detail message of
     * {@code (cause==null ? null : cause.toString())} (which typically contains the class and detail message of
     * {@code cause}). This constructor is useful for IO exceptions that are little more than wrappers for other
     * throwables.
     *
     * @param cause The cause (which is saved for later retrieval by the {@link #getCause()} method). (A null value is
     *        permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public AlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
