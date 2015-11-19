package com.github.b0ch3nski.rtla.common.serialization;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.RandomLogFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author bochen
 */
public class SimplifiedLogSerializerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifiedLogSerializerTest.class);
    private static final SimplifiedLogSerializer SERIALIZER = new SimplifiedLogSerializer();

    @Test
    public void shouldSerializeAndDeserialize() {
        SimplifiedLog toSerialize = RandomLogFactory.create();
        LOGGER.debug("Original: {}", toSerialize);

        byte[] serialized = SERIALIZER.toBytes(toSerialize);
        LOGGER.debug("Serialized: {}", DatatypeConverter.printHexBinary(serialized));

        SimplifiedLog deserialized = SERIALIZER.fromBytes(serialized);
        LOGGER.debug("Deserialized: {}", deserialized);

        assertThat(deserialized, is(toSerialize));
    }

    @Test
    public void shouldSerializeAndDeserializeList() {
        List<SimplifiedLog> toSerialize = RandomLogFactory.create(5);
        LOGGER.debug("Original: {}", toSerialize);

        byte[] serialized = SERIALIZER.listToBytes(toSerialize);
        LOGGER.debug("Serialized: {}", DatatypeConverter.printHexBinary(serialized));

        List<SimplifiedLog> deserialized = SERIALIZER.listFromBytes(serialized);
        LOGGER.debug("Deserialized: {}", deserialized);

        assertThat(deserialized, is(toSerialize));
    }
}