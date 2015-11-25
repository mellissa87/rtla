package com.github.b0ch3nski.rtla.cassandra.dao;

import ch.qos.logback.classic.Level;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.Validation;
import com.jayway.awaitility.Awaitility;
import org.junit.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.b0ch3nski.rtla.common.utils.RandomLogFactory.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author bochen
 */
public class ErrorLogDaoIT extends DaoIT {

    private static final int MSG_AMOUNT = 50;
    private static final int TIMEOUT = MSG_AMOUNT * 2;

    private static final List<SimplifiedLog> ALL_LOGS = create(MSG_AMOUNT, Level.ERROR, false);
    private static final Map<String, List<SimplifiedLog>> OUTPUT = new HashMap<>();
    private static final long TIME_MIN = TIME1 - 10000L;
    private static final long TIME_MAX = TIME1 + 10000L;

    private static final ErrorLogDao DAO = new ErrorLogDao(getConfig(), DEFAULT_TTL);

    static {
        List<SimplifiedLog> host = ALL_LOGS.stream().filter(log -> log.getHostName().equals(HOST1)).collect(Collectors.toList());
        List<SimplifiedLog> time = host.stream().filter(Validation.isTimestampAround(TIME1)).collect(Collectors.toList());
        List<SimplifiedLog> thread = time.stream().filter(log -> log.getThreadName().equals(THREAD1)).collect(Collectors.toList());
        List<SimplifiedLog> logger = thread.stream().filter(log -> log.getLoggerName().equals(LOGGER1)).collect(Collectors.toList());
        OUTPUT.put("host", host);
        OUTPUT.put("time", time);
        OUTPUT.put("thread", thread);
        OUTPUT.put("logger", logger);
    }

    @BeforeClass
    public static void setUpBeforeClass() {
        DAO.save(ALL_LOGS);
        waitForMessages(MSG_AMOUNT);
    }

    private static void waitForMessages(int count) {
        Awaitility.await()
                .atMost(TIMEOUT, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> (DAO.countAllElements() == count));
    }

    private void checkLists(List<SimplifiedLog> expected, List<SimplifiedLog> retrieved) {
        assertThat(retrieved.size(), is(expected.size()));
        assertThat(retrieved.containsAll(expected), is(true));
    }

    @Test
    public void shouldRetrieveLogsByHost() {
        List<SimplifiedLog> expected = OUTPUT.get("host");

        List<SimplifiedLog> retrieved = DAO.get(HOST1);

        checkLists(expected, retrieved);
    }

    @Test
    public void shouldRetrieveLogsByTimestamp() {
        List<SimplifiedLog> expected = OUTPUT.get("time");

        List<SimplifiedLog> retrieved = DAO.get(HOST1, TIME_MIN, TIME_MAX);

        checkLists(expected, retrieved);
    }

    /*
    @Test
    public void shouldRetrieveLogsByThread() {
        List<SimplifiedLog> expected = OUTPUT.get("thread");

        List<SimplifiedLog> retrieved = DAO.get(HOST1, TIME_MIN, TIME_MAX, THREAD1);

        checkLists(expected, retrieved);
    }

    @Test
    public void shouldRetrieveLogsByLogger() {
        List<SimplifiedLog> expected = OUTPUT.get("logger");

        List<SimplifiedLog> retrieved = DAO.get(HOST1, TIME_MIN, TIME_MAX, THREAD1, LOGGER1);

        checkLists(expected, retrieved);
    }
    */

    @AfterClass
    public static void tearDownAfterClass() {
        DAO.truncateTable();
        DAO.shutdown();
    }
}
