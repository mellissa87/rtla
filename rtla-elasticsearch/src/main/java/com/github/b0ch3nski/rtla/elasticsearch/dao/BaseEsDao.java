package com.github.b0ch3nski.rtla.elasticsearch.dao;

import com.github.b0ch3nski.rtla.common.serialization.SerializationHandler;
import com.github.b0ch3nski.rtla.elasticsearch.ElasticsearchSession;
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
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
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
public abstract class BaseEsDao<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseEsDao.class);
    private final Class<T> classType;
    private final String indexName;
    private final String typeName;
    private final Client client;
    private final BulkProcessor bulkProcessor;
    private XContentBuilder mapping;
    protected final Settings settings;

    protected BaseEsDao(Settings settings, Class<T> classType, String indexName, String typeName) {
        this.settings = settings;
        this.classType = classType;
        this.indexName = indexName;
        this.typeName = typeName;

        client = ElasticsearchSession.getInstance(settings).getClient();
        bulkProcessor = new BulkHandler().getBulkProcessor();
        ElasticsearchSession.getInstance(settings).createIndex(indexName, typeName, getMapping());
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

        private BulkProcessor getBulkProcessor() {
            return BulkProcessor.builder(client, this)
                    .setBulkActions(settings.getAsInt("bulk.actions", 10))
                    .setBulkSize(ByteSizeValue.parseBytesSizeValue(settings.get("bulk.size"), ""))
                    .setFlushInterval(TimeValue.parseTimeValue(settings.get("flush.time"), TimeValue.timeValueMinutes(1L), ""))
                    .build();
        }
    }

    protected abstract XContentBuilder provideMapping() throws IOException;

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
    protected final void deleteIndex() {
        ElasticsearchSession.getInstance(settings).deleteIndex(indexName);
    }

    @VisibleForTesting
    protected final void flushBulk() {
        LOGGER.info("Force flush called on bulk processor");
        bulkProcessor.flush();
    }

    public final void save(List<T> toSave) {
        toSave.forEach(item -> {
            byte[] json = SerializationHandler.getJsonFromObject(item);
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
            return Optional.of(SerializationHandler.getObjectFromJson(json, classType));
        } else return Optional.absent();
    }

    private SearchHits getSearchHits(QueryBuilder query, int size) {
        SearchResponse response = client.prepareSearch(indexName).setTypes(typeName).setQuery(query).setSize(size).execute().actionGet();
        SearchHits hits = response.getHits();
        LOGGER.trace("Search took {} milliseconds and found {} hits | Query:\n{}", response.getTookInMillis(), hits.getTotalHits(), query);
        return hits;
    }

    private T getObjectFromSearchHit(SearchHit hit) {
        return SerializationHandler.getObjectFromJson(hit.getSourceAsString().getBytes(), classType);
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
