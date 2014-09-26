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

import java.util.ArrayList;
import java.util.List;

/**
 * Object holds a List of AuditRecord-Objects.
 */
public class AuditRecords {

    private List<AuditRecord> auditRecords = new ArrayList<AuditRecord>();

    /**
     * Default Constructor.
     */
    public AuditRecords() {
    }

    /**
     * Constructor with List of AuditRecord-Objects.
     * 
     * @param auditRecords
     */
    public AuditRecords(List<AuditRecord> auditRecords) {
        if (auditRecords != null) {
            this.auditRecords = auditRecords;
        }
    }

    /**
     * @return the auditRecords
     */
    public List<AuditRecord> getAuditRecords() {
        return auditRecords;
    }

    /**
     * @param auditRecords the auditRecords to set
     */
    public void setAuditRecords(List<AuditRecord> auditRecords) {
        if (auditRecords == null) {
            this.auditRecords = new ArrayList<AuditRecord>();
        }
        else {
            this.auditRecords = auditRecords;
        }
    }

}
