package com.github.b0ch3nski.rtla.cassandra;

import com.google.common.base.Joiner;

/**
 * @author bochen
 */
public enum CassandraTable {
    ERROR("rtla", "errorlogs"),
    WARN("rtla", "warnlogs"),
    INFO("rtla", "infologs"),
    DEBUG("rtla", "debuglogs"),
    TRACE("rtla", "tracelogs");

    private final String keyspaceName;
    private final String tableName;

    CassandraTable(String keyspaceName, String tableName) {
        this.keyspaceName = keyspaceName;
        this.tableName = tableName;
    }

    public String getKeyspaceAndTable() {
        return keyspaceName + "." + tableName;
    }

    public String getInsertQuery(String[] columns, long timeToLive) {
        StringBuilder builder = new StringBuilder();

        builder.append("INSERT INTO ");
        builder.append(getKeyspaceAndTable());
        builder.append(" (");
        builder.append(Joiner.on(", ").join(columns));
        builder.append(") VALUES (");
        for (int i = 0; i < columns.length; i++) {
            builder.append((i < (columns.length - 1)) ? "?, " : "?");
        }
        builder.append(") USING TTL ");
        builder.append(timeToLive);
        builder.append(";");

        return builder.toString();
    }

    public String getCountQuery() {
        return "SELECT COUNT(*) FROM " + getKeyspaceAndTable() + ";";
    }

    public String getTruncateQuery() {
        return "TRUNCATE " + getKeyspaceAndTable() + ";";
    }
}
