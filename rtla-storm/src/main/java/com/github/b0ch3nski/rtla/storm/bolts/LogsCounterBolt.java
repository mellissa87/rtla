package com.github.b0ch3nski.rtla.storm.bolts;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import com.codahale.metrics.Meter;
import com.github.b0ch3nski.rtla.cassandra.CassandraTable;
import com.github.b0ch3nski.rtla.common.metrics.MetricsHandler;
import com.github.b0ch3nski.rtla.storm.utils.FieldNames;
import com.github.b0ch3nski.rtla.storm.utils.StormMetricsWrapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import java.util.EnumSet;
import java.util.Map;

/**
 * @author bochen
 */
public class LogsCounterBolt extends BaseBasicBolt {

    private transient Map<String, Meter> meters;

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);

        Builder<String, Meter> builder = ImmutableMap.builder();
        EnumSet.allOf(CassandraTable.class).forEach(table ->
                builder.put(table.name(), StormMetricsWrapper.getMeter(context, table.name().toLowerCase(), "logs", "count"))
        );
        meters = builder.build();
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        String level = input.getStringByField(FieldNames.LEVEL.toString());
        Meter meter = meters.get(level);

        if (meter != null) meter.mark();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // final bolt - no output fields
    }

    @Override
    public void cleanup() {
        MetricsHandler.getInstance().shutdown();
        super.cleanup();
    }
}
