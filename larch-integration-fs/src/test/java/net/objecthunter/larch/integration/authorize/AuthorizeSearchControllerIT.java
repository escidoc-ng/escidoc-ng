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

    private static int totalPendingEntitiesCount = 0;

    private static int totalSubmittedEntitiesCount = 0;

    private static int totalPublishedEntitiesCount = 0;

    private static int totalWorkspacePendingEntitiesCount = 3;

    private static int totalWorkspaceSubmittedEntitiesCount = 4;

    private static int totalWorkspacePublishedEntitiesCount = 2;

    @Before
    public void initialize() throws Exception {
        super.initialize();
        if (methodCounter == 0) {
            prepareSearch();
            methodCounter++;
        }
    }

    /**
     * test retrieving list of all entities and check hit-count.
     * 
     * @throws Exception
     */
    //@Test
    public void testList() throws Exception {
        String url = hostUrl + "list";
        // user with no workspace rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.ALL)[0], usernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedEntitiesCount + totalWorkspacePublishedEntitiesCount, getHitCount(response));

        // user with no read pending metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_PENDING_METADATA)[0], usernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedEntitiesCount + totalWorkspacePublishedEntitiesCount +
                totalWorkspaceSubmittedEntitiesCount, getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], usernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedEntitiesCount + totalWorkspacePublishedEntitiesCount +
                totalWorkspacePendingEntitiesCount, getHitCount(response));

        // user with all read metadata rights
        for (Entry<MissingPermission, String[]> entry : usernames.entrySet()) {
            if (!MissingPermission.READ_SUBMITTED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PENDING_METADATA.equals(entry.getKey())
                    && !MissingPermission.ALL.equals(entry.getKey())) {
                response =
                        this.executeAsUser(HttpMethod.GET, url, null,
                                entry.getValue()[0], entry.getValue()[1], false);
                assertEquals(response.getStatusLine().getStatusCode(), 200);
                assertEquals(totalPublishedEntitiesCount + totalWorkspacePublishedEntitiesCount +
                        totalWorkspacePendingEntitiesCount +
                        totalWorkspaceSubmittedEntitiesCount, getHitCount(response));
            }
        }

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.GET, url, null, false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedEntitiesCount + totalWorkspacePublishedEntitiesCount, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null, adminUsername, adminPassword, false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedEntitiesCount + totalWorkspacePublishedEntitiesCount + totalPendingEntitiesCount +
                totalSubmittedEntitiesCount +
                totalWorkspacePendingEntitiesCount + totalWorkspaceSubmittedEntitiesCount, getHitCount(response));
    }

    /**
     * test retrieving published list of all entities and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testPublishedList() throws Exception {
        HttpResponse response = this.executeAsAdmin(Request.Get(hostUrl + "list/published/0/0"));
        long totalPublishedHits = getHitCount(response);
        String url = hostUrl + "/list/published";
        // user with no workspace rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.ALL)[0], usernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedHits, getHitCount(response));

        // user with no read pending metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_PENDING_METADATA)[0], usernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedHits, getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], usernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedHits, getHitCount(response));

        // user with all read metadata rights
        for (Entry<MissingPermission, String[]> entry : usernames.entrySet()) {
            if (!MissingPermission.READ_SUBMITTED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PENDING_METADATA.equals(entry.getKey())
                    && !MissingPermission.ALL.equals(entry.getKey())) {
                response =
                        this.executeAsUser(HttpMethod.GET, url, null,
                                entry.getValue()[0], entry.getValue()[1], false);
                assertEquals(response.getStatusLine().getStatusCode(), 200);
                assertEquals(totalPublishedHits, getHitCount(response));
            }
        }

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.GET, url, null, false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedHits, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null, adminUsername, adminPassword, false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedHits, getHitCount(response));
    }

    /**
     * test retrieving list of all entities belonging to a workspace and check hit-count.
     * 
     * @throws Exception
     */
    //@Test
    public void testListForWorkspace() throws Exception {
        String url = hostUrl + "workspace/" + permissionId + "/list";
        // user with no workspace rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.ALL)[0], usernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalWorkspacePublishedEntitiesCount, getHitCount(response));

        // user with no read pending metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_PENDING_METADATA)[0], usernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalWorkspacePublishedEntitiesCount + totalWorkspaceSubmittedEntitiesCount,
                getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], usernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalWorkspacePublishedEntitiesCount + totalWorkspacePendingEntitiesCount, getHitCount(response));

        // user with all read metadata rights
        for (Entry<MissingPermission, String[]> entry : usernames.entrySet()) {
            if (!MissingPermission.READ_SUBMITTED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PENDING_METADATA.equals(entry.getKey())
                    && !MissingPermission.ALL.equals(entry.getKey())) {
                response =
                        this.executeAsUser(HttpMethod.GET, url, null,
                                entry.getValue()[0], entry.getValue()[1], false);
                assertEquals(response.getStatusLine().getStatusCode(), 200);
                assertEquals(totalWorkspacePublishedEntitiesCount + totalWorkspacePendingEntitiesCount +
                        totalWorkspaceSubmittedEntitiesCount, getHitCount(response));
            }
        }

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.GET, url, null, false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalWorkspacePublishedEntitiesCount, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null, adminUsername, adminPassword, false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalWorkspacePublishedEntitiesCount + totalWorkspacePendingEntitiesCount +
                totalWorkspaceSubmittedEntitiesCount, getHitCount(response));
    }

    /**
     * test retrieving published list of all entities belonging to a workspace and check hit-count.
     * 
     * @throws Exception
     */
    //@Test
    public void testPublishedListForWorkspace() throws Exception {
        HttpResponse response =
                this.executeAsAdmin(Request.Get(hostUrl + "workspace/" + permissionId + "/list/published/0/0"));
        long totalPublishedWorkspaceHits = getHitCount(response);
        String url = hostUrl + "workspace/" + permissionId + "/list/published";
        // user with no workspace rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.ALL)[0], usernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedWorkspaceHits, getHitCount(response));

        // user with no read pending metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_PENDING_METADATA)[0], usernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedWorkspaceHits, getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        usernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], usernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedWorkspaceHits, getHitCount(response));

        // user with all read metadata rights
        for (Entry<MissingPermission, String[]> entry : usernames.entrySet()) {
            if (!MissingPermission.READ_SUBMITTED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PENDING_METADATA.equals(entry.getKey())
                    && !MissingPermission.ALL.equals(entry.getKey())) {
                response =
                        this.executeAsUser(HttpMethod.GET, url, null,
                                entry.getValue()[0], entry.getValue()[1], false);
                assertEquals(response.getStatusLine().getStatusCode(), 200);
                assertEquals(totalPublishedWorkspaceHits, getHitCount(response));
            }
        }

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.GET, url, null, false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedWorkspaceHits, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null, adminUsername, adminPassword, false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedWorkspaceHits, getHitCount(response));
    }

    /**
     * test searching for entities and check hit-count.
     * 
     * @throws Exception
     */
    //@Test
    public void testSearch() throws Exception {
        String postParameters = "state=pending&state=submitted&state=published";
        String url = hostUrl + "search";

        // user with no workspace rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.ALL)[0], usernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedEntitiesCount + totalWorkspacePublishedEntitiesCount, getHitCount(response));

        // user with no read pending metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.READ_PENDING_METADATA)[0], usernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedEntitiesCount + totalWorkspacePublishedEntitiesCount +
                totalWorkspaceSubmittedEntitiesCount, getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], usernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedEntitiesCount + totalWorkspacePublishedEntitiesCount +
                totalWorkspacePendingEntitiesCount, getHitCount(response));

        // user with all read metadata rights
        for (Entry<MissingPermission, String[]> entry : usernames.entrySet()) {
            if (!MissingPermission.READ_SUBMITTED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PENDING_METADATA.equals(entry.getKey())
                    && !MissingPermission.ALL.equals(entry.getKey())) {
                response =
                        this.executeAsUser(HttpMethod.POST, url, postParameters,
                                entry.getValue()[0], entry.getValue()[1], false);
                assertEquals(response.getStatusLine().getStatusCode(), 200);
                assertEquals(totalPublishedEntitiesCount + totalWorkspacePublishedEntitiesCount +
                        totalWorkspacePendingEntitiesCount +
                        totalWorkspaceSubmittedEntitiesCount, getHitCount(response));
            }
        }

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.POST, url, postParameters, false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedEntitiesCount + totalWorkspacePublishedEntitiesCount, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters, adminUsername, adminPassword, false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedEntitiesCount + totalWorkspacePublishedEntitiesCount + totalPendingEntitiesCount +
                totalSubmittedEntitiesCount +
                totalWorkspacePendingEntitiesCount + totalWorkspaceSubmittedEntitiesCount, getHitCount(response));
    }

    /**
     * test searching for published entities and check hit-count.
     * 
     * @throws Exception
     */
    //@Test
    public void testSearchPublished() throws Exception {
        HttpResponse response = this.executeAsAdmin(Request.Get(hostUrl + "list/published/0/0"));
        long totalPublishedHits = getHitCount(response);
        String postParameters = "state=ingested&state=submitted&state=published";
        String url = hostUrl + "search/published";

        // user with no workspace rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.ALL)[0], usernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedHits, getHitCount(response));

        // user with no read pending metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.READ_PENDING_METADATA)[0], usernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedHits, getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], usernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedHits, getHitCount(response));

        // user with all read metadata rights
        for (Entry<MissingPermission, String[]> entry : usernames.entrySet()) {
            if (!MissingPermission.READ_SUBMITTED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PENDING_METADATA.equals(entry.getKey())
                    && !MissingPermission.ALL.equals(entry.getKey())) {
                response =
                        this.executeAsUser(HttpMethod.POST, url, postParameters,
                                entry.getValue()[0], entry.getValue()[1], false);
                assertEquals(response.getStatusLine().getStatusCode(), 200);
                assertEquals(totalPublishedHits, getHitCount(response));
            }
        }

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.POST, url, postParameters, false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedHits, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters, adminUsername, adminPassword, false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedHits, getHitCount(response));
    }

    /**
     * test searching entities belonging to a workspace and check hit-count.
     * 
     * @throws Exception
     */
    //@Test
    public void testSearchForWorkspace() throws Exception {
        String postParameters = "state=pending&state=submitted&state=published&workspace=" + permissionId;
        String url = hostUrl + "search";
        // user with no workspace rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.ALL)[0], usernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalWorkspacePublishedEntitiesCount, getHitCount(response));

        // user with no read pending metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.READ_PENDING_METADATA)[0], usernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalWorkspacePublishedEntitiesCount + totalWorkspaceSubmittedEntitiesCount,
                getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], usernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalWorkspacePublishedEntitiesCount + totalWorkspacePendingEntitiesCount, getHitCount(response));

        // user with all read metadata rights
        for (Entry<MissingPermission, String[]> entry : usernames.entrySet()) {
            if (!MissingPermission.READ_SUBMITTED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PENDING_METADATA.equals(entry.getKey())
                    && !MissingPermission.ALL.equals(entry.getKey())) {
                response =
                        this.executeAsUser(HttpMethod.POST, url, postParameters,
                                entry.getValue()[0], entry.getValue()[1], false);
                assertEquals(response.getStatusLine().getStatusCode(), 200);
                assertEquals(totalWorkspacePublishedEntitiesCount + totalWorkspacePendingEntitiesCount +
                        totalWorkspaceSubmittedEntitiesCount, getHitCount(response));
            }
        }

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.POST, url, postParameters, false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalWorkspacePublishedEntitiesCount, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters, adminUsername, adminPassword, false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalWorkspacePublishedEntitiesCount + totalWorkspacePendingEntitiesCount +
                totalWorkspaceSubmittedEntitiesCount, getHitCount(response));
    }

    /**
     * test retrieving published list of all entities belonging to a workspace and check hit-count.
     * 
     * @throws Exception
     */
    //@Test
    public void testSearchPublishedForWorkspace() throws Exception {
        HttpResponse response =
                this.executeAsAdmin(Request.Get(hostUrl + "workspace/" + permissionId + "/list/published/0/0"));
        long totalPublishedWorkspaceHits = getHitCount(response);
        String postParameters = "state=ingested&state=submitted&state=published&workspace=" + permissionId;
        String url = hostUrl + "search/published";
        // user with no workspace rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.ALL)[0], usernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedWorkspaceHits, getHitCount(response));

        // user with no read pending metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.READ_PENDING_METADATA)[0], usernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedWorkspaceHits, getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        usernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], usernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedWorkspaceHits, getHitCount(response));

        // user with all read metadata rights
        for (Entry<MissingPermission, String[]> entry : usernames.entrySet()) {
            if (!MissingPermission.READ_SUBMITTED_METADATA.equals(entry.getKey())
                    && !MissingPermission.READ_PENDING_METADATA.equals(entry.getKey())
                    && !MissingPermission.ALL.equals(entry.getKey())) {
                response =
                        this.executeAsUser(HttpMethod.POST, url, postParameters,
                                entry.getValue()[0], entry.getValue()[1], false);
                assertEquals(response.getStatusLine().getStatusCode(), 200);
                assertEquals(totalPublishedWorkspaceHits, getHitCount(response));
            }
        }

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.POST, url, postParameters, false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedWorkspaceHits, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters, adminUsername, adminPassword, false);
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        assertEquals(totalPublishedWorkspaceHits, getHitCount(response));
    }

    /**
     * Get all already indexed entities. Create entities with different status to test search. Save counts.
     */
    private void prepareSearch() throws Exception {
        // get total hits
        HttpResponse response = this.executeAsAdmin(Request.Get(hostUrl + "list/0/0"));
        assertEquals(response.getStatusLine().getStatusCode(), 200);
        long totalHits = getHitCount(response);

        int counter = 0;
        List<HashMap> entities = new ArrayList<HashMap>();
        while (counter <= totalHits) {
            response = this.executeAsAdmin(Request.Get(hostUrl + "list/" + counter + "/50"));
            counter += 50;
            assertEquals(response.getStatusLine().getStatusCode(), 200);
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
            }
        }
        for (int i = 0; i < totalWorkspacePendingEntitiesCount; i++) {
            createEntity(EntityState.PENDING, permissionId);
        }
        for (int i = 0; i < totalWorkspaceSubmittedEntitiesCount; i++) {
            createEntity(EntityState.SUBMITTED, permissionId);
        }
        for (int i = 0; i < totalWorkspacePublishedEntitiesCount; i++) {
            createEntity(EntityState.PUBLISHED, permissionId);
        }
    }

    private long getHitCount(HttpResponse response) throws IOException {
        SearchResult searchResult = mapper.readValue(response.getEntity().getContent(), SearchResult.class);
        return searchResult.getTotalHits();
    }

}
