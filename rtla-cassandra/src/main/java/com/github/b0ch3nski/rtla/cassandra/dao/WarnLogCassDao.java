package com.github.b0ch3nski.rtla.cassandra.dao;

import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;

import static com.github.b0ch3nski.rtla.cassandra.CassandraTable.WARN;

/**
 * @author bochen
 */
public final class WarnLogCassDao extends SimplifiedLogGenericCassDao {

    public WarnLogCassDao(CassandraConfig config) {
        super(config, WARN);
    }
}
