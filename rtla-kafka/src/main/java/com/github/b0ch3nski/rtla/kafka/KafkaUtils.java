package com.github.b0ch3nski.rtla.kafka;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.Validators;
import kafka.cluster.Broker;
import kafka.consumer.*;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringDecoder;
import kafka.serializer.StringEncoder;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;
import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bochen
 */
public final class KafkaUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaUtils.class);
    private static final Properties COMMON_PROPS;

    static {
        COMMON_PROPS = new Properties();
        COMMON_PROPS.put("key.serializer.class", StringEncoder.class.getName());
        COMMON_PROPS.put("serializer.class", SimplifiedLogKafkaSerializer.class.getName());
        COMMON_PROPS.put("partitioner.class", ObjectHashcodePartitioner.class.getName());
        COMMON_PROPS.put("compression.codec", "none");
    }

    private KafkaUtils() { }

    protected static ZkClient createZkClient(String zkConnection) {
        Validators.isNotNullOrEmpty(zkConnection, "zkConnection");
        return new ZkClient(zkConnection, 10000, 10000, ZKStringSerializer$.MODULE$);
    }

    public static ConsumerConnector createConsumer(String zkConnection, String groupId, String consumerId) {
        Validators.isNotNullOrEmpty(zkConnection, "zkConnection");
        Validators.isNotNullOrEmpty(groupId, "groupId");
        Validators.isNotNullOrEmpty(consumerId, "consumerId");

        Properties consumerProperties = new Properties();
        consumerProperties.put("zookeeper.connect", zkConnection);
        consumerProperties.put("group.id", groupId);
        consumerProperties.put("consumer.id", consumerId);
        LOGGER.debug("Creating consumer with properties: {}", consumerProperties);
        return Consumer.createJavaConsumerConnector(new ConsumerConfig(consumerProperties));
    }

    public static List<KafkaStream<String, SimplifiedLog>> getConsumerStreams(ConsumerConnector consumer, String topicName, int streamAmount) {
        Validators.isNotNull(consumer, "consumer");
        Validators.isNotNullOrEmpty(topicName, "topicName");
        Validators.isGreaterThanZero(streamAmount, "streamAmount");

        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topicName, streamAmount);

        return consumer.createMessageStreams(
                topicCountMap,
                new StringDecoder(null),
                new SimplifiedLogKafkaSerializer(null)
        ).get(topicName);
    }

    public static List<String> listTopics(String zkConnection) {
        return createZkClient(zkConnection)
                .getChildren("/brokers/topics")
                .parallelStream()
                .collect(Collectors.toList());
    }

    public static List<Integer> listPartitions(String zkConnection, String topicName) {
        ZkClient zkClient = createZkClient(zkConnection);

        String path = "/brokers/topics/" + topicName + "/partitions";
        if (zkClient.exists(path)) return zkClient.getChildren(path).parallelStream().map(Integer::new).collect(Collectors.toList());
        else throw new KafkaException("Topic " + topicName + " doesn't exist");
    }

    public static String listBrokers(String zkConnection) {
        ZkClient zkClient = createZkClient(zkConnection);
        List<String> brokers = new ArrayList<>();

        zkClient.getChildren("/brokers/ids").forEach(id -> {
            Broker broker = Broker.createBroker(Integer.valueOf(id), zkClient.readData("/brokers/ids/" + id, false));
            if (broker != null) brokers.add(broker.connectionString());
        });
        return String.join(",", brokers);
    }

    public static Producer<String, SimplifiedLog> createProducer(String zkConnection, KafkaProducerType type, boolean acks) {
        Validators.isNotNull(type, "type");

        Properties producerProperties = new Properties();
        producerProperties.putAll(COMMON_PROPS);
        producerProperties.put("metadata.broker.list", listBrokers(zkConnection));
        producerProperties.put("producer.type", type.toString());
        producerProperties.put("request.required.acks", String.valueOf(Boolean.compare(acks, false)));
        LOGGER.debug("Creating producer with properties: {}", producerProperties);
        return new Producer<>(new ProducerConfig(producerProperties));
    }

    public enum KafkaProducerType {
        SYNC("sync"), ASYNC("async");

        private final String typeAsString;

        KafkaProducerType(String typeAsString) {
            this.typeAsString = typeAsString;
        }

        @Override
        public String toString() {
            return typeAsString;
        }
    }
}
