package com.github.b0ch3nski.rtla.kafka;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.kafka.utils.KafkaUtils;
import com.github.b0ch3nski.rtla.kafka.utils.KafkaUtils.KafkaProducerType;
import kafka.admin.AdminUtils;
import kafka.consumer.ConsumerIterator;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.message.MessageAndMetadata;
import kafka.producer.KeyedMessage;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;
import org.apache.curator.test.TestingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author bochen
 */
public final class KafkaTestingHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTestingHelper.class);
    private final int zkPort;
    private final int kafkaPort;
    private TestingServer zkServer;
    private ZkClient zkClient;
    private KafkaServerStartable kafkaServer;
    private Producer<String, SimplifiedLog> producer;

    public KafkaTestingHelper(int zkPort, int kafkaPort) {
        this.zkPort = zkPort;
        this.kafkaPort = kafkaPort;
    }

    private Properties getServerProperties(String logsPath) {
        Properties serverProperties = new Properties();
        serverProperties.put("zookeeper.connect", zkServer.getConnectString());
        serverProperties.put("broker.id", "1");
        serverProperties.put("host.name", "localhost");
        serverProperties.put("port", String.valueOf(kafkaPort));
        serverProperties.put("log.dir", logsPath);
        serverProperties.put("log.flush.interval.messages", "1");
        return serverProperties;
    }

    public void start() throws Exception {
        zkServer = new TestingServer(zkPort, true);
        zkClient = new ZkClient(zkServer.getConnectString(), 10000, 10000, ZKStringSerializer$.MODULE$);

        File logs = Files.createTempDirectory("kafka_tmp").toFile();
        logs.deleteOnExit();
        String logsPath = logs.getAbsolutePath();
        LOGGER.trace("Created temp log dir: {}", logsPath);

        kafkaServer = new KafkaServerStartable(new KafkaConfig(getServerProperties(logsPath)));
        kafkaServer.startup();
        LOGGER.debug("Started Kafka server at port {}", kafkaPort);
    }

    public void createTopic(String topicName, int topicPartitions) {
        AdminUtils.createTopic(zkClient, topicName, topicPartitions, 1, new Properties());
        LOGGER.debug("Created topic '{}' with {} partitions", topicName, topicPartitions);
    }

    public void send(SimplifiedLog message, String topicName) {
        if (producer == null) producer = KafkaUtils.createProducer("localhost:" + kafkaPort, KafkaProducerType.ASYNC, false);
        producer.send(new KeyedMessage<>(topicName, message.getHostName(), message));
        LOGGER.debug("Sent message: {}", message);
    }

    public void send(List<SimplifiedLog> messages, String topicName) {
        messages.forEach(message -> send(message, topicName));
    }

    public Callable<Boolean> messagesArrived(String topicName, List<SimplifiedLog> expected) {
        ConsumerConnector consumer = KafkaUtils.createConsumer(zkServer.getConnectString(), "test_group", "1");
        ConsumerIterator<String, SimplifiedLog> consumerIterator = KafkaUtils.getConsumerIterator(consumer, topicName);
        List<SimplifiedLog> received = new ArrayList<>();

        return () -> {
            if (consumerIterator.hasNext()) {
                MessageAndMetadata data = consumerIterator.next();
                received.add((SimplifiedLog) data.message());
                LOGGER.debug("Received message: {} | From partition: {}", data.message(), data.partition());
            }
            consumer.shutdown();
            return received.containsAll(expected);
        };
    }

    public boolean isTopicAvailable(String topicName) {
        return AdminUtils.topicExists(zkClient, topicName);
    }

    public void stop() throws IOException {
        if (zkClient != null) zkClient.close();
        if (kafkaServer != null) kafkaServer.shutdown();
        if (zkServer != null) zkServer.stop();
        LOGGER.debug("Zookeeper / Kafka services stopped!");
    }
}
