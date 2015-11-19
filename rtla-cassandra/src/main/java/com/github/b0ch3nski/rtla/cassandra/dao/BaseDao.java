package com.github.b0ch3nski.rtla.cassandra.dao;

import com.datastax.driver.core.*;
import com.datastax.driver.core.BatchStatement.Type;
import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;
import com.github.b0ch3nski.rtla.cassandra.CassandraSession;
import com.google.common.base.Joiner;
import com.google.common.cache.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author bochen
 */
public abstract class BaseDao<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseDao.class);
    private static final String CACHE_KEY_NAME = "cache";
    private final CassandraConfig config;
    private final String columnWithKeyspace;
    private final int timeToLive;
    private final int batchSize;
    private final Cache<String, List<T>> batchCache;

    public BaseDao(CassandraConfig config, String keyspaceName, String columnName, int timeToLive) {
        this.config = config;
        columnWithKeyspace = keyspaceName + "." + columnName;
        this.timeToLive = timeToLive;
        batchSize = config.getBatchSize();

        batchCache = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getFlushTime(), TimeUnit.SECONDS)
                .removalListener(new BatchCacheFlusher())
                .build();
    }

    private final class BatchCacheFlusher implements RemovalListener<String, List<T>> {
        @Override
        public void onRemoval(RemovalNotification<String, List<T>> notification) {
            if (notification.getCause() != RemovalCause.REPLACED) {
                BatchStatement statement = new BatchStatement(Type.UNLOGGED);
                List<T> toFlush = (notification.getValue() != null) ? notification.getValue() : new ArrayList<>();
                flushBatch(statement, toFlush);

                LOGGER.debug("Flushed batch cache | Flush cause: {} | All cached buffers: {}",
                        getRemovalCause(notification.getCause()), batchCache.size());
            }
        }

        private String getRemovalCause(RemovalCause cause) {
            return (cause == RemovalCause.EXPLICIT) ? "Buffer size exceeded" : StringUtils.capitalize(cause.toString().toLowerCase());
        }
    }

    public String getColumnWithKeyspace() {
        return columnWithKeyspace;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public long count() {
        return executeStatement(getPreparedStatement("SELECT COUNT(*) FROM " + columnWithKeyspace + ";").bind()).one().getLong(0);
    }

    public PreparedStatement getPreparedStatement(String query) {
        return CassandraSession.getInstance(config).getPreparedStatement(query);
    }

    public ResultSet executeStatement(Statement statement) {
        return CassandraSession.getInstance(config).executeStatement(statement);
    }

    private void flushBatch(BatchStatement statement, List<T> toFlush) {
        for (T item : toFlush) {
            addToBatch(statement, item);
        }
        CassandraSession.getInstance(config).executeAsyncStatement(statement);

        if (LOGGER.isTraceEnabled()) {
            String type = toFlush.isEmpty() ? "" : toFlush.get(0).getClass().getName();
            String joinedList = Joiner.on(", ").join(toFlush);
            LOGGER.trace("[{}] saved: {}", type, joinedList);
        }
    }

    public void batchSave(T item) {
        List<T> buffer = batchCache.getIfPresent(CACHE_KEY_NAME);
        if (buffer == null) buffer = new ArrayList<>();

        buffer.add(item);
        batchCache.put(CACHE_KEY_NAME, buffer);

        if (buffer.size() >= batchSize) {
            batchCache.invalidate(CACHE_KEY_NAME);
            LOGGER.debug("Max size reached, invalidating batch cache");
        }
    }

    protected abstract void addToBatch(BatchStatement batch, T item);

    public void shutdown() {
        CassandraSession.shutdown();
    }
}
