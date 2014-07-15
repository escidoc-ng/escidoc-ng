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

import static org.junit.Assert.assertEquals;

import net.objecthunter.larch.model.Describe;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DescribeControllerIT extends AbstractLarchIT {

    @Test
    public void testDescribe() throws Exception {
        HttpResponse resp = Request.Get(hostUrl + "/describe")
                .execute()
                .returnResponse();
        assertEquals(200, resp.getStatusLine().getStatusCode());
        String data = EntityUtils.toString(resp.getEntity());
        Describe desc = mapper.readValue(data, Describe.class);
    }
}
