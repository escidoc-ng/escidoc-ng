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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.objecthunter.larch.integration.helpers.AuthConfigurer.MissingPermission;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.SearchResult;
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

public class AuthorizeSearchControllerIT extends AbstractAuthorizeLarchIT {

    private static final Logger log = LoggerFactory.getLogger(AuthorizeSearchControllerIT.class);

    private static int methodCounter = 0;
    
    private static String entityPrefix = "entity";
    
    private static String archivePrefix = "archive";
    
    private static String countsSuffix = "Counts";
    
    private static int totalLevel2PendingCount = 3;

    private static int totalLevel2SubmittedCount = 4;

    private static int totalLevel2PublishedCount = 2;

    private static int totalLevel2WithdrawnCount = 1;

    private static HashMap<String, Integer> entityCounts = new HashMap<String, Integer>();

    private static HashMap<String, Integer> archiveCounts = new HashMap<String, Integer>();

    private static HashMap<String, HashMap<String, Integer>> allCounts =
            new HashMap<String, HashMap<String, Integer>>() {

                {
                    put(entityPrefix + countsSuffix, entityCounts);
                    put(archivePrefix + countsSuffix, archiveCounts);
                }
            };

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
    public void testSearchEntities() throws Exception {
        search(entityPrefix);
    }

