package com.github.b0ch3nski.rtla.cassandra.dao;

/**
 * @author bochen
 */
public final class InfoLogCassDaoIT extends SimplifiedLogGenericCassDaoIT {

    public InfoLogCassDaoIT() {
        super(new InfoLogCassDao(getConfig(), DEFAULT_TTL));
    }
}
