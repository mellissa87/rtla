package com.github.b0ch3nski.rtla.cassandra.dao;

import com.datastax.driver.core.*;
import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;
import com.github.b0ch3nski.rtla.cassandra.CassandraTable;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.serialization.SimplifiedLogSerializer;
import com.github.b0ch3nski.rtla.common.utils.Filters;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * @author bochen
 */
public abstract class SimplifiedLogGenericCassDao extends BaseCassDao<SimplifiedLog> {

    private static final String HOST = "host";
    private static final String TIME = "time";
    private static final String LOG = "log";

    private static final SimplifiedLogSerializer SERIALIZER = new SimplifiedLogSerializer();
    private final Map<String, String> selectQueries;

    protected SimplifiedLogGenericCassDao(CassandraConfig config, CassandraTable table) {
        super(config, table);

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
    protected String[] provideColumns() {
        return new String[] { HOST, TIME, LOG };
    }

    @Override
    protected List<SimplifiedLog> getListFromResultSet(ResultSet result) {
        Builder<SimplifiedLog> builder = ImmutableList.builder();

        result.forEach(row -> builder.add(getObjectFromRow(row)));
        return builder.build();
    }

    @Override
    protected SimplifiedLog getObjectFromRow(Row single) {
        ByteBuffer buffer = single.getBytesUnsafe(LOG);
        byte[] serialized = new byte[buffer.remaining()];
        buffer.get(serialized);
        return SERIALIZER.fromBytes(serialized);
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
        return Filters.filterByLogger(getByTime(hostName, startTime, stopTime), loggerName);
    }

    public final List<SimplifiedLog> getByThread(String hostName, long startTime, long stopTime, String threadName) {
        return Filters.filterByThread(getByTime(hostName, startTime, stopTime), threadName);
    }

    public final List<SimplifiedLog> getByLoggerAndThread(String hostName, long startTime, long stopTime, String loggerName, String threadName) {
        return Filters.filterByThread(getByLogger(hostName, startTime, stopTime, loggerName), threadName);
    }
}
