package com.github.b0ch3nski.rtla.cassandra.dao;

import static com.github.b0ch3nski.rtla.cassandra.CassandraTable.INFO;

/**
 * @author bochen
 */
public final class InfoLogCassDaoIT extends SimplifiedLogCassDaoIT {

    public InfoLogCassDaoIT() {
        super(SimplifiedLogCassDaoFactory.createDaoForLevel(getConfig(), INFO));
    }
}
