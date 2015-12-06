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
    private static SimplifiedLogGenericDao dao;
    private Map<String, List<SimplifiedLog>> expectedLists;

    protected SimplifiedLogGenericDaoIT(SimplifiedLogGenericDao newDao) {
        dao = newDao;
        prepareData();
    }

    private void prepareData() {
        Level level = Level.toLevel(dao.getTable().name());
        expectedLists = getPreparedTestData(MSG_AMOUNT, level);

        List<SimplifiedLog> allLogs = expectedLists.get("all");
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

    private void checkLists(String expectedListName, List<SimplifiedLog> retrievedList) {
        List<SimplifiedLog> expectedList = expectedLists.get(expectedListName);

        LOGGER.debug("Looking for {} results [{}] | Got {}", expectedList.size(), expectedListName, retrievedList.size());
        assertThat(retrievedList.size(), is(expectedList.size()));
        assertThat(retrievedList.containsAll(expectedList), is(true));
    }

    @Test
    public void shouldRetrieveLogsByHost() {
        List<SimplifiedLog> retrievedList = dao.getByHost(HOST1);

        checkLists("host", retrievedList);
    }

    @Test
    public void shouldRetrieveLogsByTimestamp() {
        List<SimplifiedLog> retrievedList = dao.getByTime(HOST1, TIME_MIN, TIME_MAX);

        checkLists("time", retrievedList);
    }

    @Test
    public void shouldRetrieveLogsByLogger() {
        List<SimplifiedLog> retrievedList = dao.getByLogger(HOST1, TIME_MIN, TIME_MAX, LOGGER1);

        checkLists("logger", retrievedList);
    }

    @Test
    public void shouldRetrieveLogsByThread() {
        List<SimplifiedLog> retrievedList = dao.getByThread(HOST1, TIME_MIN, TIME_MAX, THREAD1);

        checkLists("thread", retrievedList);
    }

    @Test
    public void shouldRetrieveLogsByLoggerAndThread() {
        List<SimplifiedLog> retrievedList = dao.getByLoggerAndThread(HOST1, TIME_MIN, TIME_MAX, LOGGER1, THREAD1);

        checkLists("logger_thread", retrievedList);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        if (dao != null) {
            dao.truncateTable();
            dao.shutdown();
        }
    }
}
