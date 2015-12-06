package com.github.b0ch3nski.rtla.cassandra.dao;

/**
 * @author bochen
 */
public final class WarnLogCassDaoIT extends SimplifiedLogGenericCassDaoIT {

    public WarnLogCassDaoIT() {
        super(new WarnLogCassDao(getConfig(), DEFAULT_TTL));
    }
}
