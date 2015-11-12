package com.github.b0ch3nski.rtla.common.model;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog.SimplifiedLogBuilder;
import com.google.common.annotations.VisibleForTesting;

/**
 * @author bochen
 */
public final class SimplifiedLogSerializer {

    private static final ThreadLocal<Kryo> KRYO = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.register(SimplifiedLog.class, new SimplifiedLogKryoSerializer());
            return kryo;
        }
    };

    public byte[] toBytes(SimplifiedLog toSerialize) {
        return toBytes(toSerialize, 1000, -1);
    }

    @VisibleForTesting
    byte[] toBytes(SimplifiedLog toSerialize, int kryoBufferSize, int kryoMaxBufferSize) {
        Output output = new Output(kryoBufferSize, kryoMaxBufferSize);
        KRYO.get().writeObject(output, toSerialize);
        output.close();
        return output.toBytes();
    }

    public SimplifiedLog fromBytes(byte[] toDeserialize) {
        Input input = new Input(toDeserialize);
        SimplifiedLog deserialized = KRYO.get().readObject(input, SimplifiedLog.class);
        input.close();
        return deserialized;
    }

    private static final class SimplifiedLogKryoSerializer extends Serializer<SimplifiedLog> {
        @Override
        public void write(Kryo kryo, Output output, SimplifiedLog log) {
            output.writeLong(log.getTimeStamp());
            output.writeString(log.getHostName());
            output.writeString(log.getLevel());
            output.writeString(log.getThreadName());
            output.writeString(log.getLoggerName());
            output.writeString(log.getFormattedMessage());
        }

        @Override
        public SimplifiedLog read(Kryo kryo, Input input, Class<SimplifiedLog> type) {
            return new SimplifiedLogBuilder()
                    .withTimeStamp(input.readLong())
                    .withHostName(input.readString())
                    .withLevel(input.readString())
                    .withThreadName(input.readString())
                    .withLoggerName(input.readString())
                    .withFormattedMessage(input.readString())
                    .build();
        }
    }
}
