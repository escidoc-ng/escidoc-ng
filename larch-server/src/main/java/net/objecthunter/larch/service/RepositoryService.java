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

package net.objecthunter.larch.service;

import java.io.IOException;

import net.objecthunter.larch.model.Describe;
import net.objecthunter.larch.model.state.LarchState;

/**
 * Service definition for the repository service responsible for getting the state of a repository
 */
public interface RepositoryService {

    /**
     * Retrieve the current {@link net.objecthunter.larch.model.state.LarchState} form the repository containing
     * status information about the index and the blob store
     * 
     * @return a {@link net.objecthunter.larch.model.state.LarchState} containing the stats
     * @throws IOException
     */
    LarchState status() throws IOException;

    /**
     * Retrieve a {@link net.objecthunter.larch.model.Describe} object from the repository containing e.g. cluster
     * name and node information
     * 
     * @return A {@link net.objecthunter.larch.model.Describe} object containing the requested data
     */
    Describe describe() throws IOException;
}
