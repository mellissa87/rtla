package com.github.b0ch3nski.rtla.cassandra;

import com.google.common.base.*;

/**
 * @author bochen
 */
public final class CassandraConfig {
    private final String host;
    private final int port;
    private final String logsKeyspace;
    private final int logsTTL;
    private final boolean isAsync;
    private final int batchSize;

    private CassandraConfig(CassandraConfigBuilder builder) {
        host = builder.host;
        port = builder.port;
        logsKeyspace = builder.logsKeyspace;
        logsTTL = builder.logsTTL;
        isAsync = builder.isAsync;
        batchSize = builder.batchSize;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getLogsKeyspace() {
        return logsKeyspace;
    }

    public int getLogsTTL() {
        return logsTTL;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public int getBatchSize() {
        return batchSize;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("host", host)
                .add("port", port)
                .add("logsKeyspace", logsKeyspace)
                .add("logsTTL", logsTTL)
                .add("isAsync", isAsync)
                .add("batchSize", batchSize)
                .toString();
    }

    public static final class CassandraConfigBuilder {
        private String host;
        private int port;
        private String logsKeyspace;
        private int logsTTL;
        private boolean isAsync;
        private int batchSize;

        public CassandraConfigBuilder withHost(String host) {
            this.host = host;
            return this;
        }

        public CassandraConfigBuilder withPort(int port) {
            Preconditions.checkArgument(((port > 1024) && (port < 65535)), "port must be > 1024 and < 65535");
            this.port = port;
            return this;
        }

        public CassandraConfigBuilder withLogsKeyspace(String logsKeyspace) {
            this.logsKeyspace = logsKeyspace;
            return this;
        }

        public CassandraConfigBuilder withLogsTTL(int logsTTL) {
            Preconditions.checkArgument((logsTTL >= 0), "logsTTL must be >= 0");
            this.logsTTL = logsTTL;
            return this;
        }

        public CassandraConfigBuilder withAsync(boolean isAsync) {
            this.isAsync = isAsync;
            return this;
        }

        public CassandraConfigBuilder withBatchSize(int batchSize) {
            Preconditions.checkArgument((batchSize >= 0), "batchSize must be >= 0");
            this.batchSize = batchSize;
            return this;
        }

        private void validate(String toValidate, String varName) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(toValidate), varName + " cannot be null or empty!");
        }

        public CassandraConfig build() {
            validate(host, "host");
            validate(logsKeyspace, "logsKeyspace");
            return new CassandraConfig(this);
        }
    }
}
