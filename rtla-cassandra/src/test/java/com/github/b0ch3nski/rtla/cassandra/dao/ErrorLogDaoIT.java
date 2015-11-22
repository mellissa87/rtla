package com.github.b0ch3nski.rtla.cassandra.dao;

import ch.qos.logback.classic.Level;
import com.github.b0ch3nski.rtla.cassandra.CassandraTestingHelper;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.RandomLogFactory;
import org.junit.*;
import org.junit.rules.ExternalResource;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author bochen
 */
public class ErrorLogDaoIT {
    private static final long TTL = 3600L;
    private static ErrorLogDao errorLogDao;

    @ClassRule
    public static final ExternalResource RESOURCE = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            CassandraTestingHelper.launchCassandra();
            errorLogDao = new ErrorLogDao(CassandraTestingHelper.getConfig(), TTL);
        }

        @Override
        protected void after() {
            errorLogDao.shutdown();
        }
    };

    @Before
    public void setUp() {
        errorLogDao.truncateTable();
    }

    @Test
    public void shouldSaveAndRetrieveErrorLog() {
        SimplifiedLog toSave = RandomLogFactory.create(Level.ERROR);

        errorLogDao.save(toSave);

        List<SimplifiedLog> retrieved = errorLogDao.get(toSave.getHostName());

        assertThat(errorLogDao.countAllElements(), is(1L));
        assertThat(retrieved.size(), is(1));
        assertThat(retrieved.get(0), is(toSave));
    }
}
