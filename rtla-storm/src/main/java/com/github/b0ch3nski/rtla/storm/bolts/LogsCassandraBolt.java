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

import static com.github.b0ch3nski.rtla.cassandra.CassandraTable.*;

/**
 * @author bochen
 */
public class LogsCassandraBolt extends BaseBasicBolt {

    private Map<String, SimplifiedLogGenericCassDao> daos;

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);
        CassandraConfig config = new CassandraConfigBuilder().fromStormConf(stormConf).build();

        Builder<String, SimplifiedLogGenericCassDao> builder = ImmutableMap.builder();
        builder.put(ERROR.name(), new ErrorLogCassDao(config));
        builder.put(WARN.name(), new WarnLogCassDao(config));
        builder.put(INFO.name(), new InfoLogCassDao(config));
        builder.put(DEBUG.name(), new DebugLogCassDao(config));
        builder.put(TRACE.name(), new TraceLogCassDao(config));
        daos = builder.build();
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        SimplifiedLog log = (SimplifiedLog) input.getValue(1);

        SimplifiedLogGenericCassDao dao = daos.get(log.getLevel());

        if (dao != null) dao.save(log);
        else throw new IllegalStateException("DAO for " + log.getLevel() + " wasn't found!");
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // final bolt - no output fields
    }

    @Override
    public void cleanup() {
        daos.forEach((name, dao) -> dao.shutdown());
        super.cleanup();
    }
}
