package com.github.b0ch3nski.rtla.simulator.utils;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author bochen
 */
public final class LogReader {
    private static final Pattern PATTERN = Pattern.compile("(.+?)  (\\d+?:\\d+?:\\d+?) (.+)");
    private final Logger logger;
    private final List<Path> allFiles;
    private final int delay;

    public LogReader(Logger logger, String inputDir, int delay) {
        this.logger = logger;
        allFiles = FileHandler.listAllFiles(inputDir);
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

    private void handleSingleLine(String line) {
        Matcher matcher = PATTERN.matcher(line);
        while (matcher.find()) {
            getLoggerLevel(matcher);
        }
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {
        }
    }

    private void handleAllLines(Path singleFile) throws IOException {
        Files.lines(singleFile).forEach(this:: handleSingleLine);
    }

    public void iterateOverAllFiles() throws IOException {
        for (Path singleFile : allFiles) {
            handleAllLines(singleFile);
        }
    }
}
