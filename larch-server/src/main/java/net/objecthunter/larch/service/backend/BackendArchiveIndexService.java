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
package net.objecthunter.larch.service.backend;

import java.io.IOException;
import java.util.List;

import net.objecthunter.larch.model.Archive;

public interface BackendArchiveIndexService {
    Archive retrieve(String id, int version) throws IOException;

    void saveOrUpdate(Archive a) throws IOException;

    void delete(String entityId, int version) throws IOException;

    boolean exists(String id, int version) throws IOException;

    List<Archive> list(int offset, int count) throws IOException;
}
