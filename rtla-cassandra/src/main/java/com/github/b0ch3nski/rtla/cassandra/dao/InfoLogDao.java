package com.github.b0ch3nski.rtla.cassandra.dao;

import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;

import static com.github.b0ch3nski.rtla.cassandra.Table.INFO;

/**
 * @author bochen
 */
public final class InfoLogDao extends SimplifiedLogGenericDao {

    public InfoLogDao(CassandraConfig config, long timeToLive) {
        super(config, INFO, timeToLive);
    }
}
