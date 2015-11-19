package com.github.b0ch3nski.rtla.cassandra.dao;

import com.datastax.driver.core.BatchStatement;
import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;

/**
 * @author bochen
 */
public class SimplifiedLogDao extends BaseDao<SimplifiedLog> {

    public SimplifiedLogDao(CassandraConfig config, String keyspaceName, String columnName, int timeToLive) {
        super(config, keyspaceName, columnName, timeToLive);
    }

    @Override
    protected void addToBatch(BatchStatement batch, SimplifiedLog item) {
        // TODO
    }
}
