package com.github.b0ch3nski.rtla.cassandra.dao;

import com.datastax.driver.core.*;
import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;
import com.github.b0ch3nski.rtla.cassandra.Table;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog.SimplifiedLogBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

import java.util.*;

/**
 * @author bochen
 */
public abstract class SimplifiedLogGenericDao extends BaseDao<SimplifiedLog> {

    private final Map<String, String> selectQueries;

    SimplifiedLogGenericDao(CassandraConfig config, Table table, long timeToLive) {
        super(config, table, timeToLive);

        String keyspaceAndTable = getTable().getKeyspaceAndTable();
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put("host", "SELECT * FROM " + keyspaceAndTable + " WHERE host = ?;");
        builder.put("time", "SELECT * FROM " + keyspaceAndTable + " WHERE host = ? AND time >= ? AND time <= ?;");
        builder.put("logger", "SELECT * FROM " + keyspaceAndTable + " WHERE host = ? AND time >= ? AND time <= ? AND logger = ?;");
        builder.put("thread", "SELECT * FROM " + keyspaceAndTable + " WHERE host = ? AND time >= ? AND time <= ? AND logger = ? AND thread = ?;");
        selectQueries = builder.build();
    }

    @Override
    protected Object[] getValuesToInsert(SimplifiedLog item) {
        return new Object[] {
                item.getHostName(),
                new Date(item.getTimeStamp()),
                item.getLoggerName(),
                item.getThreadName(),
                item.getFormattedMessage()
        };
    }

    @Override
    protected String[] getColumns() {
        return new String[] { "host", "time", "logger", "thread", "message" };
    }

    @Override
    protected List<SimplifiedLog> getListFromResultSet(ResultSet result) {
        Builder<SimplifiedLog> builder = ImmutableList.builder();

        for (Row single : result) {
            builder.add(getObjectFromRow(single));
        }
        return builder.build();
    }

    @Override
    protected SimplifiedLog getObjectFromRow(Row single) {
        return new SimplifiedLogBuilder()
                .withTimeStamp(single.getTimestamp("time").getTime())
                .withHostName(single.getString("host"))
                .withLevel(getTable().name())
                .withLoggerName(single.getString("logger"))
                .withThreadName(single.getString("thread"))
                .withFormattedMessage(single.getString("message"))
                .build();
    }

    public List<SimplifiedLog> get(String hostName) {
        PreparedStatement statement = getPreparedStatement(selectQueries.get("host"));
        ResultSet result = executeStatement(statement.bind(hostName));
        return getListFromResultSet(result);
    }

    public List<SimplifiedLog> get(String hostName, long startTime, long stopTime) {
        PreparedStatement statement = getPreparedStatement(selectQueries.get("time"));
        ResultSet result = executeStatement(statement.bind(hostName, new Date(startTime), new Date(stopTime)));
        return getListFromResultSet(result);
    }

    public List<SimplifiedLog> get(String hostName, long startTime, long stopTime, String loggerName) {
        PreparedStatement statement = getPreparedStatement(selectQueries.get("logger"));
        ResultSet result = executeStatement(statement.bind(hostName, new Date(startTime), new Date(stopTime), loggerName));
        return getListFromResultSet(result);
    }

    public List<SimplifiedLog> get(String hostName, long startTime, long stopTime, String loggerName, String threadName) {
        PreparedStatement statement = getPreparedStatement(selectQueries.get("thread"));
        ResultSet result = executeStatement(statement.bind(hostName, new Date(startTime), new Date(stopTime), loggerName, threadName));
        return getListFromResultSet(result);
    }
}
