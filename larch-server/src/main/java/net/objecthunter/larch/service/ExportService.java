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

import java.io.IOException;

import net.objecthunter.larch.model.Entity;

/**
 * Service definition for the export service which allows to write JSON representation of an
 * {@link net.objecthunter .larch.model.Entity} to a given target
 */
public interface ExportService {

    /**
     * Export the Entity to a target like a FileSystem or a Database depending on the implementation
     * 
     * @param e The {@link net.objecthunter.larch.model.Entity} to export
     * @throws IOException
     */
    void export(Entity e) throws IOException;
}
