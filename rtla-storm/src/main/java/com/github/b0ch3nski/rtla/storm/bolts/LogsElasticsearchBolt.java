package com.github.b0ch3nski.rtla.storm.bolts;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.elasticsearch.ElasticsearchConfigBuilder;
import com.github.b0ch3nski.rtla.elasticsearch.dao.SimplifiedLogEsDao;
import org.elasticsearch.common.settings.Settings;

import java.util.Map;

/**
 * @author bochen
 */
public class LogsElasticsearchBolt extends BaseBasicBolt {

    private SimplifiedLogEsDao dao;

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);

        Settings settings = new ElasticsearchConfigBuilder().fromStormConf(stormConf).build();
        dao = new SimplifiedLogEsDao(settings);
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        SimplifiedLog log = (SimplifiedLog) input.getValue(1);
        dao.save(log);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // final bolt - no output fields
    }

    @Override
    public void cleanup() {
        dao.shutdown();
        super.cleanup();
    }
}
