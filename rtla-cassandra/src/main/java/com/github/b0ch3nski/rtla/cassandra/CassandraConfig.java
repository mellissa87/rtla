package com.github.b0ch3nski.rtla.cassandra;

import com.github.b0ch3nski.rtla.common.utils.Validators;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import java.util.Map;

/**
 * @author bochen
 */
public final class CassandraConfig {
    private final String host;
    private final int port;
    private final int batchSize;
    private final int flushTime;

    private CassandraConfig(CassandraConfigBuilder builder) {
        host = builder.host;
        port = builder.port;
        batchSize = builder.batchSize;
        flushTime = builder.flushTime;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getFlushTime() {
        return flushTime;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("host", host)
                .add("port", port)
                .add("batchSize", batchSize)
                .add("flushTime", flushTime)
                .toString();
    }

    public static final class CassandraConfigBuilder {
        private String host;
        private int port;
        private int batchSize;
        private int flushTime;

        public CassandraConfigBuilder fromStormConf(Map stormConf) {
            Map config = (Map) stormConf.get("cassandra.config");

            withHost((String) config.get("cassandra.host"));
            withPort(((Long) config.get("cassandra.port")).intValue());
            withBatchSize(((Long) config.get("cassandra.batch.size")).intValue());
            withFlushTime(((Long) config.get("cassandra.flush.time")).intValue());
            return this;
        }

        public CassandraConfigBuilder withHost(String host) {
            Validators.isNotNullOrEmpty(host, "host");
            this.host = host;
            return this;
        }

        public CassandraConfigBuilder withPort(int port) {
            Preconditions.checkArgument(((port > 1024) && (port < 65535)), "port must be > 1024 and < 65535");
            this.port = port;
            return this;
        }

        public CassandraConfigBuilder withBatchSize(int batchSize) {
            Preconditions.checkArgument((batchSize >= 0), "batchSize must be >= 0");
            this.batchSize = batchSize;
            return this;
        }

        public CassandraConfigBuilder withFlushTime(int flushTime) {
            Preconditions.checkArgument((flushTime >= 0), "flushTime must be >= 0");
            this.flushTime = flushTime;
            return this;
        }

        public CassandraConfig build() {
            return new CassandraConfig(this);
        }
    }
}
