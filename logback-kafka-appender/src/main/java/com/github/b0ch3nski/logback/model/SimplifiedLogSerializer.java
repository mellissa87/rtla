package com.github.b0ch3nski.logback.model;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.b0ch3nski.logback.model.SimplifiedLog.SimplifiedLogBuilder;
import kafka.serializer.Decoder;
import kafka.serializer.Encoder;
import kafka.utils.VerifiableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bochen
 */
public final class SimplifiedLogSerializer implements Encoder<SimplifiedLog>, Decoder<SimplifiedLog> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifiedLogSerializer.class);

    private static final ThreadLocal<Kryo> KRYO = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.register(SimplifiedLog.class, new SimplifiedLogKryoSerializer());
            return kryo;
        }
    };

    public SimplifiedLogSerializer(VerifiableProperties properties) {
        if (properties != null) {
            LOGGER.debug("Creating serializer with properties: {}", properties);
        }
    }

    @Override
    public byte[] toBytes(SimplifiedLog toSerialize) {
        Output output = new Output(10000, 10000);   // TODO: Performance tests: find out best values
        KRYO.get().writeObject(output, toSerialize);
        output.close();
        return output.toBytes();
    }

    @Override
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
