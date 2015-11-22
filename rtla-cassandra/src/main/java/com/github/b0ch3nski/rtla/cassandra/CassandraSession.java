package com.github.b0ch3nski.rtla.cassandra;

import com.datastax.driver.core.*;
import com.datastax.driver.core.QueryTrace.Event;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bochen
 */
public final class CassandraSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraSession.class);
    private static CassandraSession instance;
    private Map<String, PreparedStatement> statementCache;
    private boolean traceQuery;
    private Cluster cluster;
    private Session session;

    private CassandraSession(CassandraConfig config) {
        statementCache = new HashMap<>();
        traceQuery = LOGGER.isDebugEnabled();

        cluster = Cluster.builder()
                .addContactPoint(config.getHost())
                .withPort(config.getPort())
                .withSocketOptions(new SocketOptions().setReadTimeoutMillis(30000))
                .build();
        session = cluster.connect();

        LOGGER.debug("New Cassandra session was created with config = {}", config);
    }

    public static synchronized CassandraSession getInstance(CassandraConfig config) {
        if (instance == null) instance = new CassandraSession(config);
        return instance;
    }

    public synchronized PreparedStatement getPreparedStatement(String query) {
        PreparedStatement statement = statementCache.get(query);
        if (statement == null) {
            LOGGER.trace("Statement [{}] was not found in cache - preparing it now | Statements in cache = {}", query, statementCache.size());
            statement = session.prepare(query);
            statementCache.put(query, statement);
        }
        return statement;
    }

    public Statement getStatement(String query) {
        return session.newSimpleStatement(query);
    }

    public ResultSet executeStatement(Statement statement) {
        if (traceQuery) statement.enableTracing();
        return withQueryTraceInfo(session.execute(statement));
    }

    public ResultSetFuture executeAsyncStatement(Statement statement) {
        if (traceQuery) statement.enableTracing();
        return withCallback(session.executeAsync(statement));
    }

    private ResultSet withQueryTraceInfo(ResultSet result) {
        if (traceQuery) {
            QueryTrace trace = result.getExecutionInfo().getQueryTrace();

            if (LOGGER.isTraceEnabled()) {
                for (Event event : trace.getEvents()) {
                    LOGGER.trace("{}", event);
                }
            }
            LOGGER.debug("Request type: {} | Coordinator used: {} | Execution took: {} microseconds",
                    trace.getRequestType(), trace.getCoordinator(), trace.getDurationMicros());
        }
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
        });
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
