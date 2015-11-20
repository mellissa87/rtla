package com.github.b0ch3nski.rtla.cassandra.dao;

import com.datastax.driver.core.*;
import com.datastax.driver.core.BatchStatement.Type;
import com.github.b0ch3nski.rtla.cassandra.*;
import com.google.common.annotations.VisibleForTesting;
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
    private final Table table;
    private final int batchSize;
    private final Cache<String, List<T>> batchCache;
    private final PreparedStatement insertStatement;

    public BaseDao(CassandraConfig config, Table table, long timeToLive) {
        this.config = config;
        this.table = table;
        batchSize = config.getBatchSize();
        insertStatement = getPreparedStatement(table.getInsertQuery(getColumns(), timeToLive));

        batchCache = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getFlushTime(), TimeUnit.SECONDS)
                .removalListener(new BatchCacheFlusher())
                .build();
    }

    private final class BatchCacheFlusher implements RemovalListener<String, List<T>> {
        @Override
        public void onRemoval(RemovalNotification<String, List<T>> notification) {
            if (notification.getCause() != RemovalCause.REPLACED) {
                flushBatch((notification.getValue() != null) ? notification.getValue() : new ArrayList<>());

                LOGGER.debug("Flushed batch cache | Flush cause: {} | All cached buffers: {}",
                        getRemovalCause(notification.getCause()), batchCache.size());
            }
        }

        private String getRemovalCause(RemovalCause cause) {
            return (cause == RemovalCause.EXPLICIT) ? "Buffer size exceeded" : StringUtils.capitalize(cause.toString().toLowerCase());
        }
    }

    public Table getTable() {
        return table;
    }

    public long countAllElements() {
        return executeStatement(getPreparedStatement(table.getCountQuery()).bind()).one().getLong(0);
    }

    @VisibleForTesting
    void truncateTable() {
        executeStatement(getStatement(table.getTruncateQuery()));
    }

    public Statement getStatement(String query) {
        return CassandraSession.getInstance(config).getStatement(query);
    }

    public PreparedStatement getPreparedStatement(String query) {
        return CassandraSession.getInstance(config).getPreparedStatement(query);
    }

    public ResultSet executeStatement(Statement statement) {
        return CassandraSession.getInstance(config).executeStatement(statement);
    }

    public ResultSetFuture executeAsyncStatement(Statement statement) {
        return CassandraSession.getInstance(config).executeAsyncStatement(statement);
    }

    private void flushBatch(List<T> toFlush) {
        BatchStatement statement = new BatchStatement(Type.UNLOGGED);
        for (T item : toFlush) {
            statement.add(insertStatement.bind(getValuesToInsert(item)));
        }
        executeAsyncStatement(statement);

        LOGGER.trace("Saved batch of:\n{}", Joiner.on("\n").join(toFlush));
    }

    public void save(T item) {
        List<T> buffer = batchCache.getIfPresent(CACHE_KEY_NAME);
        if (buffer == null) buffer = new ArrayList<>();

        buffer.add(item);
        batchCache.put(CACHE_KEY_NAME, buffer);

        if (buffer.size() >= batchSize) batchCache.invalidate(CACHE_KEY_NAME);
    }

    public void shutdown() {
        CassandraSession.shutdown();
    }

    protected abstract Object[] getValuesToInsert(T item);

    protected abstract String[] getColumns();

    protected abstract List<T> getListFromResultSet(ResultSet result);

    protected abstract T getObjectFromRow(Row single);
}
