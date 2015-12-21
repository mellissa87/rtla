package com.github.b0ch3nski.rtla.cassandra.dao;

import static com.github.b0ch3nski.rtla.cassandra.CassandraTable.ERROR;

/**
 * @author bochen
 */
public final class ErrorLogCassDaoIT extends SimplifiedLogCassDaoIT {

    public ErrorLogCassDaoIT() {
        super(SimplifiedLogCassDaoFactory.createDaoForLevel(getConfig(), ERROR));
    }
}
