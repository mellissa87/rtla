package com.github.b0ch3nski.rtla.common.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.RandomLogFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author bochen
 */
public class SimplifiedLogSerializerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifiedLogSerializerTest.class);
    private static final SimplifiedLogSerializer SERIALIZER = new SimplifiedLogSerializer();
    private static final ObjectMapper MAPPER = JsonMapperFactory.getForSimplifiedLog();
    private static final SimplifiedLog TO_SERIALIZE;

    static {
        TO_SERIALIZE = RandomLogFactory.create();
        LOGGER.debug("Original object: {}", TO_SERIALIZE);
    }

    private void checkObjectsEquality(SimplifiedLog deserialized) {
        LOGGER.debug("Deserialized object: {}", deserialized);

        assertThat(deserialized, is(TO_SERIALIZE));
    }

    @Test
    public void shouldSerializeAndDeserializeUsingKryo() {

        byte[] serialized = SERIALIZER.toBytes(TO_SERIALIZE);
        LOGGER.debug("Serialized using Kryo: {} | Size: {}", DatatypeConverter.printHexBinary(serialized), serialized.length);

        SimplifiedLog deserialized = SERIALIZER.fromBytes(serialized);

        checkObjectsEquality(deserialized);
    }

    @Test
    public void shouldSerializeAndDeserializeUsingJackson() throws IOException {

        byte[] serialized = MAPPER.writeValueAsBytes(TO_SERIALIZE);
        LOGGER.debug("Serialized using Jackson: {} | Size: {}", new String(serialized), serialized.length);

        SimplifiedLog deserialized = MAPPER.readValue(serialized, SimplifiedLog.class);

        checkObjectsEquality(deserialized);
    }
}
