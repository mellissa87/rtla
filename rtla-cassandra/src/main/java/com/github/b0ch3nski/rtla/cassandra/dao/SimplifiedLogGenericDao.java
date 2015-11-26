package com.github.b0ch3nski.rtla.cassandra.dao;

import com.datastax.driver.core.*;
import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;
import com.github.b0ch3nski.rtla.cassandra.Table;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.serialization.SimplifiedLogSerializer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bochen
 */
public abstract class SimplifiedLogGenericDao extends BaseDao<SimplifiedLog> {

    private static final String HOST = "host";
    private static final String TIME = "time";
    private static final String LOG = "log";

    private static final SimplifiedLogSerializer SERIALIZER = new SimplifiedLogSerializer();
    private final Map<String, String> selectQueries;

    SimplifiedLogGenericDao(CassandraConfig config, Table table, long timeToLive) {
        super(config, table, timeToLive);

        String keyspaceAndTable = getTable().getKeyspaceAndTable();
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put(HOST, "SELECT * FROM " + keyspaceAndTable + " WHERE " + HOST + " = ?;");
        builder.put(TIME, "SELECT * FROM " + keyspaceAndTable + " WHERE " + HOST + " = ? AND " + TIME + " >= ? AND " + TIME + " <= ?;");
        selectQueries = builder.build();
    }

    @Override
    protected Object[] getValuesToInsert(SimplifiedLog item) {
        return new Object[] {
                item.getHostName(),
                new Date(item.getTimeStamp()),
                ByteBuffer.wrap(SERIALIZER.toBytes(item))
        };
    }

    @Override
    protected String[] getColumns() {
        return new String[] { HOST, TIME, LOG };
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
        ByteBuffer buffer = single.getBytesUnsafe(LOG);
        byte[] serialized = new byte[buffer.remaining()];
        buffer.get(serialized);
        return SERIALIZER.fromBytes(serialized);
    }

    @VisibleForTesting
    static List<SimplifiedLog> filterByLogger(List<SimplifiedLog> input, String loggerName) {
        return input.parallelStream().filter(log -> log.getLoggerName().equals(loggerName)).collect(Collectors.toList());
    }

    @VisibleForTesting
    static List<SimplifiedLog> filterByThread(List<SimplifiedLog> input, String threadName) {
        return input.parallelStream().filter(log -> log.getThreadName().equals(threadName)).collect(Collectors.toList());
    }

    public final List<SimplifiedLog> getByHost(String hostName) {
        PreparedStatement statement = getPreparedStatement(selectQueries.get(HOST));
        ResultSet result = executeStatement(statement.bind(hostName));
        return getListFromResultSet(result);
    }

    public final List<SimplifiedLog> getByTime(String hostName, long startTime, long stopTime) {
        PreparedStatement statement = getPreparedStatement(selectQueries.get(TIME));
        ResultSet result = executeStatement(statement.bind(hostName, new Date(startTime), new Date(stopTime)));
        return getListFromResultSet(result);
    }

    public final List<SimplifiedLog> getByLogger(String hostName, long startTime, long stopTime, String loggerName) {
        return filterByLogger(getByTime(hostName, startTime, stopTime), loggerName);
    }

    public final List<SimplifiedLog> getByThread(String hostName, long startTime, long stopTime, String threadName) {
        return filterByThread(getByTime(hostName, startTime, stopTime), threadName);
    }

    public final List<SimplifiedLog> getByLoggerAndThread(String hostName, long startTime, long stopTime, String loggerName, String threadName) {
        return filterByThread(getByLogger(hostName, startTime, stopTime, loggerName), threadName);
    }
}
