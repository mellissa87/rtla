package com.github.b0ch3nski.rtla.cassandra.dao;

import com.datastax.driver.core.*;
import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;
import com.github.b0ch3nski.rtla.cassandra.CassandraSession;

/**
 * @author bochen
 */
public class BaseDao {
    private final CassandraConfig config;
    private final String columnWithKeyspace;

    public BaseDao(CassandraConfig config, String keyspaceName, String columnName) {
        this.config = config;
        columnWithKeyspace = keyspaceName + "." + columnName;
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

    public void shutdown() {
        CassandraSession.shutdown();
    }
}
