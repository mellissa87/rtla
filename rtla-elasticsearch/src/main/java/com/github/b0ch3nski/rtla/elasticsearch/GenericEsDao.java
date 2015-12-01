package com.github.b0ch3nski.rtla.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author bochen
 */
public abstract class GenericEsDao<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericEsDao.class);
    private final Class<T> classType;
    private final ObjectMapper jsonMapper;
    private final String indexName;
    private final String typeName;
    private final Client client;

    protected GenericEsDao(Settings settings, Class<T> classType, ObjectMapper jsonMapper, String indexName, String typeName) {
        this.classType = classType;
        this.jsonMapper = jsonMapper;
        this.indexName = indexName;
        this.typeName = typeName;
        client = ElasticsearchSession.getInstance(settings).getClient();

        if (!isMappingExists()) createMapping();
    }

    protected abstract XContentBuilder provideMapping() throws IOException;

    private boolean isMappingExists() {
        boolean isExists = client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
        LOGGER.debug("Mapping for {} found = {}", indexName, isExists);
        return isExists;
    }

    private void createMapping() {
        try (XContentBuilder mapping = provideMapping()) {
            LOGGER.debug("Creating mapping for {} = {}", indexName, mapping.string());
            client.admin().indices().prepareCreate(indexName).addMapping(typeName, mapping).execute().actionGet();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't create mapping", e);
        }
    }

    public final String save(T toSave) {
        IndexRequestBuilder requestBuilder = client.prepareIndex(indexName, typeName);
        byte[] json;

        try {
            json = jsonMapper.writeValueAsBytes(toSave);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }

        IndexResponse response = requestBuilder.setSource(json).execute().actionGet();
        LOGGER.trace("Object {} was saved to /{}/{} with ID = {}", toSave, response.getIndex(), response.getType(), response.getId());
        return response.getId();
    }

    public final Optional<T> get(String id) {
        GetResponse response = client.prepareGet(indexName, typeName, id).get();

        try {
            if (response.isExists()) return Optional.of(jsonMapper.readValue(response.getSourceAsBytes(), classType));
            else return Optional.absent();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public final void shutdown() {
        ElasticsearchSession.shutdown();
    }
}
