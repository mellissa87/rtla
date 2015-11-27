package com.github.b0ch3nski.rtla.cassandra.dao;

import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;

import static com.github.b0ch3nski.rtla.cassandra.Table.DEBUG;

/**
 * @author bochen
 */
public final class DebugLogDao extends SimplifiedLogGenericDao {

    public DebugLogDao(CassandraConfig config, long timeToLive) {
        super(config, DEBUG, timeToLive);
    }
}
