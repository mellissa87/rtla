package com.github.b0ch3nski.rtla.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.bulk.BulkProcessor.Listener;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.*;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    private final BulkProcessor bulkProcessor;
    private XContentBuilder mapping;

    protected GenericEsDao(Settings settings, Class<T> classType, ObjectMapper jsonMapper, String indexName, String typeName) {
        this.classType = classType;
        this.jsonMapper = jsonMapper;
        this.indexName = indexName;
        this.typeName = typeName;

        client = ElasticsearchSession.getInstance(settings).getClient();
        bulkProcessor = new BulkHandler().getBulkProcessor();
        createIndex();
    }

    private class BulkHandler implements Listener {
        @Override
        public void beforeBulk(long executionId, BulkRequest request) {
            LOGGER.trace("Going to execute bulk of {} actions", request.numberOfActions());
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
            LOGGER.debug("Executed bulk of {} actions | Execution took {} milliseconds", request.numberOfActions(), response.getTookInMillis());
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
            LOGGER.warn("Bulk execution failed", failure);
        }

        // TODO: make those settings configurable somehow
        private BulkProcessor getBulkProcessor() {
            return BulkProcessor.builder(client, this)
                    .setBulkActions(10)
                    .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.MB))
                    .setFlushInterval(TimeValue.timeValueSeconds(15))
                    .setConcurrentRequests(1)
                    .build();
        }
    }

    protected abstract XContentBuilder provideMapping() throws IOException;

    private boolean isIndexExists() {
        boolean isExists = client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
        LOGGER.info("Index {} found = {}", indexName, isExists);
        return isExists;
    }

    private XContentBuilder getMapping() {
        if (mapping == null) try {
            mapping = provideMapping();
            if (mapping == null) throw new IllegalStateException("Got null mapping from child class");
            LOGGER.info("Got mapping for {} index = {}", indexName, mapping.string());
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't create mapping", e);
        }
        return mapping;
    }

    @VisibleForTesting
    protected final void createIndex() {
        if (isIndexExists()) return;
        LOGGER.info("Creating {} index", indexName);
        client.admin().indices().prepareCreate(indexName).addMapping(typeName, getMapping()).execute().actionGet();
    }

    @VisibleForTesting
    protected final void deleteIndex() {
        if (!isIndexExists()) return;
        LOGGER.info("Index removal called on {} index", indexName);
        client.admin().indices().prepareDelete(indexName).execute().actionGet();
    }

    @VisibleForTesting
    protected final void refreshIndex() {
        LOGGER.info("Force refresh called on {} index", indexName);
        client.admin().indices().prepareRefresh(indexName).execute().actionGet();
        /*
        client.admin().indices().prepareClearCache(indexName).execute().actionGet();
        client.admin().indices().prepareFlush(indexName).execute().actionGet();
        */
    }

    @VisibleForTesting
    protected final void refreshBulk() {
        LOGGER.info("Force flush called on bulk processor");
        bulkProcessor.flush();
    }

    private byte[] getJsonFromObject(T toJson) {
        try {
            return jsonMapper.writeValueAsBytes(toJson);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Couldn't serialize object of type " + classType + " to JSON", e);
        }
    }

    private T getObjectFromJson(byte[] json) {
        try {
            return jsonMapper.readValue(json, classType);
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't deserialize JSON to object of type " + classType, e);
        }
    }

    public final void save(List<T> toSave) {
        toSave.forEach(item -> {
            byte[] json = getJsonFromObject(item);
            bulkProcessor.add(new IndexRequest(indexName, typeName).source(json));
        });
    }

    @SafeVarargs
    public final void save(T... toSave) {
        save(Lists.newArrayList(toSave));
    }

    public final Optional<T> get(String id) {
        GetResponse response = client.prepareGet(indexName, typeName, id).get();

        if (response.isExists()) {
            byte[] json = response.getSourceAsBytes();
            return Optional.of(getObjectFromJson(json));
        } else return Optional.absent();
    }

    private SearchHits getSearchHits(QueryBuilder query, int size) {
        SearchResponse response = client.prepareSearch(indexName).setTypes(typeName).setQuery(query).setSize(size).execute().actionGet();
        SearchHits hits = response.getHits();
        LOGGER.trace("Search took {} milliseconds and found {} hits | Query:\n{}", response.getTookInMillis(), hits.getTotalHits(), query);
        return hits;
    }

    private T getObjectFromSearchHit(SearchHit hit) {
        return getObjectFromJson(hit.getSourceAsString().getBytes());
    }

    public final List<T> search(QueryBuilder query, int size) {
        SearchHits hits = getSearchHits(query, size);
        return StreamSupport.stream(hits.spliterator(), false)
                .map(this::getObjectFromSearchHit)
                .collect(Collectors.toList());
    }

    public final List<String> searchIds(QueryBuilder query, int size) {
        SearchHits hits = getSearchHits(query, size);
        return StreamSupport.stream(hits.spliterator(), false)
                .map(SearchHit::getId)
                .collect(Collectors.toList());
    }

    public final void shutdown() {
        ElasticsearchSession.shutdown();
    }
}
