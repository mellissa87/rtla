package com.github.b0ch3nski.rtla.common.utils;

import ch.qos.logback.classic.Level;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog.SimplifiedLogBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.net.InetAddresses;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bochen
 */
public final class RandomLogFactory {

    public static final String HOST1 = "host1";
    public static final String HOST2 = "host2";
    public static final long TIME1 = 1429725600000L;
    public static final long TIME2 = 1434841200000L;
    public static final String THREAD1 = "thread1";
    public static final String THREAD2 = "thread2";
    public static final String LOGGER1 = "logger1";
    public static final String LOGGER2 = "logger2";

    private static final Random RAND = new Random();

    private RandomLogFactory() { }

    private static int getRandomIntBetween(int lower, int upper) {
        return (int) (Math.random() * (upper - lower)) + lower;
    }

    private static Level getRandomLogLevel() {
        return Level.toLevel(RandomStringUtils.randomAlphanumeric(5));
    }

    private static String getRandomString(int minChars, int maxChars) {
        return RandomStringUtils.randomAlphanumeric(getRandomIntBetween(minChars, maxChars));
    }

    public static String getRandomIpAddress() {
        return InetAddresses.fromInteger(RAND.nextInt()).getHostAddress();
    }

    public static SimplifiedLog create() {
        return create(getRandomLogLevel());
    }

    public static SimplifiedLog create(String hostName) {
        return create(getRandomLogLevel(), hostName);
    }

    public static SimplifiedLog create(Level level) {
        return create(level, getRandomIpAddress());
    }

    public static SimplifiedLog create(Level level, String hostName) {
        return create(
                level,
                hostName,
                System.currentTimeMillis(),
                getRandomString(10, 20),
                getRandomString(10, 20)
        );
    }

    private static SimplifiedLog create(Level level, String hostName, long timeStamp, String threadName, String loggerName) {
        return new SimplifiedLogBuilder()
                .withTimeStamp(timeStamp)
                .withHostName(hostName)
                .withLevel(level.toString())
                .withThreadName(threadName)
                .withLoggerName(loggerName)
                .withFormattedMessage(getRandomString(50, 150))
                .build();
    }

    public static List<SimplifiedLog> create(int amount) {
        return create(amount, getRandomLogLevel(), true);
    }

    public static List<SimplifiedLog> create(int amount, String hostName) {
        return create(amount, getRandomLogLevel(), hostName, true);
    }

    private static List<SimplifiedLog> create(int amount, Level level, boolean random) {
        return create(amount, level, getRandomIpAddress(), random);
    }

    private static List<SimplifiedLog> create(int amount, Level level, String hostName, boolean random) {
        Builder<SimplifiedLog> builder = ImmutableList.builder();
        for (int i = 0; i < amount; i++) {
            builder.add((random) ? create(level, hostName) : createPrepared(level, i));
        }
        return builder.build();
    }

    private static SimplifiedLog createPrepared(Level level, int i) {
        return create(
                level,
                ((i % 2) == 0) ? HOST1 : HOST2,
                (((i % 3) == 0) ? TIME1 : TIME2) + (i * 10),
                ((i % 4) == 0) ? THREAD1 : THREAD2,
                ((i % 5) == 0) ? LOGGER1 : LOGGER2
        );
    }

    public static Map<String, List<SimplifiedLog>> getPreparedTestData(int msgAmount, Level level) {
        List<SimplifiedLog> allLogs = create(msgAmount, level, false);
        List<SimplifiedLog> host = allLogs.stream().filter(log -> log.getHostName().equals(HOST1)).collect(Collectors.toList());
        List<SimplifiedLog> time = host.stream().filter(Validators.isTimestampAround(TIME1)).collect(Collectors.toList());
        List<SimplifiedLog> logger = Filters.filterByLogger(time, LOGGER1);
        List<SimplifiedLog> thread = Filters.filterByThread(time, THREAD1);
        List<SimplifiedLog> loggerThread = Filters.filterByThread(logger, THREAD1);

        Map<String, List<SimplifiedLog>> toReturn = new HashMap<>();
        toReturn.put("all", allLogs);
        toReturn.put("host", host);
        toReturn.put("time", time);
        toReturn.put("logger", logger);
        toReturn.put("thread", thread);
        toReturn.put("logger_thread", loggerThread);
        return toReturn;
    }
}
