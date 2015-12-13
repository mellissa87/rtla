package com.github.b0ch3nski.rtla.common.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLogFrame;

/**
 * @author bochen
 */
public final class SimplifiedLogFrameKryoSerializer extends Serializer<SimplifiedLogFrame> {

    public SimplifiedLogFrameKryoSerializer() {
        super(false, false);
    }

    @Override
    public void write(Kryo kryo, Output output, SimplifiedLogFrame frame) {
        output.writeLong(frame.getTimeStamp(), true);
        output.writeString(frame.getHostName());
        output.writeString(frame.getLevel());
        output.writeInt(frame.getSimplifiedLogLength(), true);
        output.write(frame.getSimplifiedLog());
    }

    @Override
    public SimplifiedLogFrame read(Kryo kryo, Input input, Class<SimplifiedLogFrame> type) {
        return new SimplifiedLogFrame(
                input.readLong(true),
                input.readString(),
                input.readString(),
                input.readBytes(input.readInt(true))
        );
    }
}
