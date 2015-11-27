package com.github.b0ch3nski.rtla.cassandra.dao;

/**
 * @author bochen
 */
public final class TraceLogDaoIT extends SimplifiedLogGenericDaoIT {

    public TraceLogDaoIT() {
        super(new TraceLogDao(getConfig(), DEFAULT_TTL));
    }
}
