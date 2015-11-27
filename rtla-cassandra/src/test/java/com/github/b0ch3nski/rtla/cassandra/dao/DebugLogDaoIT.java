package com.github.b0ch3nski.rtla.cassandra.dao;

/**
 * @author bochen
 */
public final class DebugLogDaoIT extends SimplifiedLogGenericDaoIT {

    public DebugLogDaoIT() {
        super(new DebugLogDao(getConfig(), DEFAULT_TTL));
    }
}
