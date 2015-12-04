package com.github.b0ch3nski.rtla.common.utils;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bochen
 */
public final class Filters {

    private Filters() { }

    public static List<SimplifiedLog> filterByLogger(List<SimplifiedLog> input, String loggerName) {
        return input.parallelStream().filter(log -> log.getLoggerName().equals(loggerName)).collect(Collectors.toList());
    }

    public static List<SimplifiedLog> filterByThread(List<SimplifiedLog> input, String threadName) {
        return input.parallelStream().filter(log -> log.getThreadName().equals(threadName)).collect(Collectors.toList());
    }
}
