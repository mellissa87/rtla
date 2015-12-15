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
    private final Map<String, PreparedStatement> statementCache = new ConcurrentHashMap<>();
    private final Cluster cluster;
    private final Session session;
    private final SessionHandler sessionHandler;
    private static CassandraSession instance;

    private CassandraSession(CassandraConfig config) {
        cluster = Cluster.builder()
                .addContactPoint(config.getHost())
                .withPort(config.getPort())
                .build();
        session = cluster.connect();
        sessionHandler = new SessionHandler();

        LOGGER.info("New Cassandra session was created with config = {}", config);
    }

    public final class SessionHandler {

        private SessionHandler() { }

        public PreparedStatement getPreparedStatement(String query) {
            PreparedStatement statement = statementCache.get(query);
            if (statement == null) {
                LOGGER.debug("Statement [{}] was not found in cache - preparing it now | Statements in cache = {}", query, statementCache.size() + 1);
                statement = session.prepare(query);
                statementCache.put(query, statement);
            }
            return statement;
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
                    LOGGER.warn("Unable to execute statement", t);
                }
            }, Executors.newCachedThreadPool());
            return future;
        }
    }

    public static CassandraSession getInstance(CassandraConfig config) {
        if (instance == null) {
            synchronized (CassandraSession.class) {
                if (instance == null) {
                    instance = new CassandraSession(config);
                }
            }
        }
        return instance;
    }

    public SessionHandler getSessionHandler() {
        return sessionHandler;
    }

    public static synchronized void shutdown() {
        if (instance != null) {
            instance.session.close();
            instance.cluster.close();
            instance = null;
            LOGGER.info("Cassandra session has been closed!");
        }
    }
}
