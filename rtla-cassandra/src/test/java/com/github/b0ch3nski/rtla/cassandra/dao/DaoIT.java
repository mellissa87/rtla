package com.github.b0ch3nski.rtla.cassandra.dao;

import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;
import com.github.b0ch3nski.rtla.cassandra.CassandraTestingHelper;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;

/**
 * @author bochen
 */
public class DaoIT {

    public static final long DEFAULT_TTL = 3600L;
    private static CassandraConfig config;

    @ClassRule
    public static final ExternalResource RESOURCE = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            CassandraTestingHelper.launchCassandra();
        }

        @Override
        protected void after() {
        }
    };

    public static CassandraConfig getConfig() {
        if (config == null) {
            config = CassandraTestingHelper.getConfig();
        }
        return config;
    }
}
