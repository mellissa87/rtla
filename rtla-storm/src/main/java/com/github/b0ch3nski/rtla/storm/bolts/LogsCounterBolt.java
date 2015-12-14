package com.github.b0ch3nski.rtla.storm.bolts;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import com.codahale.metrics.Meter;
import com.github.b0ch3nski.rtla.common.metrics.MetricsHandler;
import com.github.b0ch3nski.rtla.storm.utils.StormMetricsWrapper;

import java.util.Map;

/**
 * @author bochen
 */
public class LogsCounterBolt extends BaseBasicBolt {

    private transient Meter logsCountMeter;

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);

        logsCountMeter = StormMetricsWrapper.getMeter(context, "logs", "count");
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        logsCountMeter.mark();
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