    /**
     * test searching for archives and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testSearchArchives() throws Exception {
        search(archivePrefix);
    }

    /**
     * test searching entities belonging to a level2 and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testSearchEntitiesInLevel2() throws Exception {
        searchInLevel2(entityPrefix);
    }

    /**
     * test searching archives belonging to a level2 and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testSearchArchivesInLevel2() throws Exception {
        searchInLevel2(archivePrefix);
    }

    /**
     * test searching entities being a level2 and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testSearchEntitiesForLevel2s() throws Exception {
        searchForLevel2s(entityPrefix);
    }

    /**
     * test searching archives being a level2 and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testSearchArchivesForLevel2s() throws Exception {
        searchForLevel2s(archivePrefix);
    }

    /**
     * test searching entities being a level1 and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testSearchEntitiesForLevel1s() throws Exception {
        searchForLevel1s(entityPrefix);
    }

    /**
     * test searching archives being a level1 and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testSearchArchivesForLevel1s() throws Exception {
        searchForLevel1s(archivePrefix);
    }

    /**
     * test searching users and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testSearchUsers() throws Exception {
        // user with no rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.GET, userSearchUrl, null,
                        userRoleUsernames.get(MissingPermission.ALL)[0], userRoleUsernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(1, getHitCount(response));

        // user with read level2 rights + level1_admin rights
        response =
                this.executeAsUser(HttpMethod.GET, userSearchUrl, null,
                        userRoleUsernames.get(MissingPermission.READ_PENDING_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalUsersCount, getHitCount(response));

        // level1-admin
        response =
                this.executeAsUser(HttpMethod.GET, userSearchUrl, null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0],
                        level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalUsersCount, getHitCount(response));

        // level1-admin
        response =
                this.executeAsUser(HttpMethod.GET, userSearchUrl, null,
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + level1Id1)[0], level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + level1Id1)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalUsersCount, getHitCount(response));

        // user-admin
        response =
                this.executeAsUser(HttpMethod.GET, userSearchUrl, null,
                        userAdminRoleUsernames.get("ROLE_USER_ADMIN")[0], userAdminRoleUsernames
                                .get("ROLE_USER_ADMIN")[1],
                        false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalUsersCount, getHitCount(response));

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.GET, userSearchUrl, null, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.GET, userSearchUrl, null, adminUsername, adminPassword, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalUsersCount, getHitCount(response));
    }

    private void search(String type) throws Exception {
        String query =
                "(state:PENDING OR state:SUBMITTED OR state:PUBLISHED OR state:WITHDRAWN) AND contentModelId:data";
        String url;
        if (entityPrefix.equals(type)) {
            url = entitySearchUrl + "?query=" + URLEncoder.encode(query, "UTF-8");
        } else if (archivePrefix.equals(type)) {
            url = archiveSearchUrl + "?query=" + URLEncoder.encode(query, "UTF-8");
        } else {
            throw new Exception("Wrong type");
        }

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
        assertEquals(totalLevel2PublishedCount +
                totalLevel2SubmittedCount +
                totalLevel2WithdrawnCount, getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel2PublishedCount +
                totalLevel2PendingCount +
                totalLevel2WithdrawnCount, getHitCount(response));

        // user with no read published metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_PUBLISHED_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_PUBLISHED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel2SubmittedCount +
                totalLevel2PendingCount +
                totalLevel2WithdrawnCount, getHitCount(response));

        // user with no read withdrawn metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_WITHDRAWN_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_WITHDRAWN_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel2PublishedCount +
                totalLevel2PendingCount +
                totalLevel2SubmittedCount, getHitCount(response));

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
                assertEquals(totalLevel2PublishedCount +
                        totalLevel2SubmittedCount +
                        totalLevel2PendingCount +
                        totalLevel2WithdrawnCount,
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
        assertEquals(totalLevel2PublishedCount +
                totalLevel2SubmittedCount +
                totalLevel2PendingCount +
                totalLevel2WithdrawnCount +
                allCounts.get(type + countsSuffix).get("totalPublishedCount") +
                allCounts.get(type + countsSuffix).get("totalSubmittedCount") +
                allCounts.get(type + countsSuffix).get("totalPendingCount") +
                allCounts.get(type + countsSuffix).get("totalWithdrawnCount"), getHitCount(response));
    }

    private void searchInLevel2(String type) throws Exception {
        String query =
                "(state:PENDING OR state:SUBMITTED OR state:PUBLISHED OR state:WITHDRAWN) AND contentModelId:data AND level2:" +
                        level2Id;
        String url;
        if (entityPrefix.equals(type)) {
            url = entitySearchUrl + "?query=" + URLEncoder.encode(query, "UTF-8");
        } else if (archivePrefix.equals(type)) {
            url = archiveSearchUrl + "?query=" + URLEncoder.encode(query, "UTF-8");
        } else {
            throw new Exception("Wrong type");
        }
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
        assertEquals(totalLevel2PublishedCount + totalLevel2SubmittedCount +
                totalLevel2WithdrawnCount,
                getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel2PublishedCount + totalLevel2PendingCount +
                totalLevel2WithdrawnCount, getHitCount(response));

        // user with no read published metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_PUBLISHED_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_PUBLISHED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel2SubmittedCount + totalLevel2PendingCount +
                totalLevel2WithdrawnCount, getHitCount(response));

        // user with no read withdrawn metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_WITHDRAWN_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_WITHDRAWN_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalLevel2PublishedCount + totalLevel2PendingCount +
                totalLevel2SubmittedCount, getHitCount(response));

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
                assertEquals(totalLevel2PublishedCount + totalLevel2PendingCount +
                        totalLevel2SubmittedCount + totalLevel2WithdrawnCount,
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
        assertEquals(totalLevel2PublishedCount + totalLevel2PendingCount +
                totalLevel2SubmittedCount + totalLevel2WithdrawnCount, getHitCount(response));
    }

    private void searchForLevel2s(String type) throws Exception {
        String query = "contentModelId:level2";
        String url;
        if (entityPrefix.equals(type)) {
            url = entitySearchUrl + "?query=" + URLEncoder.encode(query, "UTF-8");
        } else if (archivePrefix.equals(type)) {
            url = archiveSearchUrl + "?query=" + URLEncoder.encode(query, "UTF-8");
        } else {
            throw new Exception("Wrong type");
        }
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
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0],
                        level1AdminRoleUsernames
                                .get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(allCounts.get(type + countsSuffix).get("totalLevel1Level2Count") + 1, getHitCount(response));

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
                        userAdminRoleUsernames.get("ROLE_USER_ADMIN")[0], userAdminRoleUsernames
                                .get("ROLE_USER_ADMIN")[1],
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
        assertEquals(allCounts.get(type + countsSuffix).get("totalLevel2sCount").intValue(), getHitCount(response));
    }

    private void searchForLevel1s(String type) throws Exception {
        String query = "contentModelId:level1";
        String url;
        if (entityPrefix.equals(type)) {
            url = entitySearchUrl + "?query=" + URLEncoder.encode(query, "UTF-8");
        } else if (archivePrefix.equals(type)) {
            url = archiveSearchUrl + "?query=" + URLEncoder.encode(query, "UTF-8");
        } else {
            throw new Exception("Wrong type");
        }
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
                        level1AdminRoleUsernames.get("ROLE_LEVEL1_ADMIN" + Fixtures.LEVEL1_ID)[0],
                        level1AdminRoleUsernames
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
                        userAdminRoleUsernames.get("ROLE_USER_ADMIN")[0], userAdminRoleUsernames
                                .get("ROLE_USER_ADMIN")[1],
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
        assertEquals(allCounts.get(type + countsSuffix).get("totalLevel1sCount").intValue(), getHitCount(response));
    }

    /**
     * Get all already indexed entities. Create entities with different status to test search. Save counts.
     */
    private void prepareSearch() throws Exception {
        prepareSearch(entityPrefix);
        prepareSearch(archivePrefix);
        for (int i = 0; i < totalLevel2PendingCount; i++) {
            Entity e = createEntity(EntityState.PENDING, FixedContentModel.DATA.getName(), level2Id, false);
            createArchive(e.getId(), e.getVersion());
        }
        for (int i = 0; i < totalLevel2SubmittedCount; i++) {
            Entity e = createEntity(EntityState.SUBMITTED, FixedContentModel.DATA.getName(), level2Id, false);
            createArchive(e.getId(), e.getVersion());
        }
        for (int i = 0; i < totalLevel2PublishedCount; i++) {
            Entity e = createEntity(EntityState.PUBLISHED, FixedContentModel.DATA.getName(), level2Id, false);
            createArchive(e.getId(), e.getVersion());
        }
        for (int i = 0; i < totalLevel2WithdrawnCount; i++) {
            Entity e = createEntity(EntityState.WITHDRAWN, FixedContentModel.DATA.getName(), level2Id, false);
            createArchive(e.getId(), e.getVersion());
        }

        // get users
        HttpResponse response =
                this.executeAsAdmin(Request.Get(userSearchUrl));
        assertEquals(200, response.getStatusLine().getStatusCode());
        totalUsersCount = (int) getHitCount(response);

    }
    
