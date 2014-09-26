/* 
 * Copyright 2014 Frank Asseg
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

package net.objecthunter.larch.model;

/**
 * DTO class for holding a validation resukt
 */
public class MetadataValidationResult {

    private String timestamp;

    private boolean success;

    private String details;

    /**
     * Get the details of of validation result
     * 
     * @return the details
     */
    public String getDetails() {
        return details;
    }

    /**
     * Set the details of a validation result
     * 
     * @param details the details to set
     */
    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * Check if a validation was successful
     * 
     * @return true is successful false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Set the success of the validation result
     * 
     * @param success the boolean to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Get the timestamp of the validation result
     * 
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Set the timestamp of a validation result
     * 
     * @param timestamp the timestamp
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
