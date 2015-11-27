package com.github.b0ch3nski.rtla.cassandra.dao;

import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;
import com.github.b0ch3nski.rtla.cassandra.Table;

/**
 * @author bochen
 */
public final class TraceLogDao extends SimplifiedLogGenericDao {

    public TraceLogDao(CassandraConfig config, long timeToLive) {
        super(config, Table.TRACE, timeToLive);
    }
}
