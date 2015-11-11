package com.github.b0ch3nski.rtla.simulator.utils;

import org.slf4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author bochen
 */
public final class LogReader {
    private static final Pattern PATTERN = Pattern.compile("(.+?)  (\\d+?:\\d+?:\\d+?) (.+)");
    private final Logger logger;
    private final int delay;

    public LogReader(Logger logger, int delay) {
        this.logger = logger;
        this.delay = delay;
    }

    private void getLoggerLevel(Matcher matcher) {
        String level = matcher.group(1);
        String log = matcher.group(3);
        switch (level) {
            case "TRACE": logger.trace(log); break;
            case "DEBUG": logger.debug(log); break;
            case "INFO": logger.info(log); break;
            case "WARN": logger.warn(log); break;
            case "ERROR": logger.error(log); break;
            default: throw new IllegalStateException("Cannot recognize logger level " + level);
        }
    }

    public void handleSingleLine(String line) {
        Matcher matcher = PATTERN.matcher(line);
        while (matcher.find()) {
            getLoggerLevel(matcher);
        }
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {
        }
    }
}
