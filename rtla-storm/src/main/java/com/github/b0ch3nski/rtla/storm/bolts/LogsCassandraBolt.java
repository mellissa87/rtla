package com.github.b0ch3nski.rtla.storm.bolts;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;
import com.github.b0ch3nski.rtla.cassandra.CassandraConfig.CassandraConfigBuilder;
import com.github.b0ch3nski.rtla.cassandra.dao.*;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import java.util.Map;

import static com.github.b0ch3nski.rtla.cassandra.Table.*;

/**
 * @author bochen
 */
public class LogsCassandraBolt extends BaseBasicBolt {

    private static final long TTL = 3600L;
    private Map<String, SimplifiedLogGenericDao> daos;

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);
        CassandraConfig config = new CassandraConfigBuilder().fromStormConf(stormConf).build();

        Builder<String, SimplifiedLogGenericDao> builder = ImmutableMap.builder();
        builder.put(ERROR.name(), new ErrorLogDao(config, TTL));
        builder.put(WARN.name(), new WarnLogDao(config, TTL));
        builder.put(INFO.name(), new InfoLogDao(config, TTL));
        builder.put(DEBUG.name(), new DebugLogDao(config, TTL));
        builder.put(TRACE.name(), new TraceLogDao(config, TTL));
        daos = builder.build();
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        SimplifiedLog log = (SimplifiedLog) input.getValue(1);

        SimplifiedLogGenericDao dao = daos.get(log.getLevel());

        if (dao != null) dao.save(log);
        else throw new IllegalStateException("DAO for " + log.getLevel() + " wasn't found!");
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // final bolt - no output fields
    }
}
