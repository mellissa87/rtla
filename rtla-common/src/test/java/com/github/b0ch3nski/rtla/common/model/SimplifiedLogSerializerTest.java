package com.github.b0ch3nski.rtla.common.model;

import com.github.b0ch3nski.rtla.common.utils.RandomLogFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        LOGGER.debug("Serialized: {}", serialized);

        SimplifiedLog deserialized = SERIALIZER.fromBytes(serialized);
        LOGGER.debug("Deserialized: {}", deserialized);

        assertThat(deserialized, is(toSerialize));
    }
}
