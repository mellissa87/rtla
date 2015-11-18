package com.github.b0ch3nski.rtla.storm.spouts;

import backtype.storm.spout.MultiScheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.serialization.SimplifiedLogSerializer;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * @author bochen
 */
public class LogsSpoutScheme implements MultiScheme {

    private static final SimplifiedLogSerializer SERIALIZER = new SimplifiedLogSerializer();

    @Override
    public Iterable<List<Object>> deserialize(byte[] ser) {
        Values values = new Values();
        SimplifiedLog deserialized = SERIALIZER.fromBytes(ser);

        values.add(deserialized.getHostName());
        values.add(deserialized);
        return ImmutableList.of(values);
    }

    @Override
    public Fields getOutputFields() {
        return new Fields("host", "log");
    }
}
