package com.github.b0ch3nski.rtla.storm.spouts;

import backtype.storm.spout.MultiScheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLogFrame;
import com.github.b0ch3nski.rtla.common.serialization.SerializationHandler;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.github.b0ch3nski.rtla.storm.utils.FieldNames.*;

/**
 * @author bochen
 */
public class LogsSpoutScheme implements MultiScheme {

    @Override
    public Iterable<List<Object>> deserialize(byte[] serializedFrame) {
        Values outputFields = new Values();
        SimplifiedLogFrame deserializedFrame = SerializationHandler.fromBytesUsingKryo(serializedFrame, SimplifiedLogFrame.class);

        outputFields.add(deserializedFrame.getHostName());
        outputFields.add(deserializedFrame.getLevel());
        outputFields.add(deserializedFrame.getSimplifiedLog());
        outputFields.add(serializedFrame);
        return ImmutableList.of(outputFields);
    }

    @Override
    public Fields getOutputFields() {
        return new Fields(
                HOST.toString(),
                LEVEL.toString(),
                LOG.toString(),
                FRAME.toString()
        );
    }
}
