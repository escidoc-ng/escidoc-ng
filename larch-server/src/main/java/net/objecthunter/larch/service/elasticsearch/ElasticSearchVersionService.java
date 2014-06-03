package net.objecthunter.larch.service.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Version;
import net.objecthunter.larch.service.BlobstoreService;
import net.objecthunter.larch.service.VersionService;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Service implementation on top of ElasticSearch
 */
public class ElasticSearchVersionService implements VersionService {
    public static final String INDEX_VERSIONS = "versions";
    public static final String TYPE_VERSIONS = "version";
    private static final Logger log = LoggerFactory.getLogger(ElasticSearchVersionService.class);

    @Autowired
    private BlobstoreService blobstoreService;

    @Autowired
    private Client client;

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    public void init() throws IOException {
        final IndicesExistsResponse existsResp = client.admin().indices().prepareExists(INDEX_VERSIONS)
                .execute()
                .actionGet();
        if (!existsResp.isExists()) {
            log.info("Creating non existant versions index");
            final CreateIndexResponse indexResp = client.admin().indices().prepareCreate(INDEX_VERSIONS)
                    .execute()
                    .actionGet();
        }

    }
    private void refreshIndex(String... indices) {
        client.admin()
                .indices()
                .refresh(new RefreshRequest(indices))
                .actionGet();
    }

    @Override
    public void addOldVersion(Entity e) throws IOException {
        final String path = this.blobstoreService.createOldVersionBlob(e);
        final Version version = new Version();
        version.setEntityId(e.getId());
        version.setVersionNumber(e.getVersion());
        version.setPath(path);
        final IndexResponse resp = this.client.prepareIndex(INDEX_VERSIONS, TYPE_VERSIONS)
                .setSource(this.mapper.writeValueAsBytes(version))
                .execute()
                .actionGet();
        this.refreshIndex(INDEX_VERSIONS);
        log.info("added entity {} version {}", version.getEntityId(), version.getVersionNumber());
    }

    @Override
    public Entity getOldVersion(String id, int versionNumber) throws IOException {
        final SearchResponse resp = client.prepareSearch(INDEX_VERSIONS)
                .setQuery(QueryBuilders.boolQuery()
                    .must(QueryBuilders.matchQuery("entityId", id))
                    .must(QueryBuilders.matchQuery("versionNumber", versionNumber)))
                .setFrom(0)
                .setSize(1)
                .execute()
                .actionGet();
        if (resp.getHits().getTotalHits() == 0) {
            throw new FileNotFoundException("Entity " + id + " does not exists with version " + versionNumber);
        }
        final Version v = this.mapper.readValue(resp.getHits().getAt(0).getSourceAsString(), Version.class);
        return this.mapper.readValue(this.blobstoreService.retrieveOldVersionBlob(v.getPath()), Entity.class);
    }
}