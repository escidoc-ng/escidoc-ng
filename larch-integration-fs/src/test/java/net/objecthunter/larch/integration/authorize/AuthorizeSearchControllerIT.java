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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.objecthunter.larch.integration.helpers.AuthConfigurer.MissingPermission;
import net.objecthunter.larch.model.Entity.EntityState;
import net.objecthunter.larch.model.Entity.EntityType;
import net.objecthunter.larch.model.SearchResult;
import net.objecthunter.larch.model.security.User;
import net.objecthunter.larch.service.backend.elasticsearch.ElasticSearchEntityService.EntitiesSearchField;
import net.objecthunter.larch.test.util.Fixtures;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
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

    private static int totalAreasCount = 0;

    private static int totalAreaPermissionCount = 0;

    private static int totalPermissionsCount = 0;

    private static int totalPendingEntitiesCount = 0;

    private static int totalSubmittedEntitiesCount = 0;

    private static int totalPublishedEntitiesCount = 0;

    private static int totalWithdrawnEntitiesCount = 0;

    private static int totalPermissionPendingEntitiesCount = 3;

    private static int totalPermissionSubmittedEntitiesCount = 4;

    private static int totalPermissionPublishedEntitiesCount = 2;

    private static int totalPermissionWithdrawnEntitiesCount = 1;

    private static int totalUsersCount = 0;

    @Before
    public void initialize() throws Exception {
        if (methodCounter == 0) {
            preparePermission();
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
        String postParameters = "state=PENDING&state=SUBMITTED&state=PUBLISHED&state=WITHDRAWN&type=DATA";
        String url = hostUrl + "search";

        // user with no workspace rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        userRoleUsernames.get(MissingPermission.ALL)[0], userRoleUsernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // user with no read pending metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        userRoleUsernames.get(MissingPermission.READ_PENDING_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount +
                totalPermissionSubmittedEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        userRoleUsernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount +
                totalPermissionPendingEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));

        // user with no read published metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        userRoleUsernames.get(MissingPermission.READ_PUBLISHED_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_PUBLISHED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionSubmittedEntitiesCount +
                totalPermissionPendingEntitiesCount + totalPermissionWithdrawnEntitiesCount, getHitCount(response));

        // user with no read withdrawn metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        userRoleUsernames.get(MissingPermission.READ_WITHDRAWN_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_WITHDRAWN_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount +
                totalPermissionPendingEntitiesCount + totalPermissionSubmittedEntitiesCount, getHitCount(response));

        // user with all read metadata rights
        for (Entry<MissingPermission, String[]> entry : userRoleUsernames.entrySet()) {
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
                        totalPermissionSubmittedEntitiesCount + totalPermissionWithdrawnEntitiesCount,
                        getHitCount(response));
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
    public void testSearchInPermission() throws Exception {
        String postParameters =
                "state=PENDING&state=SUBMITTED&state=PUBLISHED&state=WITHDRAWN&type=DATA&permissionId=" +
                        permissionId;
        String url = hostUrl + "search";
        // user with no workspace rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        userRoleUsernames.get(MissingPermission.ALL)[0], userRoleUsernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // user with no read pending metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        userRoleUsernames.get(MissingPermission.READ_PENDING_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount + totalPermissionSubmittedEntitiesCount +
                totalPermissionWithdrawnEntitiesCount,
                getHitCount(response));

        // user with no read submitted metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        userRoleUsernames.get(MissingPermission.READ_SUBMITTED_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_SUBMITTED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount + totalPermissionPendingEntitiesCount +
                totalPermissionWithdrawnEntitiesCount, getHitCount(response));

        // user with no read published metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        userRoleUsernames.get(MissingPermission.READ_PUBLISHED_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_PUBLISHED_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionSubmittedEntitiesCount + totalPermissionPendingEntitiesCount +
                totalPermissionWithdrawnEntitiesCount, getHitCount(response));

        // user with no read withdrawn metadata rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        userRoleUsernames.get(MissingPermission.READ_WITHDRAWN_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_WITHDRAWN_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionPublishedEntitiesCount + totalPermissionPendingEntitiesCount +
                totalPermissionSubmittedEntitiesCount, getHitCount(response));

        // user with all read metadata rights
        for (Entry<MissingPermission, String[]> entry : userRoleUsernames.entrySet()) {
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
                        totalPermissionSubmittedEntitiesCount + totalPermissionWithdrawnEntitiesCount,
                        getHitCount(response));
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
     * test searching entities being a permission and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testSearchForPermissions() throws Exception {
        String postParameters = "type=PERMISSION";
        String url = hostUrl + "search";
        // user with no workspace rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        userRoleUsernames.get(MissingPermission.ALL)[0], userRoleUsernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // user with read workspace rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        userRoleUsernames.get(MissingPermission.READ_PENDING_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(1, getHitCount(response));

        // area-admin
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        areaAdminRoleUsernames.get("AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalAreaPermissionCount + 1, getHitCount(response));

        // area-admin
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        areaAdminRoleUsernames.get("AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("AREA_ADMIN" + areaId1)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(1, getHitCount(response));

        // user-admin
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        userAdminRoleUsernames.get("USER_ADMIN")[0], userAdminRoleUsernames.get("USER_ADMIN")[1],
                        false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(1, getHitCount(response));

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.POST, url, postParameters, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters, adminUsername, adminPassword, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalPermissionsCount, getHitCount(response));
    }

    /**
     * test searching entities being a permission and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testSearchForAreas() throws Exception {
        String postParameters = "type=AREA";
        String url = hostUrl + "search";
        // user with no workspace rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        userRoleUsernames.get(MissingPermission.ALL)[0], userRoleUsernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // user with read workspace rights
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        userRoleUsernames.get(MissingPermission.READ_PENDING_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(1, getHitCount(response));

        // area-admin
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        areaAdminRoleUsernames.get("AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(1, getHitCount(response));

        // area-admin
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        areaAdminRoleUsernames.get("AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("AREA_ADMIN" + areaId1)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(1, getHitCount(response));

        // user-admin
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters,
                        userAdminRoleUsernames.get("USER_ADMIN")[0], userAdminRoleUsernames.get("USER_ADMIN")[1],
                        false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(1, getHitCount(response));

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.POST, url, postParameters, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, getHitCount(response));

        // admin
        response =
                this.executeAsUser(HttpMethod.POST, url, postParameters, adminUsername, adminPassword, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(totalAreasCount, getHitCount(response));
    }

    /**
     * test lising users and check hit-count.
     * 
     * @throws Exception
     */
    @Test
    public void testListUsers() throws Exception {
        String url = hostUrl + "user";
        // user with no rights
        HttpResponse response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.ALL)[0], userRoleUsernames
                                .get(MissingPermission.ALL)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        List<User> users = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<User>>() {});
        assertNotNull(users);
        assertEquals(1, users.size());

        // user with read workspace rights + area_admin rights
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userRoleUsernames.get(MissingPermission.READ_PENDING_METADATA)[0], userRoleUsernames
                                .get(MissingPermission.READ_PENDING_METADATA)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        users = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<User>>() {});
        assertNotNull(users);
        assertEquals(totalUsersCount, users.size());

        // area-admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        areaAdminRoleUsernames.get("AREA_ADMIN" + Fixtures.AREA_ID)[0], areaAdminRoleUsernames
                                .get("AREA_ADMIN" + Fixtures.AREA_ID)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        users = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<User>>() {});
        assertNotNull(users);
        assertEquals(totalUsersCount, users.size());

        // area-admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        areaAdminRoleUsernames.get("AREA_ADMIN" + areaId1)[0], areaAdminRoleUsernames
                                .get("AREA_ADMIN" + areaId1)[1], false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        users = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<User>>() {});
        assertNotNull(users);
        assertEquals(totalUsersCount, users.size());

        // user-admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null,
                        userAdminRoleUsernames.get("USER_ADMIN")[0], userAdminRoleUsernames.get("USER_ADMIN")[1],
                        false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        users = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<User>>() {});
        assertNotNull(users);
        assertEquals(totalUsersCount, users.size());

        // anonymous
        response =
                this.executeAsAnonymous(HttpMethod.GET, url, null, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        users = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<User>>() {});
        assertNotNull(users);
        assertEquals(0, users.size());

        // admin
        response =
                this.executeAsUser(HttpMethod.GET, url, null, adminUsername, adminPassword, false);
        assertEquals(200, response.getStatusLine().getStatusCode());
        users = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<User>>() {});
        assertNotNull(users);
        assertEquals(totalUsersCount, users.size());
    }

    /**
     * Get all already indexed entities. Create entities with different status to test search. Save counts.
     */
    private void prepareSearch() throws Exception {
        // get total hits
        String dataPostParameters = "type=DATA&maxRecords=50";
        String permissionPostParameters = "type=PERMISSION&maxRecords=50";
        String areaPostParameters = "type=AREA&maxRecords=50";
        String url = hostUrl + "search";
        HttpResponse response =
                this.executeAsAdmin(Request.Post(url).bodyString(dataPostParameters,
                        ContentType.APPLICATION_FORM_URLENCODED));
        assertEquals(200, response.getStatusLine().getStatusCode());
        long totalHits = getHitCount(response);

        int counter = 0;
        List<HashMap> entities = new ArrayList<HashMap>();
        while (counter <= totalHits) {
            response = this.executeAsAdmin(Request.Post(url).bodyString(dataPostParameters + "&offset=" + counter,
                    ContentType.APPLICATION_FORM_URLENCODED));
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
        response = this.executeAsAdmin(Request.Post(url).bodyString(permissionPostParameters,
                ContentType.APPLICATION_FORM_URLENCODED));
        assertEquals(200, response.getStatusLine().getStatusCode());
        totalPermissionsCount = (int) getHitCount(response);
        counter = 0;
        while (counter <= totalPermissionsCount) {
            response = this.executeAsAdmin(Request.Post(url).bodyString(permissionPostParameters + "&offset=" + counter,
                    ContentType.APPLICATION_FORM_URLENCODED));
            counter += 50;
            assertEquals(200, response.getStatusLine().getStatusCode());
            SearchResult searchResult =
                    mapper.readValue(EntityUtils.toString(response.getEntity()), SearchResult.class);
            for (HashMap hit : (List<HashMap>) searchResult.getData()) {
                if (hit.get(EntitiesSearchField.PARENT.getFieldName()) != null &&
                        hit.get(EntitiesSearchField.PARENT.getFieldName()).equals(Fixtures.AREA_ID)) {
                    totalAreaPermissionCount++;
                }
            }
        }

        // get areas
        response = this.executeAsAdmin(Request.Post(url).bodyString(areaPostParameters,
                ContentType.APPLICATION_FORM_URLENCODED));
        assertEquals(200, response.getStatusLine().getStatusCode());
        totalAreasCount = (int) getHitCount(response);

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

        // get users
        response = this.executeAsAdmin(Request.Get(hostUrl + "user"));
        assertEquals(200, response.getStatusLine().getStatusCode());
        List<User> users = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<User>>() {});

        if (users != null) {
            totalUsersCount = users.size();
        }

    }

    private long getHitCount(HttpResponse response) throws IOException {
        SearchResult searchResult = mapper.readValue(response.getEntity().getContent(), SearchResult.class);
        return searchResult.getTotalHits();
    }

}
