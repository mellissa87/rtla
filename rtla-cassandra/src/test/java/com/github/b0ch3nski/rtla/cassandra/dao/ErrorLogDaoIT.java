package com.github.b0ch3nski.rtla.cassandra.dao;

/**
 * @author bochen
 */
public final class ErrorLogDaoIT extends SimplifiedLogGenericDaoIT {

    public ErrorLogDaoIT() {
        super(new ErrorLogDao(getConfig(), DEFAULT_TTL));
    }
}
