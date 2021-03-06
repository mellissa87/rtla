package com.github.b0ch3nski.rtla.cassandra.dao;

import com.github.b0ch3nski.rtla.cassandra.*;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;

/**
 * @author bochen
 */
public abstract class CassandraDaoIT {

    @ClassRule
    public static final ExternalResource RESOURCE = new ExternalResource() {
        @Override
        protected void before() {
            EmbeddedCassandra.start();
        }

        @Override
        protected void after() {
            CassandraSession.shutdown();
        }
    };

    protected static CassandraConfig getConfig() {
        return EmbeddedCassandra.getConfig();
    }
}
