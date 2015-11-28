package com.github.b0ch3nski.rtla.cassandra;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.TraceRetrievalException;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * @author bochen
 */
public final class CassandraSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraSession.class);
    private static final boolean TRACE_QUERY = LOGGER.isDebugEnabled();
    private static CassandraSession instance = null;
    private final Map<String, PreparedStatement> statementCache = new ConcurrentHashMap<>();
    private Cluster cluster = null;
    private Session session = null;

    private CassandraSession(CassandraConfig config) {
        cluster = Cluster.builder()
                .addContactPoint(config.getHost())
                .withPort(config.getPort())
                .build();
        session = cluster.connect();

        LOGGER.debug("New Cassandra session was created with config = {}", config);
    }

    public static synchronized CassandraSession getInstance(CassandraConfig config) {
        if (instance == null) instance = new CassandraSession(config);
        return instance;
    }

    public PreparedStatement getPreparedStatement(String query) {
        PreparedStatement statement = statementCache.get(query);
        if (statement == null) {
            LOGGER.debug("Statement [{}] was not found in cache - preparing it now | Statements in cache = {}", query, statementCache.size() + 1);
            statement = session.prepare(query);
            statementCache.put(query, statement);
        }
        return statement;
    }

    public Statement getStatement(String query) {
        return session.newSimpleStatement(query);
    }

    public ResultSet executeStatement(Statement statement) {
        if (TRACE_QUERY) statement.enableTracing();
        return withQueryTraceInfo(session.execute(statement));
    }

    public ResultSetFuture executeAsyncStatement(Statement statement) {
        if (TRACE_QUERY) statement.enableTracing();
        return withCallback(session.executeAsync(statement));
    }

    private ResultSet withQueryTraceInfo(ResultSet result) {
        if (TRACE_QUERY) try {
            QueryTrace trace = result.getExecutionInfo().getQueryTrace();
            LOGGER.debug("Request type: {} | Coordinator used: {} | Execution took: {} microseconds",
                    trace.getRequestType(), trace.getCoordinator().getCanonicalHostName(), trace.getDurationMicros());
        } catch (TraceRetrievalException ignored) { }
        return result;
    }

    private ResultSetFuture withCallback(ResultSetFuture future) {
        Futures.addCallback(future, new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet result) {
                withQueryTraceInfo(result);
            }

            @Override
            public void onFailure(Throwable t) {
                LOGGER.error("Unable to execute statement", t);
            }
        }, Executors.newCachedThreadPool());
        return future;
    }

    public static synchronized void shutdown() {
        if (instance != null) {
            instance.session.close();
            instance.cluster.close();
            instance = null;
            LOGGER.debug("Cassandra session has been closed!");
        }
    }
}
