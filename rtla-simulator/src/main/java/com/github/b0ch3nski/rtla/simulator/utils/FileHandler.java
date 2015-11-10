package com.github.b0ch3nski.rtla.simulator.utils;

import com.google.common.collect.Lists;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * @author bochen
 */
public final class FileHandler {

    private static boolean isExtensionValid(String fileName) {
        return fileName.endsWith(".log");
    }

    private static boolean isArrayNullOrEmpty(Object[] array) {
        return ((array == null) || (array.length < 1));
    }

    public static List<String> listAllFiles(File directory) {
        List<String> toReturn = new ArrayList<>();
        File[] files = directory.listFiles();

        if (!isArrayNullOrEmpty(files)) {
            List<File> filesAsList = Lists.newArrayList(files);
            Collections.sort(filesAsList);

            for (File file : filesAsList) {
                String fileName = file.getPath();
                if (isExtensionValid(fileName)) toReturn.add(fileName);
            }
        }

        return toReturn;
    }

    public static RandomAccessFile openFile(String fileName, String mode) throws IOException {
        Path filePath = Paths.get(fileName);

        if (mode.equals("rw") && !Files.exists(filePath)) {
            Files.createFile(filePath);
        }
        return new RandomAccessFile(filePath.toFile(), mode);
    }
}
