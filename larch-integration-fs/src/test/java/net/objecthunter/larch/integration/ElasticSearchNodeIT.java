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

import static org.junit.Assert.assertTrue;

import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchNode;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ElasticSearchNodeIT extends AbstractLarchIT {

    @Autowired
    ElasticSearchNode node;

    @Test
    public void testElasticSearchNodeAlive() {
        assertTrue(node.isAlive());
    }

}
