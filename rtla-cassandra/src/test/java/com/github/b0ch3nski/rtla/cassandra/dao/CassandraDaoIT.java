package com.github.b0ch3nski.rtla.cassandra.dao;

import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;
import com.github.b0ch3nski.rtla.cassandra.EmbeddedCassandra;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;

/**
 * @author bochen
 */
public class CassandraDaoIT {

    @ClassRule
    public static final ExternalResource RESOURCE = new ExternalResource() {
        @Override
        protected void before() {
            EmbeddedCassandra.start();
        }
    };

    protected static CassandraConfig getConfig() {
        return EmbeddedCassandra.getConfig();
    }
}
