package com.github.b0ch3nski.rtla.cassandra.dao;

import static com.github.b0ch3nski.rtla.cassandra.CassandraTable.TRACE;

/**
 * @author bochen
 */
public final class TraceLogCassDaoIT extends SimplifiedLogCassDaoIT {

    public TraceLogCassDaoIT() {
        super(SimplifiedLogCassDaoFactory.createDaoForLevel(getConfig(), TRACE));
    }
}
