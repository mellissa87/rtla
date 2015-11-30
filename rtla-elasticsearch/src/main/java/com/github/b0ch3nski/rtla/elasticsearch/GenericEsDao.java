package com.github.b0ch3nski.rtla.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author bochen
 */
public final class GenericEsDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericEsDao.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Client client;

    public GenericEsDao(Settings settings) {
        client = ElasticsearchSession.getInstance(settings).getClient();
    }

    public <T> void save(T toSave, String index, String type) throws JsonProcessingException {
        IndexRequestBuilder requestBuilder = client.prepareIndex(index, type);
        byte[] json = MAPPER.writeValueAsBytes(toSave);
        IndexResponse response = requestBuilder.setSource(json).execute().actionGet();

        LOGGER.trace("Object {} was saved to /{}/{} with ID={}", toSave, response.getIndex(), response.getType(), response.getId());
    }

    @VisibleForTesting
    Class get(String index, String type, String id, Class cls) throws IOException {
        GetResponse response = client.prepareGet(index, type, id).get();
        return MAPPER.readValue(response.getSourceAsBytes(), cls.getClass());
    }

    public void shutdown() {
        ElasticsearchSession.shutdown();
    }
}
