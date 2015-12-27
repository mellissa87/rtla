package com.github.b0ch3nski.rtla.rest;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.github.b0ch3nski.rtla.common.serialization.SerializationHandler;
import com.github.b0ch3nski.rtla.common.utils.ConfigFactory;
import com.github.b0ch3nski.rtla.common.utils.Validators;
import com.github.b0ch3nski.rtla.rest.utils.RestConfig;
import com.google.common.base.Preconditions;
import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.CompressionConfig.CompressionMode;
import org.glassfish.grizzly.http.server.*;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bochen
 */
public final class Starter {
    private static final Logger LOGGER = LoggerFactory.getLogger(Starter.class);
    private static final HttpServer SERVER = new HttpServer();
    private static final String KEYSTORE = "keystore.jks";
    private final RestConfig config;

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private Starter(RestConfig config) {
        Validators.isNotNull(config, "config");
        this.config = config;

        createListener();
        ResourceConfig resourceConfig = new ResourceConfig();
        configureSerialization(resourceConfig);
        configureHttpServer(createHandlers(resourceConfig));
    }

    private void start() throws IOException {
        LOGGER.debug("Starting REST service with config = {}", config);
        SERVER.start();
    }

    private void createListener() {
        NetworkListener listener = new NetworkListener("mainListener", config.getServerHost(), config.getServerPort());
        SERVER.addListener(listener);
    }

    private void configureSerialization(ResourceConfig resourceConfig) {
        JacksonJaxbJsonProvider jsonProvider = new JacksonJaxbJsonProvider();
        jsonProvider.setMapper(SerializationHandler.createAndConfigureMapper());
        resourceConfig.register(jsonProvider);
    }

    private Map<HttpHandler, String> createHandlers(ResourceConfig config) {
        Map<HttpHandler, String> handlers = new HashMap<>();
        CLStaticHttpHandler webHandler = new CLStaticHttpHandler(getClass().getClassLoader(), "webapp/");
        handlers.put(webHandler, "/");

        config.packages(getClass().getPackage() + ".resource");
        HttpHandler apiHandler = ContainerFactory.createContainer(HttpHandler.class, config);
        handlers.put(apiHandler, "/api");
        return handlers;
    }

    private void configureHttpServer(Map<HttpHandler, String> handlers) {
        ServerConfiguration serverCfg = SERVER.getServerConfiguration();
        handlers.forEach(serverCfg::addHttpHandler);

        SERVER.getListeners().forEach(listener -> {
            if (config.isSSLEnabled()) enableSSL(listener);
            enableCompression(listener);
        });
    }

    private void enableSSL(NetworkListener listener) {
        URL resource = getClass().getClassLoader().getResource(KEYSTORE);
        String keyStore = Validators.isNotNull(resource, "resource").getFile();

        SSLContextConfigurator sslConfig = new SSLContextConfigurator();
        sslConfig.setKeyStoreFile(keyStore);
        sslConfig.setKeyPass(config.getSslPassword());

        listener.setSecure(true);
        listener.setSSLEngineConfig(new SSLEngineConfigurator(sslConfig, false, false, false));
    }

    private void enableCompression(NetworkListener listener) {
        CompressionConfig compCfg = listener.getCompressionConfig();
        compCfg.setCompressionMode(CompressionMode.ON);
        compCfg.setCompressionMinSize(1);
        compCfg.setCompressableMimeTypes("text/plain", "text/html", "text/css", "text/xml", "application/xhtml+xml",
                "application/json", "application/javascript", "application/xml");
    }

    private void shutdown() {
        SERVER.shutdown();
        LOGGER.debug("REST service has been shut down!");
    }

    public static void main(String... args) throws IOException, InterruptedException {
        Preconditions.checkArgument(args.length == 1, "Usage: java -jar rtla-rest-1.0.0-shaded.jar <config file path>");
        RestConfig config = (RestConfig) ConfigFactory.fromYaml(new FileReader(args[0]), RestConfig.class);

        Starter starter = new Starter(config);
        starter.start();
        Runtime.getRuntime().addShutdownHook(new Thread(starter::shutdown, "shutdownHook"));
        Thread.currentThread().join();
    }
}
