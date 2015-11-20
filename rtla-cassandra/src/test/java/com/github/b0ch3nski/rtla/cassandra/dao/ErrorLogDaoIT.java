package com.github.b0ch3nski.rtla.cassandra.dao;

import ch.qos.logback.classic.Level;
import com.github.b0ch3nski.rtla.cassandra.CassandraTestingHelper;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.RandomLogFactory;
import org.junit.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author bochen
 */
public class ErrorLogDaoIT {

    private ErrorLogDao errorLogDao;

    @BeforeClass
    public static void setUpOnce() {
        CassandraTestingHelper.launchCassandra();
    }

    @Before
    public void setUp() {
        errorLogDao = new ErrorLogDao(CassandraTestingHelper.getConfig(), 3600L);
        errorLogDao.truncateTable();
    }

    @Test
    public void shouldSaveAndRetrieveErrorLog() {
        SimplifiedLog before = RandomLogFactory.create(Level.ERROR);

        errorLogDao.save(before);

        List<SimplifiedLog> after = errorLogDao.get(before.getHostName());

        assertThat(errorLogDao.countAllElements(), is(1L));
        assertThat(after.size(), is(1));
        assertThat(after.get(0), is(before));
    }
}
