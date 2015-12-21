package com.github.b0ch3nski.rtla.cassandra.dao;

import static com.github.b0ch3nski.rtla.cassandra.CassandraTable.WARN;

/**
 * @author bochen
 */
public final class WarnLogCassDaoIT extends SimplifiedLogCassDaoIT {

    public WarnLogCassDaoIT() {
        super(SimplifiedLogCassDaoFactory.createDaoForLevel(getConfig(), WARN));
    }
}
