package com.github.b0ch3nski.rtla.cassandra.dao;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;
import com.github.b0ch3nski.rtla.cassandra.Table;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog.SimplifiedLogBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import java.util.Date;
import java.util.List;

/**
 * @author bochen
 */
public abstract class SimplifiedLogGenericDao extends BaseDao<SimplifiedLog> {

    SimplifiedLogGenericDao(CassandraConfig config, Table table, long timeToLive) {
        super(config, table, timeToLive);
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
        String selectStatement = "SELECT * FROM " + getTable().getKeyspaceAndTable() + " WHERE host=?;";
        ResultSet result = executeStatement(getPreparedStatement(selectStatement).bind(hostName));

        return getListFromResultSet(result);
    }
}
