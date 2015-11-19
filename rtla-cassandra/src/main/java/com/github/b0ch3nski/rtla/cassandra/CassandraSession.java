package com.github.b0ch3nski.rtla.cassandra;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bochen
 */
public final class CassandraSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraSession.class);
    private static CassandraSession instance = null;
    private Cluster cluster = null;
    private Session session = null;
    private Map<String, PreparedStatement> statementCache = null;

    private CassandraSession(CassandraConfig config) {
        cluster = Cluster.builder()
                .addContactPoint(config.getHost())
                .withPort(config.getPort())
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .withLoadBalancingPolicy(new TokenAwarePolicy(new DCAwareRoundRobinPolicy()))
                .build();
        session = cluster.connect();
        statementCache = new HashMap<>();
        LOGGER.debug("New Cassandra session was created with config = {}", config);
    }

    public static synchronized CassandraSession getInstance(CassandraConfig config) {
        if (instance == null) instance = new CassandraSession(config);
        return instance;
    }

    public PreparedStatement getPreparedStatement(String query) {
        PreparedStatement statement = statementCache.get(query);
        if (statement == null) {
            LOGGER.trace("Statement [{}] was not found in cache - preparing it now", query);
            statement = session.prepare(query);
            statementCache.put(query, statement);
        }
        return statement;
    }

    public ResultSet executeStatement(Statement statement) {
        return session.execute(statement);
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
