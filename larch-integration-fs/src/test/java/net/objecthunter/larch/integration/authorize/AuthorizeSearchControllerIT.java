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

package net.objecthunter.larch.integration.authorize;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.objecthunter.larch.integration.helpers.AuthConfigurer.MissingPermission;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.Entity.EntityType;
import net.objecthunter.larch.model.SearchResult;

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

    private static int totalAreasCount = 0;

    private static int totalPermissionsCount = 0;

    private static int totalPendingEntitiesCount = 0;

    private static int totalSubmittedEntitiesCount = 0;

    private static int totalPublishedEntitiesCount = 0;

    private static int totalWithdrawnEntitiesCount = 0;

    private static int totalPermissionPendingEntitiesCount = 3;

    private static int totalPermissionSubmittedEntitiesCount = 4;

    private static int totalPermissionPublishedEntitiesCount = 2;

    private static int totalPermissionWithdrawnEntitiesCount = 1;

    @Before
    public void initialize() throws Exception {
        if (methodCounter == 0) {
            preparePermission();
            prepareSearch();
            methodCounter++;
        }
    }

    /**
     * test retrieving list of all data-entities and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testDataList() throws Exception {
        String url = hostUrl + "list/data";
        // user with no workspace rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.ALL)[0], usernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // user with no read pending metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_PENDING_METADATA)[0], usernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount + totalPermissionWithdrawnEntitiesCount + 
                totalPermissionSubmittedEntitiesCount, getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], usernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount + totalPermissionWithdrawnEntitiesCount +
                totalPermissionPendingEntitiesCount, getHitCount(response));

        // user with no read published metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_PUBLISHED_METADATA)[0], usernames
                                .get(MissingPermission.READ_PUBLISHED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionSubmittedEntitiesCount + totalPermissionWithdrawnEntitiesCount +
                totalPermissionPendingEntitiesCount, getHitCount(response));

        // user with no read withdrawn metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_WITHDRAWN_METADATA)[0], usernames
                                .get(MissingPermission.READ_WITHDRAWN_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionSubmittedEntitiesCount + totalPermissionPublishedEntitiesCount +
                totalPermissionPendingEntitiesCount, getHitCount(response));

        // user with all read metadata rights
        for (Entry<MissingPermission, String[]> entry : usernames.entrySet()) {
            if (!MissingPermission.READ_SUBMITTED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PENDING_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PUBLISHED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_WITHDRAWN_METADATA.equals(entry.getKey())
                    && !MissingPermission.ALL.equals(entry.getKey())) {
                response =
                        this.executeAsUser(HttpMethod.GET, url, null,
                                entry.getValue()[0], entry.getValue()[1], false);
                assertEquals(200, response.getStatusLine().getStatusCode());
                assertEquals(totalPermissionPublishedEntitiesCount +
                        totalPermissionPendingEntitiesCount +
                        totalPermissionWithdrawnEntitiesCount +
                        totalPermissionSubmittedEntitiesCount, getHitCount(response));
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
        assertEquals(totalPublishedEntitiesCount + totalPermissionPublishedEntitiesCount + totalPendingEntitiesCount +
                totalSubmittedEntitiesCount +
                totalPermissionPendingEntitiesCount + totalPermissionSubmittedEntitiesCount + 
                totalWithdrawnEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));
    }

    /**
     * test retrieving list of all data-entities and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testPermissionList() throws Exception {
        String url = hostUrl + "list/permission";
        // user with no workspace rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.ALL)[0], usernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // user with no read pending metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_PENDING_METADATA)[0], usernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount + totalPermissionWithdrawnEntitiesCount + 
                totalPermissionSubmittedEntitiesCount, getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], usernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount + totalPermissionWithdrawnEntitiesCount +
                totalPermissionPendingEntitiesCount, getHitCount(response));

        // user with no read published metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_PUBLISHED_METADATA)[0], usernames
                                .get(MissingPermission.READ_PUBLISHED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionSubmittedEntitiesCount + totalPermissionWithdrawnEntitiesCount +
                totalPermissionPendingEntitiesCount, getHitCount(response));

        // user with no read withdrawn metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_WITHDRAWN_METADATA)[0], usernames
                                .get(MissingPermission.READ_WITHDRAWN_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionSubmittedEntitiesCount + totalPermissionPublishedEntitiesCount +
                totalPermissionPendingEntitiesCount, getHitCount(response));

        // user with all read metadata rights
        for (Entry<MissingPermission, String[]> entry : usernames.entrySet()) {
            if (!MissingPermission.READ_SUBMITTED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PENDING_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PUBLISHED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_WITHDRAWN_METADATA.equals(entry.getKey())
                    && !MissingPermission.ALL.equals(entry.getKey())) {
                response =
                        this.executeAsUser(HttpMethod.GET, url, null,
                                entry.getValue()[0], entry.getValue()[1], false);
                assertEquals(200, response.getStatusLine().getStatusCode());
                assertEquals(totalPermissionPublishedEntitiesCount +
                        totalPermissionPendingEntitiesCount +
                        totalPermissionWithdrawnEntitiesCount +
                        totalPermissionSubmittedEntitiesCount, getHitCount(response));
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
        assertEquals(totalPublishedEntitiesCount + totalPermissionPublishedEntitiesCount + totalPendingEntitiesCount +
                totalSubmittedEntitiesCount +
                totalPermissionPendingEntitiesCount + totalPermissionSubmittedEntitiesCount + 
                totalWithdrawnEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));
    }

    /**
     * test retrieving list of all entities belonging to a workspace and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testDataListForWorkspace() throws Exception {
        String url = hostUrl + permissionId + "/children/data/list";
        // user with no workspace rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.ALL)[0], usernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // user with no read pending metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_PENDING_METADATA)[0], usernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount + totalPermissionSubmittedEntitiesCount + totalPermissionWithdrawnEntitiesCount,
                getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], usernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount + totalPermissionPendingEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));

        // user with no read published metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_PUBLISHED_METADATA)[0], usernames
                                .get(MissingPermission.READ_PUBLISHED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionSubmittedEntitiesCount + totalPermissionPendingEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));

        // user with no read withdrawn metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_WITHDRAWN_METADATA)[0], usernames
                                .get(MissingPermission.READ_WITHDRAWN_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount + totalPermissionPendingEntitiesCount + totalPermissionSubmittedEntitiesCount, getHitCount(response));

        // user with all read metadata rights
        for (Entry<MissingPermission, String[]> entry : usernames.entrySet()) {
            if (!MissingPermission.READ_SUBMITTED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PENDING_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PUBLISHED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_WITHDRAWN_METADATA.equals(entry.getKey())
                    && !MissingPermission.ALL.equals(entry.getKey())) {
                response =
                        this.executeAsUser(HttpMethod.GET, url, null,
                                entry.getValue()[0], entry.getValue()[1], false);
                assertEquals(200, response.getStatusLine().getStatusCode());
                assertEquals(totalPermissionPublishedEntitiesCount + totalPermissionPendingEntitiesCount +
                        totalPermissionSubmittedEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));
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
        assertEquals(totalPermissionPublishedEntitiesCount + totalPermissionPendingEntitiesCount +
                totalPermissionSubmittedEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));
    }

    /**
     * test searching for entities and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testSearch() throws Exception {
        String postParameters = "state=PENDING&state=SUBMITTED&state=PUBLISHED&state=WITHDRAWN&type=DATA";
        String url = hostUrl + "search";

        // user with no workspace rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.ALL)[0], usernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // user with no read pending metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.READ_PENDING_METADATA)[0], usernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount +
                totalPermissionSubmittedEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], usernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount +
                totalPermissionPendingEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));

        // user with no read published metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.READ_PUBLISHED_METADATA)[0], usernames
                                .get(MissingPermission.READ_PUBLISHED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionSubmittedEntitiesCount +
                totalPermissionPendingEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));

        // user with no read withdrawn metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.READ_WITHDRAWN_METADATA)[0], usernames
                                .get(MissingPermission.READ_WITHDRAWN_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount +
                totalPermissionPendingEntitiesCount + totalPermissionSubmittedEntitiesCount, getHitCount(response));

        // user with all read metadata rights
        for (Entry<MissingPermission, String[]> entry : usernames.entrySet()) {
            if (!MissingPermission.READ_SUBMITTED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PENDING_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PUBLISHED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_WITHDRAWN_METADATA.equals(entry.getKey())
                    && !MissingPermission.ALL.equals(entry.getKey())) {
                response =
                        this.executeAsUser(HttpMethod.POST, url, postParameters,
                                entry.getValue()[0], entry.getValue()[1], false);
                assertEquals(200, response.getStatusLine().getStatusCode());
                assertEquals(totalPermissionPublishedEntitiesCount +
                        totalPermissionPendingEntitiesCount +
                        totalPermissionSubmittedEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));
            }
        }

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.POST, url, postParameters, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters, adminUsername, adminPassword, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPublishedEntitiesCount + totalPermissionPublishedEntitiesCount + totalPendingEntitiesCount +
                totalSubmittedEntitiesCount +
                totalPermissionPendingEntitiesCount + totalPermissionSubmittedEntitiesCount + 
                totalWithdrawnEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));
    }

    /**
     * test searching entities belonging to a workspace and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testSearchForWorkspace() throws Exception {
        String postParameters = "state=PENDING&state=SUBMITTED&state=PUBLISHED&state=WITHDRAWN&type=DATA&permissionId=" + permissionId;
        String url = hostUrl + "search";
        // user with no workspace rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.ALL)[0], usernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // user with no read pending metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.READ_PENDING_METADATA)[0], usernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount + totalPermissionSubmittedEntitiesCount + totalPermissionWithdrawnEntitiesCount,
                getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], usernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount + totalPermissionPendingEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));

        // user with no read published metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.READ_PUBLISHED_METADATA)[0], usernames
                                .get(MissingPermission.READ_PUBLISHED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionSubmittedEntitiesCount + totalPermissionPendingEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));

        // user with no read withdrawn metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.READ_WITHDRAWN_METADATA)[0], usernames
                                .get(MissingPermission.READ_WITHDRAWN_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount + totalPermissionPendingEntitiesCount + totalPermissionSubmittedEntitiesCount, getHitCount(response));

        // user with all read metadata rights
        for (Entry<MissingPermission, String[]> entry : usernames.entrySet()) {
            if (!MissingPermission.READ_SUBMITTED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PENDING_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PUBLISHED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_WITHDRAWN_METADATA.equals(entry.getKey())
                    && !MissingPermission.ALL.equals(entry.getKey())) {
                response =
                        this.executeAsUser(HttpMethod.POST, url, postParameters,
                                entry.getValue()[0], entry.getValue()[1], false);
                assertEquals(200, response.getStatusLine().getStatusCode());
                assertEquals(totalPermissionPublishedEntitiesCount + totalPermissionPendingEntitiesCount +
                        totalPermissionSubmittedEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));
            }
        }

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.POST, url, postParameters, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters, adminUsername, adminPassword, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount + totalPermissionPendingEntitiesCount +
                totalPermissionSubmittedEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));
    }

    /**
     * Get all already indexed entities. Create entities with different status to test search. Save counts.
     */
    private void prepareSearch() throws Exception {
        // get total hits
        HttpResponse response = this.executeAsAdmin(Request.Get(hostUrl + "list/data/0/0"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        long totalHits = getHitCount(response);

        int counter = 0;
        List<HashMap> entities = new ArrayList<HashMap>();
        while (counter <= totalHits) {
            response = this.executeAsAdmin(Request.Get(hostUrl + "list/data/" + counter + "/50"));
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
        response = this.executeAsAdmin(Request.Get(hostUrl + "list/permission/" + counter + "/50"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        totalPermissionsCount = (int)getHitCount(response);

        // get areas
        response = this.executeAsAdmin(Request.Get(hostUrl + "list/area/" + counter + "/50"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        totalAreasCount = (int)getHitCount(response);

        for (int i = 0; i < totalPermissionPendingEntitiesCount; i++) {
            createEntity(EntityState.PENDING, EntityType.DATA, permissionId);
        }
        for (int i = 0; i < totalPermissionSubmittedEntitiesCount; i++) {
            createEntity(EntityState.SUBMITTED, EntityType.DATA, permissionId);
        }
        for (int i = 0; i < totalPermissionPublishedEntitiesCount; i++) {
            createEntity(EntityState.PUBLISHED, EntityType.DATA, permissionId);
        }
        for (int i = 0; i < totalPermissionWithdrawnEntitiesCount; i++) {
            createEntity(EntityState.WITHDRAWN, EntityType.DATA, permissionId);
        }
    }

    private long getHitCount(HttpResponse response) throws IOException {
        SearchResult searchResult = mapper.readValue(response.getEntity().getContent(), SearchResult.class);
        return searchResult.getTotalHits();
    }

}
