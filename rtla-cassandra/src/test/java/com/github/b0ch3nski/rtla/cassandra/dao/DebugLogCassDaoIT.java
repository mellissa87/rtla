package com.github.b0ch3nski.rtla.cassandra.dao;

/**
 * @author bochen
 */
public final class DebugLogCassDaoIT extends SimplifiedLogGenericCassDaoIT {

    public DebugLogCassDaoIT() {
        super(new DebugLogCassDao(getConfig(), DEFAULT_TTL));
    }
}
