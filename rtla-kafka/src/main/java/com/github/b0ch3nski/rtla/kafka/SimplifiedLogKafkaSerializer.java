package com.github.b0ch3nski.rtla.kafka;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLogSerializer;
import kafka.serializer.Decoder;
import kafka.serializer.Encoder;
import kafka.utils.VerifiableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bochen
 */
public final class SimplifiedLogKafkaSerializer implements Encoder<SimplifiedLog>, Decoder<SimplifiedLog> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifiedLogKafkaSerializer.class);

    private static final SimplifiedLogSerializer SERIALIZER = new SimplifiedLogSerializer();


    public SimplifiedLogKafkaSerializer(VerifiableProperties properties) {
        if (properties != null) {
            LOGGER.debug("Creating serializer with properties: {}", properties);
        }
    }

    @Override
    public byte[] toBytes(SimplifiedLog toSerialize) {
        return SERIALIZER.toBytes(toSerialize);
    }

    @Override
    public SimplifiedLog fromBytes(byte[] toDeserialize) {
        return SERIALIZER.fromBytes(toDeserialize);
    }
}
