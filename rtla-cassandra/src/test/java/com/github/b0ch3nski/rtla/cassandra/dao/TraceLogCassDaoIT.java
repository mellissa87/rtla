package com.github.b0ch3nski.rtla.cassandra.dao;

/**
 * @author bochen
 */
public final class TraceLogCassDaoIT extends SimplifiedLogGenericCassDaoIT {

    public TraceLogCassDaoIT() {
        super(new TraceLogCassDao(getConfig(), DEFAULT_TTL));
    }
}
