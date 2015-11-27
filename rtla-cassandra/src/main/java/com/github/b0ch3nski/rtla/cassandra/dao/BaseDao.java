package com.github.b0ch3nski.rtla.cassandra.dao;

import com.datastax.driver.core.*;
import com.datastax.driver.core.BatchStatement.Type;
import com.github.b0ch3nski.rtla.cassandra.*;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.cache.*;
import com.google.common.collect.Lists;
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
    private final Table table;
    private final int batchSize;
    private final Cache<String, List<T>> batchCache;
    private final String insertQuery;

    BaseDao(CassandraConfig config, Table table, long timeToLive) {
        this.config = config;
        this.table = table;
        batchSize = config.getBatchSize();

        batchCache = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getFlushTime(), TimeUnit.SECONDS)
                .removalListener(new BatchCacheFlusher())
                .build();
        insertQuery = table.getInsertQuery(getColumns(), timeToLive);
    }

    private final class BatchCacheFlusher implements RemovalListener<String, List<T>> {
        @Override
        public void onRemoval(RemovalNotification<String, List<T>> notification) {
            if (notification.getCause() != RemovalCause.REPLACED) {
                List<T> toFlush = notification.getValue();

                if ((toFlush != null) && (!toFlush.isEmpty())) {
                    flushBatch(toFlush);
                    LOGGER.debug("Flushed batch cache | Flush cause: {} | All cached buffers: {}",
                            getRemovalCause(notification.getCause()), batchCache.size());
                }
            }
        }

        private String getRemovalCause(RemovalCause cause) {
            return (cause == RemovalCause.EXPLICIT) ? "Buffer size exceeded" : StringUtils.capitalize(cause.toString().toLowerCase());
        }
    }

    public final Table getTable() {
        return table;
    }

    @VisibleForTesting
    final long countAllElements() {
        return executeStatement(getPreparedStatement(table.getCountQuery()).bind()).one().getLong(0);
    }

    @VisibleForTesting
    final void truncateTable() {
        executeStatement(getStatement(table.getTruncateQuery()));
    }

    public final Statement getStatement(String query) {
        return CassandraSession.getInstance(config).getStatement(query);
    }

    public final PreparedStatement getPreparedStatement(String query) {
        return CassandraSession.getInstance(config).getPreparedStatement(query);
    }

    public final ResultSet executeStatement(Statement statement) {
        return CassandraSession.getInstance(config).executeStatement(statement);
    }

    public final ResultSetFuture executeAsyncStatement(Statement statement) {
        return CassandraSession.getInstance(config).executeAsyncStatement(statement);
    }

    private void flushBatch(List<T> toFlush) {
        PreparedStatement insertStatement = getPreparedStatement(insertQuery);
        BatchStatement batch = new BatchStatement(Type.UNLOGGED);
        for (T item : toFlush) {
            batch.add(insertStatement.bind(getValuesToInsert(item)));
        }
        executeAsyncStatement(batch);

        LOGGER.trace("Saved batch of:\n{}", Joiner.on("\n").join(toFlush));
    }

    public final void save(List<T> items) {
        List<T> buffer = batchCache.getIfPresent(CACHE_KEY_NAME);
        if (buffer == null) buffer = new ArrayList<>();

        buffer.addAll(items);
        batchCache.put(CACHE_KEY_NAME, buffer);

        if (buffer.size() >= batchSize) batchCache.invalidate(CACHE_KEY_NAME);
    }

    @SafeVarargs
    public final void save(T... items) {
        save(Lists.newArrayList(items));
    }

    public final void shutdown() {
        CassandraSession.shutdown();
    }

    protected abstract Object[] getValuesToInsert(T item);

    protected abstract String[] getColumns();

    protected abstract List<T> getListFromResultSet(ResultSet result);

    protected abstract T getObjectFromRow(Row single);
}
