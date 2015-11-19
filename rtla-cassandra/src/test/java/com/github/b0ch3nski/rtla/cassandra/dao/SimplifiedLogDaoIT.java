package com.github.b0ch3nski.rtla.cassandra.dao;

import com.github.b0ch3nski.rtla.cassandra.CassandraTestingHelper;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author bochen
 */
public class SimplifiedLogDaoIT {

    private SimplifiedLogDao dao;

    @BeforeClass
    public static void setUpOnce() {
//        CassandraTestingHelper.launchCassandra("test.cql");
    }

    @Before
    public void setUp() {
//        dao = new SimplifiedLogDao(CassandraTestingHelper.getConfig(), "rtla", "test", 3600);
//        dao.truncate();
    }
}
