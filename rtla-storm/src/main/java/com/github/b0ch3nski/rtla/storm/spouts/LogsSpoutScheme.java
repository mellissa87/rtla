package com.github.b0ch3nski.rtla.storm.spouts;

import backtype.storm.spout.MultiScheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLogFrame;
import com.github.b0ch3nski.rtla.common.serialization.SerializationHandler;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * @author bochen
 */
public class LogsSpoutScheme implements MultiScheme {

    @Override
    public Iterable<List<Object>> deserialize(byte[] ser) {
        Values values = new Values();
        SimplifiedLogFrame frame = SerializationHandler.fromBytesUsingKryo(ser, SimplifiedLogFrame.class);

        values.add(frame.getHostName());
        values.add(frame);
        return ImmutableList.of(values);
    }

    @Override
    public Fields getOutputFields() {
        return new Fields("host", "frame");
    }
}
