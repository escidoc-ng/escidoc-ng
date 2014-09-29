
package net.objecthunter.larch.service.backend.elasticsearch;

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.model.ContentModel;
import net.objecthunter.larch.model.ContentModel.FixedContentModel;
import net.objecthunter.larch.service.backend.BackendContentModelService;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.get.GetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service implementation on top of ElasticSearch
 */
public class ElasticSearchContentModelService extends AbstractElasticSearchService implements
        BackendContentModelService {

    public static final String INDEX_CONTENT_MODELS = "content-models";

    public static final String TYPE_CONTENT_MODELS = "content-model";

    public static final String ENTITY_ID_FIELD = "entityId";

    private static final Logger log = LoggerFactory.getLogger(ElasticSearchContentModelService.class);

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    public void init() throws IOException {
        log.debug("initialising ElasticSearchContentModelService");
        this.checkAndOrCreateIndex(INDEX_CONTENT_MODELS);
        this.waitForIndex(INDEX_CONTENT_MODELS);
        checkAndOrCreateDefaultContentModels();
    }

    private void checkAndOrCreateDefaultContentModels() throws IOException {
        long count = client.prepareCount(INDEX_CONTENT_MODELS).execute().actionGet().getCount();
        if (count == 0) {
            try {
                final ContentModel level1 = new ContentModel();
                level1.setId(FixedContentModel.LEVEL1.getName());
                level1.setName(FixedContentModel.LEVEL1.getName());
                client
                        .prepareIndex(INDEX_CONTENT_MODELS, TYPE_CONTENT_MODELS, level1.getId())
                        .setSource(mapper.writeValueAsBytes(level1))
                        .execute().actionGet();

                final ContentModel level2 = new ContentModel();
                level2.setId(FixedContentModel.LEVEL2.getName());
                level2.setName(FixedContentModel.LEVEL2.getName());
                level2.setAllowedParentContentModels(Arrays.asList(new String[] { FixedContentModel.LEVEL1.getName() }));
                client
                        .prepareIndex(INDEX_CONTENT_MODELS, TYPE_CONTENT_MODELS, level2.getId())
                        .setSource(mapper.writeValueAsBytes(level2))
                        .execute().actionGet();

                final ContentModel data = new ContentModel();
                data.setId(FixedContentModel.DATA.getName());
                data.setName(FixedContentModel.DATA.getName());
                data.setAllowedParentContentModels(Arrays.asList(new String[] { FixedContentModel.LEVEL2.getName(), FixedContentModel.DATA.getName() }));
                client
                        .prepareIndex(INDEX_CONTENT_MODELS, TYPE_CONTENT_MODELS, data.getId())
                        .setSource(mapper.writeValueAsBytes(data))
                        .execute().actionGet();
            } catch (ElasticsearchException ex) {
                throw new IOException(ex.getMostSpecificCause().getMessage());
            }
        }
    }

    @Override
    public ContentModel retrieve(String contentModelId) throws IOException {
        log.debug("fetching ContentModel " + contentModelId);
        final GetResponse resp;
        try {
            resp = client.prepareGet(INDEX_CONTENT_MODELS, TYPE_CONTENT_MODELS, contentModelId).execute().actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        if (resp.isSourceEmpty()) {
            throw new NotFoundException("ContentModel with id " + contentModelId + " not found");
        }
        return mapper.readValue(resp.getSourceAsBytes(), ContentModel.class);
    }

}
