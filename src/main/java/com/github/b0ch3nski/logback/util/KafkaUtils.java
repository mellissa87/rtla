package com.github.b0ch3nski.logback.util;

import com.github.b0ch3nski.logback.model.SimplifiedLog;
import com.github.b0ch3nski.logback.model.SimplifiedLogSerializer;
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

    private KafkaUtils() { }

    public static ConsumerConnector createConsumer(String zkConnection, String groupId, String consumerId) {
        Properties consumerProperties = new Properties();
        consumerProperties.put("zookeeper.connect", zkConnection);
        consumerProperties.put("group.id", groupId);
        consumerProperties.put("consumer.id", consumerId);
        LOGGER.debug("Creating consumer with properties: {}", consumerProperties);
        return Consumer.createJavaConsumerConnector(new ConsumerConfig(consumerProperties));
    }

    public static ConsumerIterator<String, SimplifiedLog> getConsumerIterator(ConsumerConnector consumer, String topicName) {
        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topicName, 1);

        Map<String, List<KafkaStream<String, SimplifiedLog>>> events =
                consumer.createMessageStreams(topicCountMap, new StringDecoder(null), new SimplifiedLogSerializer(null));
        return events.get(topicName).get(0).iterator();
    }

    public static Producer<String, SimplifiedLog> createProducer(String brokers, KafkaProducerType type, boolean acks) {
        Properties producerProperties = new Properties();
        producerProperties.put("metadata.broker.list", brokers);
        producerProperties.put("key.serializer.class", "kafka.serializer.StringEncoder");
        producerProperties.put("serializer.class", SimplifiedLogSerializer.class.getName());
        producerProperties.put("producer.type", type.typeAsString);
        producerProperties.put("request.required.acks", String.valueOf(Boolean.compare(acks, false)));
        producerProperties.put("compression.codec", "none");
        LOGGER.debug("Creating producer with properties: {}", producerProperties);
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
