package com.github.b0ch3nski.rtla.cassandra.dao;

import ch.qos.logback.classic.Level;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.Validation;
import com.jayway.awaitility.Awaitility;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private static final Map<String, List<SimplifiedLog>> EXPECTED = new HashMap<>();
    private static SimplifiedLogGenericDao dao;

    protected SimplifiedLogGenericDaoIT(SimplifiedLogGenericDao newDao) {
        dao = newDao;
        prepareTestData();
    }

    private void prepareTestData() {
        Level level = Level.toLevel(dao.getTable().name());

        List<SimplifiedLog> allLogs = create(MSG_AMOUNT, level, false);
        List<SimplifiedLog> host = allLogs.stream().filter(log -> log.getHostName().equals(HOST1)).collect(Collectors.toList());
        List<SimplifiedLog> time = host.stream().filter(Validation.isTimestampAround(TIME1)).collect(Collectors.toList());
        List<SimplifiedLog> logger = SimplifiedLogGenericDao.filterByLogger(time, LOGGER1);
        List<SimplifiedLog> thread = SimplifiedLogGenericDao.filterByThread(time, THREAD1);
        List<SimplifiedLog> loggerThread = SimplifiedLogGenericDao.filterByThread(logger, THREAD1);
        EXPECTED.put("host", host);
        EXPECTED.put("time", time);
        EXPECTED.put("logger", logger);
        EXPECTED.put("thread", thread);
        EXPECTED.put("logger_thread", loggerThread);

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
        List<SimplifiedLog> expected = EXPECTED.get("host");

        List<SimplifiedLog> retrieved = dao.getByHost(HOST1);

        checkLists(expected, retrieved);
    }

    @Test
    public void shouldRetrieveLogsByTimestamp() {
        List<SimplifiedLog> expected = EXPECTED.get("time");

        List<SimplifiedLog> retrieved = dao.getByTime(HOST1, TIME_MIN, TIME_MAX);

        checkLists(expected, retrieved);
    }

    @Test
    public void shouldRetrieveLogsByLogger() {
        List<SimplifiedLog> expected = EXPECTED.get("logger");

        List<SimplifiedLog> retrieved = dao.getByLogger(HOST1, TIME_MIN, TIME_MAX, LOGGER1);

        checkLists(expected, retrieved);
    }

    @Test
    public void shouldRetrieveLogsByThread() {
        List<SimplifiedLog> expected = EXPECTED.get("thread");

        List<SimplifiedLog> retrieved = dao.getByThread(HOST1, TIME_MIN, TIME_MAX, THREAD1);

        checkLists(expected, retrieved);
    }

    @Test
    public void shouldRetrieveLogsByLoggerAndThread() {
        List<SimplifiedLog> expected = EXPECTED.get("logger_thread");

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
