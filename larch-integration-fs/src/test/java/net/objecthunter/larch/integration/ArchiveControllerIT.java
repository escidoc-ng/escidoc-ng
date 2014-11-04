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
package net.objecthunter.larch.integration;

import net.objecthunter.larch.model.Entity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.Test;

import static net.objecthunter.larch.test.util.Fixtures.createFixtureEntity;
import static org.junit.Assert.assertEquals;

public class ArchiveControllerIT extends AbstractLarchIT {

    @Test
    public void testArchive() throws Exception {
        Entity e = this.archive(createFixtureEntity());
    }

    @Test
    public void testArchiveAndRetrieve() throws Exception {
        Entity e = this.archive(createFixtureEntity());
        HttpResponse resp = this.retrieveArchive(e.getId(), e.getVersion());
        assertEquals(200, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testArchiveNonExisting() throws Exception {
        HttpResponse resp = this.executeAsAdmin(Request.Put(hostUrl + "/archive/foo_entity_NON_EXISTANT/1"));
        assertEquals(404, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrieveNonExisting() throws Exception {
        HttpResponse resp = this.retrieveArchive("FOO_NO_EXIST", -1);
        assertEquals(404, resp.getStatusLine().getStatusCode());
    }

    @Test
    public void testArchiveAndRetrieveNonExistingVersion() throws Exception {
        Entity e = createFixtureEntity();
        this.archive(e);
        HttpResponse resp = this.retrieveArchive(e.getId(), Integer.MIN_VALUE);
        assertEquals(404, resp.getStatusLine().getStatusCode());
    }
}
