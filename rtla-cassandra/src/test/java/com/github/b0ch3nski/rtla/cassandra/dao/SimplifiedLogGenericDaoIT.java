package com.github.b0ch3nski.rtla.cassandra.dao;

import ch.qos.logback.classic.Level;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.jayway.awaitility.Awaitility;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.b0ch3nski.rtla.common.utils.RandomLogFactory.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author bochen
 */
public abstract class SimplifiedLogGenericDaoIT extends CassandraDaoIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifiedLogGenericDaoIT.class);
    private static final int MSG_AMOUNT = 64;
    private static final int TIMEOUT = MSG_AMOUNT * 2;
    private static final long TIME_MIN = TIME1 - 10000L;
    private static final long TIME_MAX = TIME1 + 10000L;
    private static Map<String, List<SimplifiedLog>> expected;
    private static SimplifiedLogGenericDao dao;

    protected SimplifiedLogGenericDaoIT(SimplifiedLogGenericDao newDao) {
        dao = newDao;

        Level level = Level.toLevel(dao.getTable().name());
        expected = getPreparedTestData(MSG_AMOUNT, level);

        List<SimplifiedLog> allLogs = expected.get("all");
        dao.save(allLogs);

        waitForMessages(MSG_AMOUNT);
        LOGGER.debug("Successfully saved {} {} messages in Cassandra", MSG_AMOUNT, level);
    }

    private void waitForMessages(int expected) {
        Awaitility.await()
                .atMost(TIMEOUT, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> (dao.countAllElements() == expected));
    }

    private void checkLists(List<SimplifiedLog> expected, List<SimplifiedLog> retrieved) {
        assertThat(retrieved.size(), is(expected.size()));
        assertThat(retrieved.containsAll(expected), is(true));
    }

    @Test
    public void shouldRetrieveLogsByHost() {
        List<SimplifiedLog> expected = SimplifiedLogGenericDaoIT.expected.get("host");

        List<SimplifiedLog> retrieved = dao.getByHost(HOST1);

        checkLists(expected, retrieved);
    }

    @Test
    public void shouldRetrieveLogsByTimestamp() {
        List<SimplifiedLog> expected = SimplifiedLogGenericDaoIT.expected.get("time");

        List<SimplifiedLog> retrieved = dao.getByTime(HOST1, TIME_MIN, TIME_MAX);

        checkLists(expected, retrieved);
    }

    @Test
    public void shouldRetrieveLogsByLogger() {
        List<SimplifiedLog> expected = SimplifiedLogGenericDaoIT.expected.get("logger");

        List<SimplifiedLog> retrieved = dao.getByLogger(HOST1, TIME_MIN, TIME_MAX, LOGGER1);

        checkLists(expected, retrieved);
    }

    @Test
    public void shouldRetrieveLogsByThread() {
        List<SimplifiedLog> expected = SimplifiedLogGenericDaoIT.expected.get("thread");

        List<SimplifiedLog> retrieved = dao.getByThread(HOST1, TIME_MIN, TIME_MAX, THREAD1);

        checkLists(expected, retrieved);
    }

    @Test
    public void shouldRetrieveLogsByLoggerAndThread() {
        List<SimplifiedLog> expected = SimplifiedLogGenericDaoIT.expected.get("logger_thread");

        List<SimplifiedLog> retrieved = dao.getByLoggerAndThread(HOST1, TIME_MIN, TIME_MAX, LOGGER1, THREAD1);

        checkLists(expected, retrieved);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        if (dao != null) {
            dao.truncateTable();
            dao.shutdown();
        }
    }
}
