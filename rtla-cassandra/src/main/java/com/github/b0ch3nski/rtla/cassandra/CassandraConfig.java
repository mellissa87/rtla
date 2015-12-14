package com.github.b0ch3nski.rtla.cassandra;

import com.github.b0ch3nski.rtla.common.utils.Validators;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import java.util.Map;
import java.util.Objects;

/**
 * @author bochen
 */
public final class CassandraConfig {
    private final String host;
    private final int port;
    private final int batchSize;
    private final int flushTime;
    private final long ttl;

    private CassandraConfig(CassandraConfigBuilder builder) {
        host = builder.host;
        port = builder.port;
        batchSize = builder.batchSize;
        flushTime = builder.flushTime;
        ttl = builder.ttl;
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

    public long getTtl() {
        return ttl;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("host", host)
                .add("port", port)
                .add("batchSize", batchSize)
                .add("flushTime", flushTime)
                .add("ttl", ttl)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;
        CassandraConfig config = (CassandraConfig) o;
        return (port == config.port) &&
                (batchSize == config.batchSize) &&
                (flushTime == config.flushTime) &&
                (ttl == config.ttl) &&
                (host.equals(config.host));
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, batchSize, flushTime, ttl);
    }

    public static final class CassandraConfigBuilder {
        private String host;
        private int port;
        private int batchSize;
        private int flushTime;
        private long ttl;

        public CassandraConfigBuilder fromStormConf(Map stormConf) {
            Map config = (Map) stormConf.get("cassandra.config");

            host = (String) config.get("cassandra.host");
            port = ((Long) config.get("cassandra.port")).intValue();
            batchSize = ((Long) config.get("cassandra.batch.size")).intValue();
            flushTime = ((Long) config.get("cassandra.flush.time")).intValue();
            ttl = (Long) config.get("cassandra.ttl");
            return this;
        }

        public CassandraConfigBuilder withHost(String host) {
            this.host = host;
            return this;
        }

        public CassandraConfigBuilder withPort(int port) {
            this.port = port;
            return this;
        }

        public CassandraConfigBuilder withBatchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public CassandraConfigBuilder withFlushTime(int flushTime) {
            this.flushTime = flushTime;
            return this;
        }

        public CassandraConfigBuilder withTtl(long ttl) {
            this.ttl = ttl;
            return this;
        }

        public CassandraConfig build() {
            Validators.isNotNullOrEmpty(host, "host");
            Preconditions.checkArgument((port > 1024) && (port < 65535), "port must be > 1024 and < 65535");
            Preconditions.checkArgument(batchSize >= 0, "batchSize must be >= 0");
            Preconditions.checkArgument(flushTime >= 0, "flushTime must be >= 0");
            Preconditions.checkArgument(ttl >= 60, "TTL must be >= 60 sec");
            return new CassandraConfig(this);
        }
    }
}
