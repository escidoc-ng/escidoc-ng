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

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import static net.objecthunter.larch.test.util.Fixtures.createFixtureEntity;
import static org.junit.Assert.assertEquals;

public class ArchiveControllerIT extends AbstractLarchIT {

    @Test
    public void testArchive() throws Exception {
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl).bodyString(mapper.writeValueAsString(createFixtureEntity()),
                                ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String id = EntityUtils.toString(resp.getEntity());
        resp = this.executeAsAdmin(Request.Post(hostUrl + "/archive/" + id + "/1"));
        assertEquals(201, resp.getStatusLine().getStatusCode());
    }
}
