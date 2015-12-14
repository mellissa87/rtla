package com.github.b0ch3nski.rtla.common.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.b0ch3nski.rtla.common.model.*;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author bochen
 */
public final class SerializationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerializationHandler.class);
    private static final ObjectMapper MAPPER = createAndConfigureMapper();
    private static final ThreadLocal<Kryo> KRYO = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            return createAndConfigureKryo();
        }
    };

    private SerializationHandler() { }

    private static ObjectMapper createAndConfigureMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(SimplifiedLog.class, new SimplifiedLogJacksonDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    private static Kryo createAndConfigureKryo() {
        Kryo kryo = new Kryo();
        kryo.register(SimplifiedLog.class, new SimplifiedLogKryoSerializer());
        kryo.register(SimplifiedLogFrame.class, new SimplifiedLogFrameKryoSerializer());
        return kryo;
    }

    public static <T> byte[] toBytesUsingKryo(T toSerialize) {
        if (!(toSerialize instanceof SerializableByKryo))
            throw new IllegalArgumentException("Found object of class: " + toSerialize.getClass().getSimpleName()
                    + " | Only SerializableByKryo objects are possible to serialize using this handler");
        return toBytesWithCalculatedBufferSize((SerializableByKryo) toSerialize);
    }

    private static <T> byte[] writeObjectToKryo(Output output, T toSerialize) {
        KRYO.get().writeObject(output, toSerialize);
        output.close();
        return output.toBytes();
    }

    @VisibleForTesting
    protected static byte[] toBytesWithCalculatedBufferSize(SerializableByKryo toSerialize) {
        int objectSize = toSerialize.getObjectSizeInBytes();
        LOGGER.trace("Calculated size of object of class {} is {}", toSerialize.getClass().getSimpleName(), objectSize);

        Output output = new Output(objectSize);
        return writeObjectToKryo(output, toSerialize);
    }

    private static Output createOutputWithDefinedBufferSize(int kryoBufferSize, int kryoMaxBufferSize) {
        if (kryoMaxBufferSize == -1) return new Output(kryoBufferSize, -1);
        if (kryoBufferSize > kryoMaxBufferSize) return new Output(kryoBufferSize, kryoBufferSize);
        return new Output(kryoBufferSize, kryoMaxBufferSize);
    }

    @VisibleForTesting
    protected static <T> byte[] toBytesWithDefinedBufferSize(T toSerialize, int kryoBufferSize, int kryoMaxBufferSize) {
        Output output = createOutputWithDefinedBufferSize(kryoBufferSize, kryoMaxBufferSize);
        return writeObjectToKryo(output, toSerialize);
    }

    @VisibleForTesting
    protected static <T> byte[] toBytesWithOutputStream(T toSerialize) {
        Output output = new Output(new ByteArrayOutputStream());
        return writeObjectToKryo(output, toSerialize);
    }

    public static <T> T fromBytesUsingKryo(byte[] toDeserialize, Class<T> cls) {
        Input input = new Input(toDeserialize);
        T deserialized = KRYO.get().readObject(input, cls);
        input.close();
        return deserialized;
    }

    public static <T> byte[] getJsonFromObject(T toJson) {
        try {
            return MAPPER.writeValueAsBytes(toJson);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Couldn't serialize object of type " + toJson.getClass().getSimpleName() + " to JSON", e);
        }
    }

    public static <T> T getObjectFromJson(byte[] json, Class<T> cls) {
        try {
            return MAPPER.readValue(json, cls);
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't deserialize JSON to object of type " + cls.getSimpleName(), e);
        }
    }
}
