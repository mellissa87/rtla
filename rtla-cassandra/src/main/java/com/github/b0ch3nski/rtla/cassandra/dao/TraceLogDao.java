package com.github.b0ch3nski.rtla.cassandra.dao;

import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;

import static com.github.b0ch3nski.rtla.cassandra.CassandraTable.TRACE;

/**
 * @author bochen
 */
public final class TraceLogDao extends SimplifiedLogGenericDao {

    public TraceLogDao(CassandraConfig config, long timeToLive) {
        super(config, TRACE, timeToLive);
    }
}
