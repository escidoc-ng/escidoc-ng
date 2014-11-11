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


package net.objecthunter.larch.integration.authorize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.objecthunter.larch.integration.helpers.AuthConfigurer.MissingPermission;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;
import net.objecthunter.larch.test.util.Fixtures;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.type.TypeReference;

public class AuthorizeSearchControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeSearchControllerIT.class);

    private static int methodCounter = 0;

    private static int totalLevel1sCount = 0;

    private static int totalLevel1Level2Count = 0;

    private static int totalLevel2sCount = 0;

    private static int totalPendingEntitiesCount = 0;

    private static int totalSubmittedEntitiesCount = 0;

    private static int totalPublishedEntitiesCount = 0;

    private static int totalWithdrawnEntitiesCount = 0;

    private static int totalLevel2PendingEntitiesCount = 3;

    private static int totalLevel2SubmittedEntitiesCount = 4;

    private static int totalLevel2PublishedEntitiesCount = 2;

    private static int totalLevel2WithdrawnEntitiesCount = 1;

    private static int totalUsersCount = 0;

    @Before
    public void initialize() throws Exception {
        if (methodCounter == 0) {
            prepareLevel2();
            prepareSearch();
            methodCounter++;
        }
    }

    /**
     * test searching for entities and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testSearch() throws Exception {
        String query = "(state:PENDING OR state:SUBMITTED OR state:PUBLISHED OR state:WITHDRAWN) AND contentModelId:data";
        String url = hostUrl + "search?query=" + URLEncoder.encode(query, "UTF-8");

        // user with no level2 rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.ALL)[0], userRoleUsernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // user with no read pending metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_PENDING_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel2PublishedEntitiesCount +
                totalLevel2SubmittedEntitiesCount + totalLevel2WithdrawnEntitiesCount, getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel2PublishedEntitiesCount +
                totalLevel2PendingEntitiesCount + totalLevel2WithdrawnEntitiesCount, getHitCount(response));

        // user with no read published metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_PUBLISHED_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_PUBLISHED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel2SubmittedEntitiesCount +
                totalLevel2PendingEntitiesCount + totalLevel2WithdrawnEntitiesCount, getHitCount(response));

        // user with no read withdrawn metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_WITHDRAWN_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_WITHDRAWN_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel2PublishedEntitiesCount +
                totalLevel2PendingEntitiesCount + totalLevel2SubmittedEntitiesCount, getHitCount(response));

        // user with all read metadata rights
        for (Entry<MissingPermission, String[]> entry : userRoleUsernames.entrySet()) {
            if (!MissingPermission.READ_SUBMITTED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PENDING_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PUBLISHED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_WITHDRAWN_METADATA.equals(entry.getKey())
                    && !MissingPermission.ALL.equals(entry.getKey())) {
                response =
                        this.executeAsUser(HttpMethod.GET, url, null,
                                entry.getValue()[0], entry.getValue()[1], false);
                assertEquals(200, response.getStatusLine().getStatusCode());
                assertEquals(totalLevel2PublishedEntitiesCount +
                        totalLevel2PendingEntitiesCount +
                        totalLevel2SubmittedEntitiesCount + totalLevel2WithdrawnEntitiesCount,
                        getHitCount(response));
            }
        }

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.GET, url, null, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null, adminUsername, adminPassword, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPublishedEntitiesCount + totalLevel2PublishedEntitiesCount + totalPendingEntitiesCount +
                totalSubmittedEntitiesCount +
                totalLevel2PendingEntitiesCount + totalLevel2SubmittedEntitiesCount +
                totalWithdrawnEntitiesCount + totalLevel2WithdrawnEntitiesCount, getHitCount(response));
    }

    /**
     * test searching entities belonging to a workspace and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testSearchInLevel2() throws Exception {
        String query = "(state:PENDING OR state:SUBMITTED OR state:PUBLISHED OR state:WITHDRAWN) AND contentModelId:data AND level2:" + level2Id;
        String url = hostUrl + "search?query=" + URLEncoder.encode(query, "UTF-8");
        // user with no level2 rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.ALL)[0], userRoleUsernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // user with no read pending metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_PENDING_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel2PublishedEntitiesCount + totalLevel2SubmittedEntitiesCount +
                totalLevel2WithdrawnEntitiesCount,
                getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel2PublishedEntitiesCount + totalLevel2PendingEntitiesCount +
                totalLevel2WithdrawnEntitiesCount, getHitCount(response));

        // user with no read published metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_PUBLISHED_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_PUBLISHED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel2SubmittedEntitiesCount + totalLevel2PendingEntitiesCount +
                totalLevel2WithdrawnEntitiesCount, getHitCount(response));

        // user with no read withdrawn metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_WITHDRAWN_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_WITHDRAWN_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel2PublishedEntitiesCount + totalLevel2PendingEntitiesCount +
                totalLevel2SubmittedEntitiesCount, getHitCount(response));

        // user with all read metadata rights
        for (Entry<MissingPermission, String[]> entry : userRoleUsernames.entrySet()) {
            if (!MissingPermission.READ_SUBMITTED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PENDING_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PUBLISHED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_WITHDRAWN_METADATA.equals(entry.getKey())
                    && !MissingPermission.ALL.equals(entry.getKey())) {
                response =
                        this.executeAsUser(HttpMethod.GET, url, null,
                                entry.getValue()[0], entry.getValue()[1], false);
                assertEquals(200, response.getStatusLine().getStatusCode());
                assertEquals(totalLevel2PublishedEntitiesCount + totalLevel2PendingEntitiesCount +
                        totalLevel2SubmittedEntitiesCount + totalLevel2WithdrawnEntitiesCount,
                        getHitCount(response));
            }
        }

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.GET, url, null, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null, adminUsername, adminPassword, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel2PublishedEntitiesCount + totalLevel2PendingEntitiesCount +
                totalLevel2SubmittedEntitiesCount + totalLevel2WithdrawnEntitiesCount, getHitCount(response));
    }

    /**
     * test searching entities being a permission and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testSearchForLevel2s() throws Exception {
        String query = "contentModelId:level2";
        String url = hostUrl + "search?query=" + URLEncoder.encode(query, "UTF-8");
        // user with no level2 rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.ALL)[0], userRoleUsernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // user with read level2 rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_PENDING_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(1, getHitCount(response));

        // level1-admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel1Level2Count + 1, getHitCount(response));

        // level1-admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + level1Id1)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(1, getHitCount(response));

        // user-admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userAdminRoleUsernames.get("ROLE_USER_ADMIN")[0], userAdminRoleUsernames.get("ROLE_USER_ADMIN")[1],
                        false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(1, getHitCount(response));

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.GET, url, null, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null, adminUsername, adminPassword, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel2sCount, getHitCount(response));
    }

    /**
     * test searching entities being a permission and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testSearchForLevel1s() throws Exception {
        String query = "contentModelId:level1";
        String url = hostUrl + "search?query=" + URLEncoder.encode(query, "UTF-8");
        // user with no level2 rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.ALL)[0], userRoleUsernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // user with read level2 rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_PENDING_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(1, getHitCount(response));

        // level1-admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(1, getHitCount(response));

        // level1-admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + level1Id1)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(1, getHitCount(response));

        // user-admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userAdminRoleUsernames.get("ROLE_USER_ADMIN")[0], userAdminRoleUsernames.get("ROLE_USER_ADMIN")[1],
                        false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(1, getHitCount(response));

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.GET, url, null, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null, adminUsername, adminPassword, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel1sCount, getHitCount(response));
    }

    /**
     * test lising users and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testListUsers() throws Exception {
        String url = hostUrl + "/search/users";
        // user with no rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.ALL)[0], userRoleUsernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(1, getHitCount(response));

        // user with read level2 rights + level1_admin rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_PENDING_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalUsersCount, getHitCount(response));

        // level1-admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalUsersCount, getHitCount(response));

        // level1-admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + level1Id1)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalUsersCount, getHitCount(response));

        // user-admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userAdminRoleUsernames.get("ROLE_USER_ADMIN")[0], userAdminRoleUsernames.get("ROLE_USER_ADMIN")[1],
                        false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalUsersCount, getHitCount(response));

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.GET, url, null, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null, adminUsername, adminPassword, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalUsersCount, getHitCount(response));
    }

    /**
     * Get all already indexed entities. Create entities with different status to test search. Save counts.
     */
    private void prepareSearch() throws Exception {
        // get total hits
        String dataGetParameters = "query=contentModelId:data&maxRecords=50";
        String level2GetParameters = "query=contentModelId:level2&maxRecords=50";
        String level1GetParameters = "query=contentModelId:level1&maxRecords=50";
        String url = hostUrl + "search?";
        HttpResponse response =
                this.executeAsAdmin(Request.Get(url + dataGetParameters));
        assertEquals(200, response.getStatusLine().getStatusCode());
        long totalHits = getHitCount(response);

        int counter = 0;
        List<HashMap> entities = new ArrayList<HashMap>();
        while (counter <= totalHits) {
            response = this.executeAsAdmin(Request.Get(url + dataGetParameters + "&offset=" + counter));
            counter += 50;
            assertEquals(200, response.getStatusLine().getStatusCode());
            SearchResult searchResult =
                    mapper.readValue(EntityUtils.toString(response.getEntity()), SearchResult.class);
            entities.addAll((List<HashMap>) searchResult.getData());
        }
        for (HashMap entity : entities) {
            if (EntityState.PENDING.name().equals(entity.get("state"))) {
                totalPendingEntitiesCount++;
            } else if (EntityState.SUBMITTED.name().equals(entity.get("state"))) {
                totalSubmittedEntitiesCount++;
            } else if (EntityState.PUBLISHED.name().equals(entity.get("state"))) {
                totalPublishedEntitiesCount++;
            } else if (EntityState.WITHDRAWN.name().equals(entity.get("state"))) {
                totalWithdrawnEntitiesCount++;
            }
        }

        // get permissions
        response = this.executeAsAdmin(Request.Get(url + level2GetParameters));
        assertEquals(200, response.getStatusLine().getStatusCode());
        totalLevel2sCount = (int) getHitCount(response);
        counter = 0;
        while (counter <= totalLevel2sCount) {
            response = this.executeAsAdmin(Request.Post(url + level2GetParameters + "&offset=" + counter));
            counter += 50;
            assertEquals(200, response.getStatusLine().getStatusCode());
            SearchResult searchResult =
                    mapper.readValue(EntityUtils.toString(response.getEntity()), SearchResult.class);
            for (HashMap hit : (List<HashMap>) searchResult.getData()) {
                if (hit.get(EntitiesSearchField.PARENT.getFieldName()) != null &&
                        hit.get(EntitiesSearchField.PARENT.getFieldName()).equals(Fixtures.LEVEL1_ID)) {
                    totalLevel1Level2Count++;
                }
            }
        }

        // get level1s
        response = this.executeAsAdmin(Request.Post(url + level1GetParameters));
        assertEquals(200, response.getStatusLine().getStatusCode());
        totalLevel1sCount = (int) getHitCount(response);

        for (int i = 0; i < totalLevel2PendingEntitiesCount; i++) {
            createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id);
        }
        for (int i = 0; i < totalLevel2SubmittedEntitiesCount; i++) {
            createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id);
        }
        for (int i = 0; i < totalLevel2PublishedEntitiesCount; i++) {
            createEntity(EntityState.PUBLISHED, FixedContentModel.DATA.getName(), level2Id);
        }
        for (int i = 0; i < totalLevel2WithdrawnEntitiesCount; i++) {
            createEntity(EntityState.WITHDRAWN, FixedContentModel.DATA.getName(), level2Id);
        }

        // get users
        String usersUrl = hostUrl + "search/users";
        response =
                this.executeAsAdmin(Request.Get(usersUrl));
        assertEquals(200, response.getStatusLine().getStatusCode());
        totalUsersCount = (int)getHitCount(response);

    }

    private long getHitCount(HttpResponse response) throws IOException {
        SearchResult searchResult = mapper.readValue(response.getEntity().getContent(), SearchResult.class);
        return searchResult.getTotalHits();
    }

}
