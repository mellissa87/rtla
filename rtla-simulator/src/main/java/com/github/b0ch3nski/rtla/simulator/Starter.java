package com.github.b0ch3nski.rtla.simulator;

import com.github.b0ch3nski.rtla.common.utils.ConfigFactory;
import com.github.b0ch3nski.rtla.simulator.utils.LogReader;
import com.github.b0ch3nski.rtla.simulator.utils.SimulatorConfig;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;

/**
 * @author bochen
 */
public final class Starter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Starter.class);

    public static void main(String... args) throws IOException {
        Preconditions.checkArgument(args.length == 1, "Usage: java -jar rtla-simulator-1.0.0-shaded.jar <config file path>");

        SimulatorConfig config = (SimulatorConfig) ConfigFactory.fromYaml(new FileReader(args[0]), SimulatorConfig.class);
        int loops = config.getLoops();

        LogReader reader = new LogReader(LOGGER, config.getInputDir(), config.getDelay());
        for (int i = 0; (loops == 0) || (i < loops); i++) {
            reader.iterateOverAllFiles();
        }
    }
}
