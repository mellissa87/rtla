package com.github.b0ch3nski.rtla.kafka;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bochen
 */
public final class ObjectHashcodePartitioner implements Partitioner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectHashcodePartitioner.class);

    public ObjectHashcodePartitioner(VerifiableProperties properties) {
        if (properties != null) LOGGER.trace("Creating partitioner with properties: {}", properties);
    }

    @Override
    public int partition(Object key, int numPartitions) {
        int partition = ((key.hashCode() % numPartitions) + numPartitions) % numPartitions;
        LOGGER.trace("Key {} goes to partition {}", key, partition);
        return partition;
    }
}
