package com.github.b0ch3nski.rtla.simulator;

import com.github.b0ch3nski.rtla.common.utils.ConfigFactory;
import com.github.b0ch3nski.rtla.simulator.utils.*;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author bochen
 */
public final class Starter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Starter.class);

    public static void main(String... args) throws IOException {
        Preconditions.checkArgument(args.length == 1, "Usage: java -jar rtla-simulator-1.0.0-shaded.jar <config file path>");

        SimulatorConfig config = (SimulatorConfig) ConfigFactory.fromYaml(new FileReader(args[0]), SimulatorConfig.class);
        List<Path> allFiles = FileHandler.listAllFiles(config.getInputDir());
//        LOGGER.debug("Config read = {} | Files read = {}", config, allFiles.size());

        LogReader reader = new LogReader(LOGGER, config.getDelay());
        for (Path singleFile : allFiles) {
//            LOGGER.debug("Reading file = {}", singleFile);
            Files.lines(singleFile).forEach(reader:: handleSingleLine);
//            LOGGER.debug("End of file = {}", singleFile);
        }
    }
}
