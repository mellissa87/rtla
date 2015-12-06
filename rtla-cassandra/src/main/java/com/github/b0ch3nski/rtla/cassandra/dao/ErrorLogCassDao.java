package com.github.b0ch3nski.rtla.cassandra.dao;

import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;

import static com.github.b0ch3nski.rtla.cassandra.CassandraTable.ERROR;

/**
 * @author bochen
 */
public final class ErrorLogCassDao extends SimplifiedLogGenericCassDao {

    public ErrorLogCassDao(CassandraConfig config, long timeToLive) {
        super(config, ERROR, timeToLive);
    }
}