package com.github.b0ch3nski.rtla.common.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog.SimplifiedLogBuilder;

/**
 * @author bochen
 */
public class SimplifiedLogKryoSerializer extends Serializer<SimplifiedLog> {

    public SimplifiedLogKryoSerializer() {
        super(false, false);
    }

    @Override
    public void write(Kryo kryo, Output output, SimplifiedLog log) {
        output.writeLong(log.getTimeStamp(), true);
        output.writeString(log.getHostName());
        output.writeString(log.getLevel());
        output.writeString(log.getThreadName());
        output.writeString(log.getLoggerName());
        output.writeString(log.getFormattedMessage());
    }

    @Override
    public SimplifiedLog read(Kryo kryo, Input input, Class<SimplifiedLog> type) {
        return new SimplifiedLogBuilder()
                .withTimeStamp(input.readLong(true))
                .withHostName(input.readString())
                .withLevel(input.readString())
                .withThreadName(input.readString())
                .withLoggerName(input.readString())
                .withFormattedMessage(input.readString())
                .build();
    }
}
