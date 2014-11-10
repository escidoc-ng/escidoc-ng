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

package net.objecthunter.larch.helpers;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import net.objecthunter.larch.model.AuditRecord;
import net.objecthunter.larch.model.security.User;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * A Factory class for creating different kinds of {@link net.objecthunter.larch.model.AuditRecord} objects
 */
public abstract class AuditRecordHelper {

    /**
     * Create an almost empty {@link net.objecthunter.larch.model.AuditRecord}
     * 
     * @param entityId the id of the Entity this {@link net.objecthunter.larch.model.AuditRecord} will be associated
     *        with
     * @return An almost empty {@link net.objecthunter.larch.model.AuditRecord} ready to be further filled
     */
    public static AuditRecord skeletonRecord(String entityId) {
        final AuditRecord record = new AuditRecord();
        record.setEntityId(entityId);
        record.setTimestamp(ZonedDateTime.now(ZoneOffset.UTC).toString());
        final User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        record.setAgentName(u.getName());
        return record;
    }

    /**
     * Create an {@link net.objecthunter.larch.model.AuditRecord} for a "Create Entity" event
     * 
     * @param entityId the id of the entity created
     * @return the AuditRecord
     */
    public static AuditRecord createEntityRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_CREATE_ENTITY);
        return record;
    }

    /**
     * Create an {@link net.objecthunter.larch.model.AuditRecord} event for an "Update Entity" event
     * 
     * @param entityId the id of the updated entity
     * @return
     */
    public static AuditRecord updateEntityRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_UPDATE_ENTITY);
        return record;
    }

    /**
     * Create a {@link net.objecthunter.larch.model.AuditRecord} for a "Delete Entity" event
     * 
     * @param entityId the id of the Entity deleted
     * @return
     */
    public static AuditRecord deleteEntityRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_DELETE_ENTITY);
        return record;
    }

    /**
     * Create an {@link net.objecthunter.larch.model.AuditRecord} for a "Create Binary" event
     * 
     * @param entityId the id of the entity the binary got created on
     * @return
     */
    public static AuditRecord createBinaryRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_CREATE_BINARY);
        return record;
    }

    /**
     * create an {@link net.objecthunter.larch.model.AuditRecord} for an "Update Binary" event
     * 
     * @param entityId The id of the entity on which the {@link net.objecthunter.larch.model.Binary} got updated
     * @return
     */
    public static AuditRecord updateBinaryRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_UPDATE_BINARY);
        return record;
    }

    /**
     * Create an {@link net.objecthunter.larch.model.AuditRecord} for an "Delete Binary" event
     * 
     * @param entityId the id the entity which's Binary got deleted
     * @return
     */
    public static AuditRecord deleteBinaryRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_DELETE_BINARY);
        return record;
    }

    /**
     * Create an {@link net.objecthunter.larch.model.AuditRecord} for a "Create Relation" event
     * 
     * @param entityId the id of the entity the relation is created on
     * @return
     */
    public static AuditRecord createRelationRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_CREATE_RELATION);
        return record;
    }

    /**
     * Create an {@link net.objecthunter.larch.model.AuditRecord} for a "Update Relation" event
     * 
     * @param entityId the is of the {@link net.objecthunter.larch.model.Entity} on which a relation got updated
     * @return
     */
    public static AuditRecord updateRelationRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_UPDATE_RELATION);
        return record;
    }

    /**
     * Create an {@link net.objecthunter.larch.model.AuditRecord} for a "Delete Relation" event
     * 
     * @param entityId the id of the {@link net.objecthunter.larch.model.Entity} on which a relation got deleted
     * @return
     */
    public static AuditRecord deleteRelationRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_DELETE_RELATION);
        return record;
    }

    /**
     * Create an {@link net.objecthunter.larch.model.AuditRecord} for a "Create Metadata" event
     * 
     * @param entityId the id of the {@link net.objecthunter.larch.model.Entity} on which the
     *        {@link net.objecthunter .larch.model.Metadata} gets created
     * @return
     */
    public static AuditRecord createMetadataRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_CREATE_METADATA);
        return record;
    }

    /**
     * Create an {@link net.objecthunter.larch.model.AuditRecord} for a "Update Metadata" event
     * 
     * @param entityId the id of the {@link net.objecthunter.larch.model.Entity} on which the
     *        {@link net.objecthunter .larch.model.Metadata} gets updated
     * @return
     */
    public static AuditRecord updateMetadataRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_UPDATE_METADATA);
        return record;
    }

    /**
     * Create an {@link net.objecthunter.larch.model.AuditRecord} for a "Delete Metadata" event
     * 
     * @param entityId the id of the {@link net.objecthunter.larch.model.Entity} on which the
     *        {@link net.objecthunter .larch.model.Metadata} got deleted
     * @return
     */
    public static AuditRecord deleteMetadataRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_DELETE_METADATA);
        return record;
    }

    /**
     * Create an {@link net.objecthunter.larch.model.AuditRecord} for a "Create binaryMetadata" event
     * 
     * @param entityId the id of the {@link net.objecthunter.larch.model.Entity} on which the
     *        {@link net.objecthunter .larch.model.Metadata} gets created
     * @return
     */
    public static AuditRecord createBinaryMetadataRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_CREATE_BINARY_METADATA);
        return record;
    }

    /**
     * Create an {@link net.objecthunter.larch.model.AuditRecord} for a "Update binary Metadata" event
     * 
     * @param entityId the id of the {@link net.objecthunter.larch.model.Entity} on which the
     *        {@link net.objecthunter .larch.model.Metadata} gets updated
     * @return
     */
    public static AuditRecord updateBinaryMetadataRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_UPDATE_BINARY_METADATA);
        return record;
    }

    /**
     * Create an {@link net.objecthunter.larch.model.AuditRecord} for a "Delete binary Metadata" event
     * 
     * @param entityId the id of the {@link net.objecthunter.larch.model.Entity} on which the
     *        {@link net.objecthunter .larch.model.Metadata} got deleted
     * @return
     */
    public static AuditRecord deleteBinaryMetadataRecord(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_DELETE_BINARY_METADATA);
        return record;
    }

    /**
     * Create an {@link net.objecthunter.larch.model.AuditRecord} for a "Delete Identifier" event
     * 
     * @param entityId the id of the {@link net.objecthunter.larch.model.Entity} on which the identifier got deleted
     * @return
     */
    public static AuditRecord deleteIdentifier(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_DELETE_IDENTIFIER);
        return record;
    }

    /**
     * Create an {@link net.objecthunter.larch.model.AuditRecord} for a "Create Metadata" event
     * 
     * @param entityId the id of the {@link net.objecthunter.larch.model.Entity} on which the
     *        {@link net.objecthunter .larch.model.Metadata} gets created
     * @return
     */
    public static AuditRecord createIdentifier(String entityId) {
        final AuditRecord record = skeletonRecord(entityId);
        record.setAction(AuditRecord.EVENT_CREATE_IDENTIFIER);
        return record;
    }

    public static AuditRecord publishEntityRecord(String id) {
        final AuditRecord rec = skeletonRecord(id);
        rec.setAction(AuditRecord.EVENT_PUBLISH_ENTITY);
        return rec;
    }

    public static AuditRecord submitEntityRecord(String id) {
        final AuditRecord rec = skeletonRecord(id);
        rec.setAction(AuditRecord.EVENT_SUBMIT_ENTITY);
        return rec;
    }

    public static AuditRecord withdrawEntityRecord(String id) {
        final AuditRecord rec = skeletonRecord(id);
        rec.setAction(AuditRecord.EVENT_WITHDRAW_ENTITY);
        return rec;
    }

    public static AuditRecord pendingEntityRecord(String id) {
        final AuditRecord rec = skeletonRecord(id);
        rec.setAction(AuditRecord.EVENT_PENDING_ENTITY);
        return rec;
    }
}
