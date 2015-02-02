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

package de.escidocng.service;

import java.io.IOException;

import de.escidocng.model.security.UserRequest;

/**
 * Interface definition for the mail service
 */
public interface MailService {

    /**
     * Send a registration email for a user request
     * @param req the user request containing the user information
     * @throws IOException
     */
    public void sendUserRequest(UserRequest req) throws IOException;

    /**
     * Check if the MailService is enabled
     * @return true if the MailService is enabled, otherwise false
     */
    public boolean isEnabled();
}
