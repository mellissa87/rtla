package com.github.b0ch3nski.rtla.cassandra.dao;

import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;

import static com.github.b0ch3nski.rtla.cassandra.CassandraTable.INFO;

/**
 * @author bochen
 */
public final class InfoLogCassDao extends SimplifiedLogGenericCassDao {

    public InfoLogCassDao(CassandraConfig config) {
        super(config, INFO);
    }
}
