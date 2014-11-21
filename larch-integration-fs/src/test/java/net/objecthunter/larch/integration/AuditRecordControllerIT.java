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


package net.objecthunter.larch.integration;

import static net.objecthunter.larch.test.util.Fixtures.LEVEL2_ID;
import static org.junit.Assert.assertEquals;
import net.objecthunter.larch.model.AuditRecord;
import net.objecthunter.larch.model.AuditRecords;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditRecordControllerIT extends AbstractLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuditRecordControllerIT.class);

    @Test
    public void testRetrieveAuditRecords() throws Exception {
        // create submitted entity
        Entity entity = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), LEVEL2_ID);
        // add binary
        entity = addBinaryStream(entity, IGNORE, IGNORE, IGNORE, 201);
        // add Metadata
        entity = addMetadataStream(entity, IGNORE, IGNORE, 201);
        // add Binary Metadata
        entity = addBinaryMetadataStream(entity, IGNORE, IGNORE, IGNORE, 201);
        // add Identifier
        entity = addIdentifier(entity, IGNORE, IGNORE, 201);
        // add Relation
        entity = addRelation(entity, IGNORE, IGNORE, 201);
        // remove Identifier
        entity = removeIdentifier(entity, IGNORE, IGNORE, 200);
        // remove Binary Metadata
        entity = removeBinaryMetadata(entity, IGNORE, IGNORE, 200);
        // remove Metadata
        entity = removeMetadata(entity, IGNORE, 200);
        // remove Binary
        entity = removeBinary(entity, IGNORE, 200);

        String[] auditMessages = new String[] {
            "Create entity", 
            "Create binary",
            "Create metadata",
            "Create metadata for binary",
            "Create identifier",
            "Add relation",
            "Delete identifier",
            "Delete metadata for binary",
            "Delete metadata",
            "Delete binary",
        };

        // retrieve audit records
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + entity.getId() + "/audit"));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        AuditRecords fetched = mapper.readValue(resp.getEntity().getContent(), AuditRecords.class);
        int i = 0;
        for (AuditRecord auditRecord : fetched.getAuditRecords()) {
            assertEquals(auditMessages[i], auditRecord.getAction());
            i++;
        }
    }
    
    @Test
    public void testRetrieveAuditRecordsWrongId() throws Exception {
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Get(entityUrl + "wrongid/audit"));
        assertEquals(200, resp.getStatusLine().getStatusCode());
        AuditRecords fetched = mapper.readValue(resp.getEntity().getContent(), AuditRecords.class);
        assertEquals(0, fetched.getAuditRecords().size());
    }
    

}
