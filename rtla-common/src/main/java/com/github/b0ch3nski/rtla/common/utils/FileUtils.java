package com.github.b0ch3nski.rtla.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author bochen
 */
public final class FileUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() { }

    public static String createTmpDir(String dirName) {
        File tmpDir;
        try {
            tmpDir = Files.createTempDirectory(dirName).toFile();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't create temporary dir " + dirName, e);
        }
        tmpDir.deleteOnExit();
        String path = tmpDir.getAbsolutePath();

        LOGGER.debug("Created temporary directory: {}", path);
        return path;
    }
}
