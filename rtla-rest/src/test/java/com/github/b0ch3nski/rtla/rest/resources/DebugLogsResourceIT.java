package com.github.b0ch3nski.rtla.rest.resources;

import static com.github.b0ch3nski.rtla.cassandra.CassandraTable.DEBUG;

/**
 * @author bochen
 */
public final class DebugLogsResourceIT extends LogsResourceIT {

    public DebugLogsResourceIT() {
        super(DEBUG);
    }
}
