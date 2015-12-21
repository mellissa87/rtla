package com.github.b0ch3nski.rtla.cassandra.dao;

import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;
import com.github.b0ch3nski.rtla.cassandra.CassandraTable;
import com.github.b0ch3nski.rtla.common.utils.Validators;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import java.util.EnumSet;
import java.util.Map;

/**
 * @author bochen
 */
public final class SimplifiedLogCassDaoFactory {

    private SimplifiedLogCassDaoFactory() { }

    public static SimplifiedLogCassDao createDaoForLevel(CassandraConfig config, CassandraTable table) {
        Validators.isNotNull(config, "Cassandra config");
        Validators.isNotNull(table, "Cassandra table");
        return new SimplifiedLogCassDao(config, table);
    }

    public static Map<String, SimplifiedLogCassDao> createAllDaos(CassandraConfig config) {
        Builder<String, SimplifiedLogCassDao> builder = ImmutableMap.builder();
        EnumSet.allOf(CassandraTable.class).forEach(table -> builder.put(table.name(), createDaoForLevel(config, table)));
        return builder.build();
    }
}
