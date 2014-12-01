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

package net.objecthunter.larch.integration.fs;

import static org.junit.Assert.assertNotNull;

import net.objecthunter.larch.model.state.LarchState;
import net.objecthunter.larch.service.impl.DefaultRepositoryService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultRepositoryServiceIT extends AbstractLarchIT {

    @Autowired
    private DefaultRepositoryService service;

    @Test
    public void testGetState() throws Exception {
        LarchState state = service.status();
        assertNotNull(state);
        assertNotNull(state.getBlobstoreState());
        assertNotNull(state.getIndexState());
    }

}
