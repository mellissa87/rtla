package com.github.b0ch3nski.rtla.cassandra.dao;

import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;

import static com.github.b0ch3nski.rtla.cassandra.CassandraTable.DEBUG;

/**
 * @author bochen
 */
public final class DebugLogCassDao extends SimplifiedLogGenericCassDao {

    public DebugLogCassDao(CassandraConfig config, long timeToLive) {
        super(config, DEBUG, timeToLive);
    }
}
