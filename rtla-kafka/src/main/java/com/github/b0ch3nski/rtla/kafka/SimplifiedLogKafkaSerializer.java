package com.github.b0ch3nski.rtla.kafka;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLogFrame;
import com.github.b0ch3nski.rtla.common.serialization.SerializationHandler;
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

    public SimplifiedLogKafkaSerializer(VerifiableProperties properties) {
        if (properties != null) LOGGER.trace("Creating serializer with properties: {}", properties);
    }

    @Override
    public byte[] toBytes(SimplifiedLog toSerialize) {
        SimplifiedLogFrame frame = new SimplifiedLogFrame(toSerialize);
        return SerializationHandler.toBytesUsingKryo(frame);
    }

    @Override
    public SimplifiedLog fromBytes(byte[] toDeserialize) {
        SimplifiedLogFrame frame = SerializationHandler.fromBytesUsingKryo(toDeserialize, SimplifiedLogFrame.class);
        byte[] serializedLog = frame.getSimplifiedLog();
        return SerializationHandler.fromBytesUsingKryo(serializedLog, SimplifiedLog.class);
    }
}
