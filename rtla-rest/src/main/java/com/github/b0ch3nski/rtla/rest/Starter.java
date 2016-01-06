package com.github.b0ch3nski.rtla.rest;

import com.github.b0ch3nski.rtla.common.utils.ConfigFactory;
import com.github.b0ch3nski.rtla.rest.utils.RestConfig;
import com.google.common.base.Preconditions;

import java.io.FileReader;
import java.io.IOException;

/**
 * @author bochen
 */
public final class Starter {

    private Starter() { }

    public static void main(String... args) throws IOException, InterruptedException {
        Preconditions.checkArgument(args.length == 1, "Usage: java -jar rtla-rest-1.0.0-shaded.jar <config file path>");
        RestConfig config = (RestConfig) ConfigFactory.fromYaml(new FileReader(args[0]), RestConfig.class);

        RestServer server = new RestServer(config);
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown, "shutdownHook"));
        Thread.currentThread().join();
    }
}
