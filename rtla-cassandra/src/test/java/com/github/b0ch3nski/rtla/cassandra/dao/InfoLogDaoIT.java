package com.github.b0ch3nski.rtla.cassandra.dao;

/**
 * @author bochen
 */
public final class InfoLogDaoIT extends SimplifiedLogGenericDaoIT {

    public InfoLogDaoIT() {
        super(new InfoLogDao(getConfig(), DEFAULT_TTL));
    }
}
