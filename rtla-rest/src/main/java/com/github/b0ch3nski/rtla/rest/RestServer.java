package com.github.b0ch3nski.rtla.rest;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;
import com.github.b0ch3nski.rtla.cassandra.CassandraConfig.CassandraConfigBuilder;
import com.github.b0ch3nski.rtla.cassandra.dao.SimplifiedLogCassDao;
import com.github.b0ch3nski.rtla.cassandra.dao.SimplifiedLogCassDaoFactory;
import com.github.b0ch3nski.rtla.common.serialization.SerializationHandler;
import com.github.b0ch3nski.rtla.common.utils.Validators;
import com.github.b0ch3nski.rtla.rest.utils.*;
import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.CompressionConfig.CompressionMode;
import org.glassfish.grizzly.http.server.*;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bochen
 */
public final class RestServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestServer.class);
    private static final HttpServer SERVER = new HttpServer();
    private static final String KEYSTORE = "keystore.jks";
    private final RestConfig config;

    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    public RestServer(RestConfig config) {
        Validators.isNotNull(config, "config");
        this.config = config;

        CassandraConfig cassandraConfig = new CassandraConfigBuilder()
                .withHost(config.getCassandraHost())
                .withPort(config.getCassandraPort())
                .build();
        ResourceConfig resourceConfig = new ResourceConfig();

        createListener();
        createDaos(cassandraConfig, resourceConfig);
        configureErrorHandling(resourceConfig);
        configureSerialization(resourceConfig);
        configureHttpServer(createHandlers(resourceConfig));
    }

    public void start() throws IOException {
        LOGGER.info("Starting REST service with config = {}", config);
        SERVER.start();
    }

    private void createListener() {
        NetworkListener listener = new NetworkListener("mainListener", config.getServerHost(), config.getServerPort());
        SERVER.addListener(listener);
    }

    private void createDaos(CassandraConfig cassandraConfig, ResourceConfig resourceConfig) {
        Map<String, SimplifiedLogCassDao> daos = SimplifiedLogCassDaoFactory.createAllDaos(cassandraConfig);
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(daos).to(new TypeLiteral<Map<String, SimplifiedLogCassDao>>() {});
            }
        });
    }

    private void configureErrorHandling(ResourceConfig resourceConfig) {
        resourceConfig.register(GenericExceptionMapper.class);
        resourceConfig.register(RequiredParamFilter.class);
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

        config.packages(getClass().getPackage() + ".resources");
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
        CompressionConfig compressionConfig = listener.getCompressionConfig();
        compressionConfig.setCompressionMode(CompressionMode.ON);
        compressionConfig.setCompressionMinSize(1);
        compressionConfig.setCompressableMimeTypes("text/plain", "text/html", "text/css", "text/xml",
                "application/xhtml+xml", "application/json", "application/javascript", "application/xml");
    }

    public void shutdown() {
        SERVER.shutdown();
        LOGGER.info("REST service has been shut down!");
    }
}
