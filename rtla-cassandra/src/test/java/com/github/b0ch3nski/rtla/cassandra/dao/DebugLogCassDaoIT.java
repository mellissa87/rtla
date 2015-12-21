package com.github.b0ch3nski.rtla.cassandra.dao;

import static com.github.b0ch3nski.rtla.cassandra.CassandraTable.DEBUG;

/**
 * @author bochen
 */
public final class DebugLogCassDaoIT extends SimplifiedLogCassDaoIT {

    public DebugLogCassDaoIT() {
        super(SimplifiedLogCassDaoFactory.createDaoForLevel(getConfig(), DEBUG));
    }
}
