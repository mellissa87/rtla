package com.github.b0ch3nski.rtla.cassandra.dao;

import com.datastax.driver.core.*;
import com.datastax.driver.core.BatchStatement.Type;
import com.github.b0ch3nski.rtla.cassandra.*;
import com.github.b0ch3nski.rtla.cassandra.CassandraSession.SessionHandler;
import com.github.b0ch3nski.rtla.common.model.InsertableToCass;
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
public abstract class BaseCassDao<T extends InsertableToCass> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCassDao.class);
    private final CassandraConfig config;
    private final CassandraTable table;
    private final int batchSize;
    private final Cache<String, List<T>> batchCache;
    private final String insertQuery;
    private SessionHandler session;
    private String[] columns;

    protected BaseCassDao(CassandraConfig config, CassandraTable table) {
        this.config = config;
        this.table = table;
        batchSize = config.getBatchSize();

        batchCache = CacheBuilder.newBuilder()
                .expireAfterAccess(config.getFlushTime(), TimeUnit.SECONDS)
                .removalListener(new BatchCacheFlusher())
                .build();
        insertQuery = table.getInsertQuery(getColumns(), config.getTtl());
    }

    private final class BatchCacheFlusher implements RemovalListener<String, List<T>> {
        @Override
        public void onRemoval(RemovalNotification<String, List<T>> notification) {
            if (notification.getCause() != RemovalCause.REPLACED) {
                List<T> toFlush = notification.getValue();

                if ((toFlush != null) && (!toFlush.isEmpty())) {
                    flushBatch(toFlush);
                    LOGGER.debug("Flushed batch of key: {} | Flush cause: {} | All cached buffers: {}",
                            notification.getKey(), getRemovalCause(notification.getCause()), batchCache.size());
                }
            }
        }

        private String getRemovalCause(RemovalCause cause) {
            return (cause == RemovalCause.EXPLICIT) ? "Buffer size exceeded" : StringUtils.capitalize(cause.toString().toLowerCase());
        }
    }

    private SessionHandler getSession() {
        if (session == null) session = CassandraSession.getInstance(config).getSessionHandler();
        return session;
    }

    public final CassandraTable getTable() {
        return table;
    }

    private String[] getColumns() {
        if (columns == null) {
            columns = provideColumns();
            if (columns == null) throw new IllegalStateException("Couldn't initialize columns; got null from child class");
        }
        return columns;
    }

    @VisibleForTesting
    protected final long countAllElements() {
        return executeStatement(getPreparedStatement(table.getCountQuery()).bind()).one().getLong(0);
    }

    @VisibleForTesting
    protected final void truncateTable() {
        executeStatement(new SimpleStatement(table.getTruncateQuery()));
    }

    public final PreparedStatement getPreparedStatement(String query) {
        return getSession().getPreparedStatement(query);
    }

    public final ResultSet executeStatement(Statement statement) {
        return getSession().executeStatement(statement);
    }

    public final ResultSetFuture executeAsyncStatement(Statement statement) {
        return getSession().executeAsyncStatement(statement);
    }

    private void flushBatch(List<T> toFlush) {
        PreparedStatement insertStatement = getPreparedStatement(insertQuery);
        BatchStatement batch = new BatchStatement(Type.UNLOGGED);

        toFlush.forEach(item -> batch.add(insertStatement.bind(getValuesToInsert(item))));
        executeAsyncStatement(batch);

        LOGGER.trace("Saved batch of:\n{}", Joiner.on("\n").join(toFlush));
    }

    public final void save(T item) {
        String partitionKey = item.getPartitionKey();

        List<T> buffer = batchCache.getIfPresent(partitionKey);
        if (buffer == null) {
            buffer = new ArrayList<>();
            LOGGER.trace("No buffer for key {} found in cache, created new one", partitionKey);
        } else {
            LOGGER.trace("Found buffer for key {} in cache | Buffer size: {}", partitionKey, buffer.size());
        }

        buffer.add(item);
        batchCache.put(partitionKey, buffer);

        if (buffer.size() >= batchSize) batchCache.invalidate(partitionKey);
    }

    public final void save(List<T> items) {
        items.forEach(this::save);
    }

    @SafeVarargs
    public final void save(T... items) {
        save(Lists.newArrayList(items));
    }

    protected abstract Object[] getValuesToInsert(T item);

    protected abstract String[] provideColumns();

    protected abstract List<T> getListFromResultSet(ResultSet result);

    protected abstract T getObjectFromRow(Row single);
}
