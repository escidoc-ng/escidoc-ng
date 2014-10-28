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
package net.objecthunter.larch.service;

import net.objecthunter.larch.model.Entity;

import java.io.IOException;

public interface ArchiveService {
    void create(Entity e) throws IOException;
    Entity retrieve(Entity e) throws IOException;
    void update(Entity e) throws IOException;
    void delete(String entityId) throws IOException;
}
