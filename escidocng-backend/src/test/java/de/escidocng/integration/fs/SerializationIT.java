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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import de.escidocng.test.util.Fixtures;

import org.junit.Test;

import de.escidocng.model.Entity;

public class SerializationIT extends AbstractFSEscidocngIT {

    @Test
    public void testSerializeEntity() throws Exception {
        Entity e = Fixtures.createFixtureEntity(false);
        e.setUtcCreated(ZonedDateTime.now(ZoneOffset.UTC).toString());
        Entity copy = mapper.readValue(mapper.writeValueAsString(e), Entity.class);
        assertEquals(e.getUtcCreated(), copy.getUtcCreated());
    }

}
