package com.github.b0ch3nski.rtla.cassandra.dao;

/**
 * @author bochen
 */
public final class WarnLogDaoIT extends SimplifiedLogGenericDaoIT {

    public WarnLogDaoIT() {
        super(new WarnLogDao(getConfig(), DEFAULT_TTL));
    }
}
