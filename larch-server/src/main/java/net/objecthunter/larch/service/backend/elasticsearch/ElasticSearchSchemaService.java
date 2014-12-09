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

package net.objecthunter.larch.service.backend.elasticsearch;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.objecthunter.larch.exceptions.AlreadyExistsException;
import net.objecthunter.larch.exceptions.InvalidParameterException;
import net.objecthunter.larch.exceptions.NotFoundException;
import net.objecthunter.larch.helpers.MetadataTypes;
import net.objecthunter.larch.model.Binary;
import net.objecthunter.larch.model.Entity;
import net.objecthunter.larch.model.Metadata;
import net.objecthunter.larch.model.MetadataType;
import net.objecthunter.larch.model.MetadataValidationResult;
import net.objecthunter.larch.service.backend.BackendBlobstoreService;
import net.objecthunter.larch.service.backend.BackendSchemaService;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.tags.form.InputTag;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticSearchSchemaService extends AbstractElasticSearchService implements BackendSchemaService {

    public static final String INDEX_MD_SCHEMATA = "mdschema";

    public static final String INDEX_MD_SCHEMATA_TYPE = "mdschema-type";

    private static final Logger log = LoggerFactory.getLogger(ElasticSearchSchemaService.class);

    @Autowired
    private BackendBlobstoreService backendBlobstoreService;

    @Autowired
    private ObjectMapper mapper;

    @PostConstruct
    public void init() throws IOException {
        /* check if the metadata type index exists */
        log.debug("initialising ElasticSearchSchemaService");
        this.checkAndOrCreateIndex(INDEX_MD_SCHEMATA);
        this.waitForIndex(INDEX_MD_SCHEMATA);
        this.checkAndOrCreateDefaultMdTypes();
    }

    private void checkAndOrCreateDefaultMdTypes() throws IOException {
        try {
            for (MetadataType type : MetadataTypes.getDefaultMetadataTypes()) {
                this.client.prepareIndex(INDEX_MD_SCHEMATA, INDEX_MD_SCHEMATA_TYPE,
                        type.getName())
                        .setSource(mapper.writeValueAsBytes(type))
                        .execute()
                        .actionGet();
            }
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public String getSchemUrlForType(String type) throws IOException {
        final GetResponse get;
        try {
            get = this.client.prepareGet(INDEX_MD_SCHEMATA, INDEX_MD_SCHEMATA_TYPE, type)
                    .execute()
                    .actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        if (!get.isExists()) {
            throw new NotFoundException("Metadata type '" + type + "' does not exist");
        }
        final MetadataType mdType = mapper.readValue(get.getSourceAsBytes(), MetadataType.class);
        return mdType.getSchemaUrl();
    }

    @Override
    public List<MetadataType> getSchemaTypes() throws IOException {
        final SearchResponse search;
        try {
            search = this.client.prepareSearch(INDEX_MD_SCHEMATA)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setQuery(QueryBuilders.matchAllQuery())
                    .execute()
                    .actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        final List<MetadataType> mdTypes = new ArrayList<>(search.getHits().getHits().length);
        for (SearchHit h : search.getHits()) {
            mdTypes.add(mapper.readValue(h.getSourceAsString(), MetadataType.class));
        }
        return mdTypes;
    }

    @Override
    public String createSchemaType(MetadataType newType) throws IOException {
        /* check if the metadata type already exists */
        final IndexResponse resp;
        try {
            final boolean exists =
                    this.client.prepareGet(INDEX_MD_SCHEMATA, INDEX_MD_SCHEMATA_TYPE, newType.getName())
                            .execute()
                            .actionGet()
                            .isExists();
            if (exists) {
                throw new AlreadyExistsException("Metadata type " + newType.getName() + " already exists");
            }
            /* add the new type to the index */
            resp =
                    this.client.prepareIndex(INDEX_MD_SCHEMATA, INDEX_MD_SCHEMATA_TYPE, newType.getName())
                            .setSource(mapper.writeValueAsBytes(newType))
                            .execute()
                            .actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        this.refreshIndex(INDEX_MD_SCHEMATA);
        return resp.getId();
    }

    @Override
    public void deleteSchemaType(String name) throws IOException {
        /* first check if the type is still used by Entities or Binaries */
        try {
            final CountResponse count = this.client.prepareCount(ElasticSearchEntityService.INDEX_ENTITIES)
                    .setQuery(QueryBuilders.nestedQuery("metadata", QueryBuilders.matchQuery("type", name)))
                    .execute()
                    .actionGet();
            if (count.getCount() > 0) {
                throw new InvalidParameterException("Unable to delete metadata type ' " + name +
                        "' since it's still in use");
            }
            /* the metadata type is safe to delete, since it's no longer used */
            log.debug("deleting meta data type {} ", name);
            this.client.prepareDelete(ElasticSearchSchemaService.INDEX_MD_SCHEMATA,
                    INDEX_MD_SCHEMATA_TYPE, name)
                    .execute()
                    .actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
    }

    @Override
    public MetadataValidationResult validate(String id, String metadataName) throws IOException {
        /* fetch the entity first */
        final GetResponse resp;
        try {
            resp = this.client.prepareGet(ElasticSearchEntityService
                    .INDEX_ENTITIES,
                    ElasticSearchEntityService.INDEX_ENTITY_TYPE, id
                    )
                    .execute()
                    .actionGet();
        } catch (ElasticsearchException ex) {
            throw new IOException(ex.getMostSpecificCause().getMessage());
        }
        if (!resp.isExists()) {
            throw new NotFoundException("The entity '" + id + "' does not exist");
        }
        /* fetch the named metadata which will be validatet */
        final Entity e = mapper.readValue(resp.getSourceAsBytes(), Entity.class);
        if (e.getMetadata() == null || !e.getMetadata().containsKey(metadataName)) {
            throw new IOException("The entity '" + id + "' has no meta data record named '" + metadataName);
        }
        final Metadata md = e.getMetadata().get(metadataName);

        return this.validate(md);
    }

    @Override
    public MetadataValidationResult validate(String id, String binaryName, String metadataName) throws IOException {
        /* fetch the entity first */
        final GetResponse resp = this.client.prepareGet(ElasticSearchEntityService
                .INDEX_ENTITIES,
                ElasticSearchEntityService.INDEX_ENTITY_TYPE, id
                )
                .execute()
                .actionGet();
        if (!resp.isExists()) {
            throw new NotFoundException("The entity '" + id + "' does not exist");
        }
        /* fetch the named metadata which will be validated */
        final Entity e = mapper.readValue(resp.getSourceAsBytes(), Entity.class);
        if (e.getBinaries() == null || !e.getBinaries().containsKey(binaryName)) {
            throw new FileNotFoundException("The binary " + binaryName + " does not exist on the entity " + id);
        }
        final Binary bin = e.getBinaries().get(binaryName);
        if (bin.getMetadata() == null || !bin.getMetadata().containsKey(metadataName)) {
            throw new NotFoundException("The binary " + binaryName + " of the entity '" + id +
                    "' has no meta data record " +
                    "named '" + metadataName);
        }
        final Metadata md = bin.getMetadata().get(metadataName);
        return this.validate(md);
    }

    public MetadataValidationResult validate(Metadata md) throws IOException {
        final String schemaUrl = this.getSchemUrlForType(md.getType());
        /* validate the schema against the given URL */
        final MetadataValidationResult result = new MetadataValidationResult();
        try (final InputStream src = backendBlobstoreService.retrieve(md.getPath())) {
            final URL schemaFile = new URL(schemaUrl);
            final Source xmlFile = new StreamSource(src);
            final SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = schemaFactory.newSchema(schemaFile);
            final Validator validator = schema.newValidator();
            try {
                validator.validate(xmlFile);
                result.setSuccess(true);
                result.setTimestamp(ZonedDateTime.now(ZoneOffset.UTC).toString());
                result.setDetails("Validation successful");
                return result;
            } catch (SAXException validationException) {
                result.setSuccess(false);
                result.setTimestamp(ZonedDateTime.now(ZoneOffset.UTC).toString());
                result.setDetails(validationException.getMessage());
                return result;
            }
        } catch (SAXException se) {
            throw new InvalidParameterException(se);
        }
    }

}
