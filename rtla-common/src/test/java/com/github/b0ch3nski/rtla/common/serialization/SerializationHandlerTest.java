package com.github.b0ch3nski.rtla.common.serialization;

import com.github.b0ch3nski.rtla.common.model.*;
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
public class SerializationHandlerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerializationHandlerTest.class);
    private static final SimplifiedLog TO_SERIALIZE;

    static {
        TO_SERIALIZE = RandomLogFactory.create();
        LOGGER.debug("Original object: {}", TO_SERIALIZE);
    }

    private <T> void checkObjectsEquality(T received, T expected) {
        LOGGER.debug("Deserialized object: {}", received);
        assertThat(received, is(expected));
    }

    private <T extends SerializableByKryo> void serializeAndDeserializeUsingKryo(T toSerialize, Class<T> cls) {
        byte[] serialized = SerializationHandler.toBytesUsingKryo(toSerialize);
        LOGGER.debug("Serialized {} using Kryo: {} | Size: {}",
                cls.getSimpleName(), DatatypeConverter.printHexBinary(serialized), serialized.length);

        T deserialized = SerializationHandler.fromBytesUsingKryo(serialized, cls);

        checkObjectsEquality(deserialized, toSerialize);
    }

    @Test
    public void shouldSerializeAndDeserializeUsingKryo() {
        SimplifiedLogFrame frame = new SimplifiedLogFrame(TO_SERIALIZE);
        LOGGER.debug("Created SimplifiedLogFrame object: {}", frame);

        assertThat(frame.getTimeStamp(), is(TO_SERIALIZE.getTimeStamp()));
        assertThat(frame.getHostName(), is(TO_SERIALIZE.getHostName()));
        assertThat(frame.getLevel(), is(TO_SERIALIZE.getLevel()));
        serializeAndDeserializeUsingKryo(frame, SimplifiedLogFrame.class);
        serializeAndDeserializeUsingKryo(TO_SERIALIZE, SimplifiedLog.class);
    }

    @Test
    public void shouldSerializeAndDeserializeUsingJackson() throws IOException {
        byte[] serialized = SerializationHandler.getJsonFromObject(TO_SERIALIZE);
        LOGGER.debug("Serialized using Jackson: {} | Size: {}", new String(serialized), serialized.length);

        SimplifiedLog deserialized = SerializationHandler.getObjectFromJson(serialized, SimplifiedLog.class);

        checkObjectsEquality(deserialized, TO_SERIALIZE);
    }
}
