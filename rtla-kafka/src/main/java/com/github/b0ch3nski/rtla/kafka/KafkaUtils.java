package com.github.b0ch3nski.rtla.kafka;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.Validators;
import kafka.consumer.*;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringDecoder;
import kafka.serializer.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

    public static Producer<String, SimplifiedLog> createProducer(String brokers, KafkaProducerType type, boolean acks) {
        Validators.isNotNullOrEmpty(brokers, "brokers");
        Validators.isNotNull(type, "type");

        Properties producerProperties = new Properties();
        producerProperties.putAll(COMMON_PROPS);
        producerProperties.put("metadata.broker.list", brokers);
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
