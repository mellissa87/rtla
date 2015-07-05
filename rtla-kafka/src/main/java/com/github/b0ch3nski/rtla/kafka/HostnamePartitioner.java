package com.github.b0ch3nski.rtla.kafka;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bochen
 */
public final class HostnamePartitioner implements Partitioner {
    private static final Logger LOGGER = LoggerFactory.getLogger(HostnamePartitioner.class);

    public HostnamePartitioner(VerifiableProperties properties) {
        if (properties != null) {
            LOGGER.debug("Creating partitioner with properties: {}", properties);
        }
    }

    @Override
    public int partition(Object key, int numPartitions) {
        int partition = key.hashCode() % numPartitions;
        LOGGER.trace("Key {} goes to partition {}", key, partition);
        return partition;
    }
}
