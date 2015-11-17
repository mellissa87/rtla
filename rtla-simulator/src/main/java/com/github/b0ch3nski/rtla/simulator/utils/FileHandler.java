package com.github.b0ch3nski.rtla.simulator.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * @author bochen
 */
public final class FileHandler {

    private static boolean isFileValid(File file) {
        return file.isFile() && file.getPath().endsWith(".log");
    }

    private static boolean isArrayNullOrEmpty(Object[] array) {
        return (array == null) || (array.length < 1);
    }

    public static List<Path> listAllFiles(String path) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(path), "Path cannot be null or empty!");
        Builder<Path> builder = ImmutableList.builder();
        File[] files = new File(path).listFiles();

        if (!isArrayNullOrEmpty(files)) {
            Lists.newArrayList(files)
                    .stream()
                    .sorted()
                    .filter(FileHandler:: isFileValid)
                    .forEach(file -> builder.add(file.toPath()));
        }
        return builder.build();
    }
}
