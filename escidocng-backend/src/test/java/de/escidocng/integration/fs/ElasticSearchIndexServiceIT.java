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

package de.escidocng.integration.fs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.escidocng.model.state.IndexState;
import de.escidocng.service.backend.elasticsearch.ElasticSearchEntityService;

public class ElasticSearchIndexServiceIT extends AbstractFSEscidocngIT {

    @Autowired
    private ElasticSearchEntityService service;

    @Test
    public void testGetState() throws Exception {
        IndexState state = service.status();
        IndexState copy = mapper.readValue(mapper.writeValueAsString(state), IndexState.class);
        assertEquals(state.getName(), copy.getName());
        assertEquals(state.getMaxDocs(), copy.getMaxDocs());
        assertEquals(state.getNumDocs(), copy.getNumDocs());
        assertEquals(state.getNumDocsToMerge(), copy.getNumDocsToMerge());
        assertEquals(state.getShardsSize(), copy.getShardsSize());
        assertEquals(state.getSizeToMerge(), copy.getSizeToMerge());
        assertEquals(state.getTotalFlushTime(), copy.getTotalFlushTime());
        assertEquals(state.getTotalMergeTime(), copy.getTotalMergeTime());
        assertEquals(state.getTotalRefreshTime(), copy.getTotalRefreshTime());
    }
}
