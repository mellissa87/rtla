package com.github.b0ch3nski.rtla.common.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * @author bochen
 */
public final class SimplifiedLogSerializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifiedLogSerializer.class);
    private final ThreadLocal<Kryo> kryo = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            return createAndConfigureKryo();
        }
    };

    private Kryo createAndConfigureKryo() {
        Kryo kryo = new Kryo();
        kryo.register(SimplifiedLog.class, new SimplifiedLogKryoSerializer());
        kryo.register(ImmutableList.class, new ImmutableListSerializer());
        return kryo;
    }

    public byte[] toBytes(SimplifiedLog toSerialize) {
        return toBytesWithCalculatedBufferSize(toSerialize);
    }

    public byte[] listToBytes(List<SimplifiedLog> toSerialize) {
        return listToBytesWithCalculatedBufferSize(toSerialize);
    }

    private int getObjectSizeInBytes(SimplifiedLog toSerialize) {
        int size = 16 // timestamp and serialuid are longs, so 2 * 8
                    + toSerialize.getHostName().length()
                    + toSerialize.getLevel().length()
                    + toSerialize.getThreadName().length()
                    + toSerialize.getLoggerName().length()
                    + toSerialize.getFormattedMessage().length();

        LOGGER.trace("Calculated size of object = {}", size);
        return size;
    }

    @VisibleForTesting
    byte[] toBytesWithCalculatedBufferSize(SimplifiedLog toSerialize) {
        Output output = new Output(getObjectSizeInBytes(toSerialize));
        kryo.get().writeObject(output, toSerialize);
        output.close();
        return output.toBytes();
    }

    @VisibleForTesting
    byte[] listToBytesWithCalculatedBufferSize(List<SimplifiedLog> toSerialize) {
        int outputSize = toSerialize.size() * (getObjectSizeInBytes(toSerialize.get(0)) + 32);
        Output output = new Output(outputSize);
        kryo.get().writeObject(output, toSerialize);
        output.close();
        return output.toBytes();
    }

    private Output createOutputWithDefinedBufferSize(int kryoBufferSize, int kryoMaxBufferSize) {
        if (kryoMaxBufferSize == -1) return new Output(kryoBufferSize, -1);
        if (kryoBufferSize > kryoMaxBufferSize) return new Output(kryoBufferSize, kryoBufferSize);
        return new Output(kryoBufferSize, kryoMaxBufferSize);
    }

    @VisibleForTesting
    byte[] toBytesWithDefinedBufferSize(SimplifiedLog toSerialize, int kryoBufferSize, int kryoMaxBufferSize) {
        Output output = createOutputWithDefinedBufferSize(kryoBufferSize, kryoMaxBufferSize);
        kryo.get().writeObject(output, toSerialize);
        output.close();
        return output.toBytes();
    }

    @VisibleForTesting
    byte[] toBytesWithOutputStream(SimplifiedLog toSerialize) {
        Output output = new Output(new ByteArrayOutputStream());
        kryo.get().writeObject(output, toSerialize);
        output.close();
        return output.toBytes();
    }

    @VisibleForTesting
    byte[] listToBytesWithOutputStream(List<SimplifiedLog> toSerialize) {
        Output output = new Output(new ByteArrayOutputStream());
        kryo.get().writeObject(output, toSerialize);
        output.close();
        return output.toBytes();
    }

    public SimplifiedLog fromBytes(byte[] toDeserialize) {
        Input input = new Input(toDeserialize);
        SimplifiedLog deserialized = kryo.get().readObject(input, SimplifiedLog.class);
        input.close();
        return deserialized;
    }

    @SuppressWarnings("unchecked")
    public List<SimplifiedLog> listFromBytes(byte[] toDeserialize) {
        Input input = new Input(toDeserialize);
        List<SimplifiedLog> deserialized = kryo.get().readObject(input, ImmutableList.class);
        input.close();
        return deserialized;
    }

    private final class ImmutableListSerializer extends Serializer<ImmutableList<Object>> {
        public ImmutableListSerializer() {
            super(false, true);
        }

        @Override
        public void write(Kryo kryo, Output output, ImmutableList<Object> object) {
            output.writeInt(object.size(), true);
            object.forEach(element -> kryo.writeClassAndObject(output, element));
        }

        @Override
        public ImmutableList<Object> read(Kryo kryo, Input input, Class<ImmutableList<Object>> type) {
            int elements = input.readInt(true);
            Builder<Object> builder = ImmutableList.builder();

            for (int i = 0; i < elements; i++) {
                builder.add(kryo.readClassAndObject(input));
            }
            return builder.build();
        }
    }
}