    private void prepareSearch(String type) throws Exception {
        // get total hits
        String dataGetParameters = "?query=contentModelId:data&maxRecords=50";
        String level2GetParameters = "?query=contentModelId:level2&maxRecords=50";
        String level1level2GetParameters = "?query=contentModelId:level2+AND+level1:" + Fixtures.LEVEL1_ID + "&maxRecords=50";
        String level1GetParameters = "?query=contentModelId:level1&maxRecords=50";
        String url = null;
        if (entityPrefix.equals(type)) {
            url = entitySearchUrl;
        } else if (archivePrefix.equals(type)) {
            url = archiveSearchUrl;
        } else {
            throw new Exception("Wrong type");
        }

        // get datas
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
        int totalPendingCount, totalSubmittedCount, totalPublishedCount, totalWithdrawnCount;
        totalPendingCount = totalSubmittedCount = totalPublishedCount = totalWithdrawnCount = 0;
        for (HashMap entity : entities) {
            if (EntityState.PENDING.name().equals(entity.get("state"))) {
                totalPendingCount++;
            } else if (EntityState.SUBMITTED.name().equals(entity.get("state"))) {
                totalSubmittedCount++;
            } else if (EntityState.PUBLISHED.name().equals(entity.get("state"))) {
                totalPublishedCount++;
            } else if (EntityState.WITHDRAWN.name().equals(entity.get("state"))) {
                totalWithdrawnCount++;
            }
        }
        allCounts.get(type + countsSuffix).put("totalPendingCount", totalPendingCount);
        allCounts.get(type + countsSuffix).put("totalSubmittedCount", totalSubmittedCount);
        allCounts.get(type + countsSuffix).put("totalPublishedCount", totalPublishedCount);
        allCounts.get(type + countsSuffix).put("totalWithdrawnCount", totalWithdrawnCount);

        // get level2s
        response = this.executeAsAdmin(Request.Get(url + level2GetParameters));
        assertEquals(200, response.getStatusLine().getStatusCode());
        allCounts.get(type + countsSuffix).put("totalLevel2sCount", (int) getHitCount(response));
        response = this.executeAsAdmin(Request.Get(url + level1level2GetParameters));
        assertEquals(200, response.getStatusLine().getStatusCode());
        allCounts.get(type + countsSuffix).put("totalLevel1Level2Count", (int) getHitCount(response));

        // get level1s
        response = this.executeAsAdmin(Request.Get(url + level1GetParameters));
        assertEquals(200, response.getStatusLine().getStatusCode());
        allCounts.get(type + countsSuffix).put("totalLevel1sCount", (int) getHitCount(response));

    }

    private long getHitCount(HttpResponse response) throws IOException {
        SearchResult searchResult = mapper.readValue(response.getEntity().getContent(), SearchResult.class);
        return searchResult.getTotalHits();
    }

}
