package com.github.b0ch3nski.rtla.kafka.utils;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.Validation;
import com.github.b0ch3nski.rtla.kafka.HostnamePartitioner;
import com.github.b0ch3nski.rtla.kafka.SimplifiedLogKafkaSerializer;
import kafka.consumer.*;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import kafka.serializer.StringDecoder;
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
        COMMON_PROPS.put("key.serializer.class", "kafka.serializer.StringEncoder");
        COMMON_PROPS.put("serializer.class", SimplifiedLogKafkaSerializer.class.getName());
        COMMON_PROPS.put("partitioner.class", HostnamePartitioner.class.getName());
        COMMON_PROPS.put("compression.codec", "none");
    }

    private KafkaUtils() { }

    public static ConsumerConnector createConsumer(String zkConnection, String groupId, String consumerId) {
        Validation.isNotNullOrEmpty(zkConnection, "zkConnection");
        Validation.isNotNullOrEmpty(groupId, "groupId");
        Validation.isNotNullOrEmpty(consumerId, "consumerId");

        Properties consumerProperties = new Properties();
        consumerProperties.put("zookeeper.connect", zkConnection);
        consumerProperties.put("group.id", groupId);
        consumerProperties.put("consumer.id", consumerId);
        LOGGER.trace("Creating consumer with properties: {}", consumerProperties);
        return Consumer.createJavaConsumerConnector(new ConsumerConfig(consumerProperties));
    }

    public static ConsumerIterator<String, SimplifiedLog> getConsumerIterator(ConsumerConnector consumer, String topicName) {
        Validation.isNotNull(consumer, "consumer");
        Validation.isNotNullOrEmpty(topicName, "topicName");

        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topicName, 1);

        Map<String, List<KafkaStream<String, SimplifiedLog>>> events =
                consumer.createMessageStreams(topicCountMap, new StringDecoder(null), new SimplifiedLogKafkaSerializer(null));
        return events.get(topicName).get(0).iterator();
    }

    public static Producer<String, SimplifiedLog> createProducer(String brokers, KafkaProducerType type, boolean acks) {
        Validation.isNotNullOrEmpty(brokers, "brokers");
        Validation.isNotNull(type, "type");

        Properties producerProperties = new Properties();
        producerProperties.putAll(COMMON_PROPS);
        producerProperties.put("metadata.broker.list", brokers);
        producerProperties.put("producer.type", type.typeAsString);
        producerProperties.put("request.required.acks", String.valueOf(Boolean.compare(acks, false)));
        LOGGER.trace("Creating producer with properties: {}", producerProperties);
        return new Producer<>(new ProducerConfig(producerProperties));
    }

    public enum KafkaProducerType {
        SYNC("sync"), ASYNC("async");

        public final String typeAsString;

        KafkaProducerType(String typeAsString) {
            this.typeAsString = typeAsString;
        }
    }
}
