package com.github.b0ch3nski.rtla.cassandra.dao;

import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;
import com.github.b0ch3nski.rtla.cassandra.Table;

/**
 * @author bochen
 */
public class ErrorLogDao extends SimplifiedLogGenericDao {

    public ErrorLogDao(CassandraConfig config, long timeToLive) {
        super(config, Table.ERROR, timeToLive);
    }
}
