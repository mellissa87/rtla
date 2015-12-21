package com.github.b0ch3nski.rtla.storm.bolts;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Tuple;
import com.github.b0ch3nski.rtla.cassandra.CassandraConfig;
import com.github.b0ch3nski.rtla.cassandra.CassandraConfig.CassandraConfigBuilder;
import com.github.b0ch3nski.rtla.cassandra.CassandraSession;
import com.github.b0ch3nski.rtla.cassandra.dao.SimplifiedLogCassDao;
import com.github.b0ch3nski.rtla.cassandra.dao.SimplifiedLogCassDaoFactory;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.serialization.SerializationHandler;
import com.github.b0ch3nski.rtla.storm.utils.FieldNames;

import java.util.Map;

/**
 * @author bochen
 */
public class LogsCassandraBolt extends BaseBasicBolt {

    private transient Map<String, SimplifiedLogCassDao> daos;

    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        super.prepare(stormConf, context);

        CassandraConfig config = new CassandraConfigBuilder().fromStormConf(stormConf).build();
        daos = SimplifiedLogCassDaoFactory.createAllDaos(config);
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        String level = input.getStringByField(FieldNames.LEVEL.toString());
        SimplifiedLogCassDao dao = daos.get(level);

        // TODO: change this to use new fields and stop serializing/deserializing objects all the time!
        byte[] serializedLog = input.getBinaryByField(FieldNames.LOG.toString());
        SimplifiedLog log = SerializationHandler.fromBytesUsingKryo(serializedLog, SimplifiedLog.class);

        if (dao != null) dao.save(log);
        else throw new IllegalStateException("DAO for " + level + " wasn't found!");
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // final bolt - no output fields
    }

    @Override
    public void cleanup() {
        CassandraSession.shutdown();
        super.cleanup();
    }
}
