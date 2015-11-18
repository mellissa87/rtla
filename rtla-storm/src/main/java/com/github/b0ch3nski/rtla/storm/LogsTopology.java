package com.github.b0ch3nski.rtla.storm;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.*;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.storm.bolts.LogsCassandraBolt;
import com.github.b0ch3nski.rtla.storm.spouts.LogsSpoutScheme;
import storm.kafka.*;

/**
 * @author bochen
 */
public class LogsTopology {

    public static void main(String... args) throws InvalidTopologyException, AuthorizationException, AlreadyAliveException {
        SpoutConfig logsSpoutConfig = new SpoutConfig(new ZkHosts("zk:2181"), "logs", "/logs-spout-offsets", "logs-spout");
        logsSpoutConfig.scheme = new LogsSpoutScheme();
        KafkaSpout logsKafkaSpout = new KafkaSpout(logsSpoutConfig);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("logsKafkaSpout", logsKafkaSpout, 4).setNumTasks(4);
        builder.setBolt("logsCassandraBolt", new LogsCassandraBolt(), 4).setNumTasks(4).partialKeyGrouping("logsKafkaSpout", new Fields("host"));

        Config conf = new Config();
        conf.setDebug(false);
        conf.setFallBackOnJavaSerialization(false);
        conf.registerSerialization(SimplifiedLog.class, FieldSerializer.class);
        conf.setNumWorkers(2);

        StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
    }
}
