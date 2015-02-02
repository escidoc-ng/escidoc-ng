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

import static net.objecthunter.larch.test.util.Fixtures.createSimpleFixtureEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.objecthunter.larch.exceptions.InvalidParameterException;
import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.model.Entity;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdentifierControllerIT extends AbstractFSLarchIT {

    private static final Logger log = LoggerFactory.getLogger(IdentifierControllerIT.class);

    private static final String identifierUrl = entityUrl + "/{id}/identifier/";

    @Test
    public void testCreateIdentifier() throws Exception {
        // create entity
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl).bodyString(mapper.writeValueAsString(createSimpleFixtureEntity()),
                                ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String entityId = EntityUtils.toString(resp.getEntity());

        // create new identifier
        resp =
                this.executeAsAdmin(
                        Request.Post(identifierUrl.replaceFirst("\\{id\\}", entityId)).bodyString(
                                "type=DOI&value=123",
                                ContentType.APPLICATION_FORM_URLENCODED));
        assertEquals(201, resp.getStatusLine().getStatusCode());

        // retrieve entity
        resp = this.executeAsAdmin(Request.Get(entityUrl + "/" + entityId));
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);

        // check alternative identifiers
        assertNotNull(fetched.getAlternativeIdentifiers());
        assertEquals(1, fetched.getAlternativeIdentifiers().size());
        assertEquals("DOI", fetched.getAlternativeIdentifiers().get(0).getType());
        assertEquals("123", fetched.getAlternativeIdentifiers().get(0).getValue());
    }

    @Test
    public void testDecliningCreateIdentifierEmptyType() throws Exception {
        // create entity
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl).bodyString(mapper.writeValueAsString(createSimpleFixtureEntity()),
                                ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String entityId = EntityUtils.toString(resp.getEntity());

        // create new identifier
        this.hideLog();
        resp =
                this.executeAsAdmin(
                        Request.Post(identifierUrl.replaceFirst("\\{id\\}", entityId)).bodyString("type=&value=123",
                                ContentType.APPLICATION_FORM_URLENCODED));
        this.showLog();
        checkResponseError(resp, 400, InvalidParameterException.class, "empty type or value given");

        // retrieve entity
        resp = this.executeAsAdmin(Request.Get(entityUrl + "/" + entityId));
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);

        // check alternative identifiers
        assertNotNull(fetched.getAlternativeIdentifiers());
        assertEquals(0, fetched.getAlternativeIdentifiers().size());
    }

    @Test
    public void testDecliningCreateIdentifierEmptyValue() throws Exception {
        // create entity
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl).bodyString(mapper.writeValueAsString(createSimpleFixtureEntity()),
                                ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String entityId = EntityUtils.toString(resp.getEntity());

        // create new identifier
        this.hideLog();
        resp =
                this.executeAsAdmin(
                        Request.Post(identifierUrl.replaceFirst("\\{id\\}", entityId)).bodyString("type=DOI&value=",
                                ContentType.APPLICATION_FORM_URLENCODED));
        this.showLog();
        checkResponseError(resp, 400, InvalidParameterException.class, "empty type or value given");

        // retrieve entity
        resp = this.executeAsAdmin(Request.Get(entityUrl + "/" + entityId));
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);

        // check alternative identifiers
        assertNotNull(fetched.getAlternativeIdentifiers());
        assertEquals(0, fetched.getAlternativeIdentifiers().size());
    }

    @Test
    public void testDecliningCreateIdentifierWrongType() throws Exception {
        // create entity
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl).bodyString(mapper.writeValueAsString(createSimpleFixtureEntity()),
                                ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String entityId = EntityUtils.toString(resp.getEntity());

        // create new identifier
        this.hideLog();
        resp =
                this.executeAsAdmin(
                        Request.Post(identifierUrl.replaceFirst("\\{id\\}", entityId)).bodyString(
                                "type=DDOI&value=123",
                                ContentType.APPLICATION_FORM_URLENCODED));
        this.showLog();
        checkResponseError(resp, 400, InvalidParameterException.class, "wrong type given");

        // retrieve entity
        resp = this.executeAsAdmin(Request.Get(entityUrl + "/" + entityId));
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);

        // check alternative identifiers
        assertNotNull(fetched.getAlternativeIdentifiers());
        assertEquals(0, fetched.getAlternativeIdentifiers().size());
    }

    @Test
    public void testDeleteIdentifier() throws Exception {
        // create entity
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl).bodyString(mapper.writeValueAsString(createSimpleFixtureEntity()),
                                ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String entityId = EntityUtils.toString(resp.getEntity());

        // create new identifier
        resp =
                this.executeAsAdmin(
                        Request.Post(identifierUrl.replaceFirst("\\{id\\}", entityId)).bodyString(
                                "type=DOI&value=123",
                                ContentType.APPLICATION_FORM_URLENCODED));
        assertEquals(201, resp.getStatusLine().getStatusCode());

        // retrieve entity
        resp = this.executeAsAdmin(Request.Get(entityUrl + "/" + entityId));
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);

        // check alternative identifiers
        assertNotNull(fetched.getAlternativeIdentifiers());
        assertEquals(1, fetched.getAlternativeIdentifiers().size());
        assertEquals("DOI", fetched.getAlternativeIdentifiers().get(0).getType());
        assertEquals("123", fetched.getAlternativeIdentifiers().get(0).getValue());

        // delete alternative identifier
        resp =
                this.executeAsAdmin(Request.Delete(identifierUrl.replaceFirst("\\{id\\}", entityId) + "/DOI/123"));
        assertEquals(200, resp.getStatusLine().getStatusCode());

        // retrieve entity
        resp = this.executeAsAdmin(Request.Get(entityUrl + "/" + entityId));
        fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);

        // check alternative identifiers
        assertNotNull(fetched.getAlternativeIdentifiers());
        assertEquals(0, fetched.getAlternativeIdentifiers().size());
    }

    @Test
    public void testDecliningDeleteIdentifierWrongType() throws Exception {
        // create entity
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl).bodyString(mapper.writeValueAsString(createSimpleFixtureEntity()),
                                ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String entityId = EntityUtils.toString(resp.getEntity());

        // create new identifier
        resp =
                this.executeAsAdmin(
                        Request.Post(identifierUrl.replaceFirst("\\{id\\}", entityId)).bodyString(
                                "type=DOI&value=123",
                                ContentType.APPLICATION_FORM_URLENCODED));
        assertEquals(201, resp.getStatusLine().getStatusCode());

        // retrieve entity
        resp = this.executeAsAdmin(Request.Get(entityUrl + "/" + entityId));
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);

        // check alternative identifiers
        assertNotNull(fetched.getAlternativeIdentifiers());
        assertEquals(1, fetched.getAlternativeIdentifiers().size());
        assertEquals("DOI", fetched.getAlternativeIdentifiers().get(0).getType());
        assertEquals("123", fetched.getAlternativeIdentifiers().get(0).getValue());

        // delete alternative identifier
        this.hideLog();
        resp =
                this.executeAsAdmin(Request.Delete(identifierUrl.replaceFirst("\\{id\\}", entityId) + "/DDOI/123"));
        this.showLog();
        checkResponseError(resp, 400, InvalidParameterException.class, "wrong type given");

        // retrieve entity
        resp = this.executeAsAdmin(Request.Get(entityUrl + "/" + entityId));
        fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);

        // check alternative identifiers
        assertNotNull(fetched.getAlternativeIdentifiers());
        assertEquals(1, fetched.getAlternativeIdentifiers().size());
        assertEquals("DOI", fetched.getAlternativeIdentifiers().get(0).getType());
        assertEquals("123", fetched.getAlternativeIdentifiers().get(0).getValue());
    }

    @Test
    public void testDecliningDeleteIdentifierWrongValue() throws Exception {
        // create entity
        HttpResponse resp =
                this.executeAsAdmin(
                        Request.Post(entityUrl)
                                .bodyString(mapper.writeValueAsString(createSimpleFixtureEntity()),
                                        ContentType.APPLICATION_JSON));
        assertEquals(201, resp.getStatusLine().getStatusCode());
        final String entityId = EntityUtils.toString(resp.getEntity());

        // create new identifier
        resp =
                this.executeAsAdmin(
                        Request.Post(identifierUrl.replaceFirst("\\{id\\}", entityId))
                                .bodyString("type=DOI&value=123",
                                        ContentType.APPLICATION_FORM_URLENCODED));
        assertEquals(201, resp.getStatusLine().getStatusCode());

        // retrieve entity
        resp = this.executeAsAdmin(Request.Get(entityUrl + "/" + entityId));
        Entity fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);

        // check alternative identifiers
        assertNotNull(fetched.getAlternativeIdentifiers());
        assertEquals(1, fetched.getAlternativeIdentifiers().size());
        assertEquals("DOI", fetched.getAlternativeIdentifiers().get(0).getType());
        assertEquals("123", fetched.getAlternativeIdentifiers().get(0).getValue());

        // delete alternative identifier
        this.hideLog();
        resp =
                this.executeAsAdmin(Request.Delete(identifierUrl.replaceFirst("\\{id\\}", entityId) + "/DOI/1234"));
        this.showLog();
        checkResponseError(resp, 404, NotFoundException.class, "Identifier of type DOI with value 1234 not found");

        // retrieve entity
        resp = this.executeAsAdmin(Request.Get(entityUrl + "/" + entityId));
        fetched = mapper.readValue(resp.getEntity().getContent(), Entity.class);

        // check alternative identifiers
        assertNotNull(fetched.getAlternativeIdentifiers());
        assertEquals(1, fetched.getAlternativeIdentifiers().size());
        assertEquals("DOI", fetched.getAlternativeIdentifiers().get(0).getType());
        assertEquals("123", fetched.getAlternativeIdentifiers().get(0).getValue());
    }

}
