package com.github.b0ch3nski.rtla.common.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

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
        return kryo;
    }

    public byte[] toBytes(SimplifiedLog toSerialize) {
        return toBytesWithCalculatedBufferSize(toSerialize);
    }

    private int getObjectSizeInBytes(SimplifiedLog toSerialize) {
        int size = 16 // timestamp and serialuid are longs, so 2 * 8
                    + toSerialize.getHostName().length()
                    + toSerialize.getLevel().length()
                    + toSerialize.getThreadName().length()
                    + toSerialize.getLoggerName().length()
                    + toSerialize.getFormattedMessage().length();

        LOGGER.trace("Calculated size of object {} is {}", toSerialize, size);
        return size;
    }

    @VisibleForTesting
    byte[] toBytesWithCalculatedBufferSize(SimplifiedLog toSerialize) {
        Output output = new Output(getObjectSizeInBytes(toSerialize));
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

    public SimplifiedLog fromBytes(byte[] toDeserialize) {
        Input input = new Input(toDeserialize);
        SimplifiedLog deserialized = kryo.get().readObject(input, SimplifiedLog.class);
        input.close();
        return deserialized;
    }
}
