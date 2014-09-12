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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.objecthunter.larch.service;

public interface MessagingService {

    public void publishCreateEntity(String entityId);

    public void publishUpdateEntity(String entityId);

    public void publishDeleteEntity(String entityId);

    public void publishCreateBinary(String entityId, String binaryName);

    public void publishUpdateBinary(String entityId, String binaryName);

    public void publishDeleteBinary(String entityId, String binaryName);

    public void publishCreateMetadata(String entityId, String mdName);

    public void publishUpdateMetadata(String entityId, String mdName);

    public void publishDeleteMetadata(String entityId, String mdname);

    public void publishCreateIdentifier(String entityId, String type, String value);

    public void publishDeleteIdentifier(String entityId, String type, String value);

    public void publishCreateBinaryMetadata(String entityId, String binaryName, String mdName);

    public void publishUpdateBinaryMetadata(String entityId, String binaryName, String mdName);

    public void publishDeleteBinaryMetadata(String entityId, String binaryName, String mdName);

    public void publishAddUser(String userName);

    public void publishDeleteUser(String userName);

    public void publishUpdateUser(String userName);

    public void publishCreateRelation(String subject, String predicate, String object);

    public void publishDeleteRelation(String subject, String predicate, String object);

    void publishSubmitEntity(String id);

    void publishPublishEntity(String id);

    void publishWithdrawEntity(String id);

}
