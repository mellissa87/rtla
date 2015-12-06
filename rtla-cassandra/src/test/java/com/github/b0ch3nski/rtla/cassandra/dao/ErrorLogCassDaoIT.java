package com.github.b0ch3nski.rtla.cassandra.dao;

/**
 * @author bochen
 */
public final class ErrorLogCassDaoIT extends SimplifiedLogGenericCassDaoIT {

    public ErrorLogCassDaoIT() {
        super(new ErrorLogCassDao(getConfig(), DEFAULT_TTL));
    }
}
