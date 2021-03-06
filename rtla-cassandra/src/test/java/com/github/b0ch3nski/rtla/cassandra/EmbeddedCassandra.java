package com.github.b0ch3nski.rtla.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import com.datastax.driver.core.exceptions.DriverException;
import com.github.b0ch3nski.rtla.cassandra.CassandraConfig.CassandraConfigBuilder;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * @author bochen
 */
public final class EmbeddedCassandra {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedCassandra.class);
    private static CassandraConfig config;

    private EmbeddedCassandra() { }

    public static void start() {
        try {
            LOGGER.debug("Starting embedded Cassandra...");
            EmbeddedCassandraServerHelper.startEmbeddedCassandra(300000);
            LOGGER.debug("Embedded Cassandra started!");
            new SchemaManager().importSchema();
        } catch (InterruptedException | IOException | TTransportException | ConfigurationException e) {
            throw new EmbeddedCassandraException("Couldn't start embedded Cassandra", e);
        }
    }

    public static CassandraConfig getConfig() {
        if (config == null) {
            config = new CassandraConfigBuilder()
                    .withHost(EmbeddedCassandraServerHelper.getHost())
                    .withPort(EmbeddedCassandraServerHelper.getNativeTransportPort())
                    .withBatchSize(32)
                    .withFlushTime(5)
                    .withTtl(3600L)
                    .build();
        }
        return config;
    }

    private static class SchemaManager {
        private final Cluster cluster;
        private final Session session;

        public SchemaManager() {
            cluster = Cluster.builder()
                    .addContactPoint(EmbeddedCassandraServerHelper.getHost())
                    .withPort(EmbeddedCassandraServerHelper.getNativeTransportPort())
                    .build();
            session = cluster.connect();
        }

        private void shutdown() {
            if (session != null) session.close();
            if (cluster != null) cluster.close();
        }

        private void execute(String statement) {
            try {
                session.execute(statement);
            } catch (AlreadyExistsException ignored) {
                // ignore
            } catch (DriverException e) {
                throw new EmbeddedCassandraException("Couldn't execute statement [" + statement + "]", e);
            }
        }

        private List<String> parseSchema(String schema) {
            return Lists.newArrayList(schema.split("\\s*;\\n"));
        }

        private void executeSchema(List<String> statementList) {
            LOGGER.trace("Executing schema statements\n{}", Joiner.on("\n").join(statementList));
            for (String statement : statementList) {
                execute(statement + ";");
            }
        }

        private String loadSchema() {
            try {
                return Resources.toString(Resources.getResource("schema.cql"), Charsets.UTF_8);
            } catch (IOException e) {
                throw new EmbeddedCassandraException("Couldn't load schema from resources", e);
            }
        }

        public void importSchema() {
            LOGGER.debug("Starting schema import...");
            executeSchema(parseSchema(loadSchema()));
            shutdown();
            LOGGER.debug("Schema import done!");
        }
    }

    private static class EmbeddedCassandraException extends RuntimeException {
        public EmbeddedCassandraException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
