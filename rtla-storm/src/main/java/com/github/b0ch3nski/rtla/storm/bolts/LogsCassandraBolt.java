package com.github.b0ch3nski.rtla.storm.bolts;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;

import java.util.Map;

/**
 * @author bochen
 */
public class LogsCassandraBolt extends BaseBasicBolt {

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}
