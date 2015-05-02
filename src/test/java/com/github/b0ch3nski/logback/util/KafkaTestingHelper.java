package com.github.b0ch3nski.logback.util;

import com.github.b0ch3nski.logback.model.SimplifiedLog;
import com.github.b0ch3nski.logback.util.KafkaUtils.KafkaProducerType;
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
import java.util.concurrent.*;

/**
 * @author bochen
 */
public final class KafkaTestingHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTestingHelper.class);
    private final int zkPort;
    private final int kafkaPort;
    private final String topicName;
    private final int topicPartitions;
    private TestingServer zkServer;
    private ZkClient zkClient;
    private KafkaServerStartable kafkaServer;
    private Producer<String, SimplifiedLog> producer;

    public KafkaTestingHelper(int zkPort, int kafkaPort, String topicName, int topicPartitions) {
        this.zkPort = zkPort;
        this.kafkaPort = kafkaPort;
        this.topicName = topicName;
        this.topicPartitions = topicPartitions;
    }

    public void start() throws Exception {
        zkServer = new TestingServer(zkPort, true);
        zkClient = new ZkClient(zkServer.getConnectString(), 10000, 10000, ZKStringSerializer$.MODULE$);

        File logs = Files.createTempDirectory("kafka_tmp").toFile();
        logs.deleteOnExit();
        LOGGER.debug("Created temp log dir: {}", logs.getAbsolutePath());

        Properties serverProperties = new Properties();
        serverProperties.put("zookeeper.connect", zkServer.getConnectString());
        serverProperties.put("broker.id", "1");
        serverProperties.put("host.name", "localhost");
        serverProperties.put("port", String.valueOf(kafkaPort));
        serverProperties.put("log.dir", logs.getAbsolutePath());
        serverProperties.put("log.flush.interval.messages", "1");

        kafkaServer = new KafkaServerStartable(new KafkaConfig(serverProperties));
        kafkaServer.startup();

        AdminUtils.createTopic(zkClient, topicName, topicPartitions, 1, new Properties());

        producer = KafkaUtils.createProducer("localhost:" + kafkaPort, KafkaProducerType.ASYNC, false);
    }

    public void send(SimplifiedLog message, String topicName) {
        producer.send(new KeyedMessage<>(topicName, message.getHostName(), message));
        LOGGER.debug("Sent message: {}", message);
    }

    public void send(Collection<SimplifiedLog> messages, String topicName) {
        for (SimplifiedLog message : messages) {
            send(message, topicName);
        }
    }

    public Callable<Boolean> messagesArrived(final Collection<SimplifiedLog> expected) {
        LOGGER.debug("Expected messages: {}", expected);

        final ConsumerConnector consumer = KafkaUtils.createConsumer(zkServer.getConnectString(), "test_group", "1");
        final ConsumerIterator<String, SimplifiedLog> consumerIterator = KafkaUtils.getConsumerIterator(consumer, topicName);
        final List<SimplifiedLog> received = new ArrayList<>();

        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (consumerIterator.hasNext()) {
                    MessageAndMetadata data = consumerIterator.next();
                    received.add((SimplifiedLog) data.message());
                    LOGGER.debug("Received message: {} | From partition: {}", data.message(), data.partition());
                }
                consumer.shutdown();
                return received.containsAll(expected);
            }
        };
    }

    /*
    // Not used anymore... Kept for a reference.
    @Deprecated
    public Collection<SimplifiedLog> receive(final int messageCount, int messagesTimeout) throws TimeoutException {
        ConsumerConnector consumer = KafkaUtils.createConsumer(zkServer.getConnectString(), "test_group", "1");
        final ConsumerIterator<String, SimplifiedLog> consumerIterator = KafkaUtils.getConsumerIterator(consumer, topicName);

        ExecutorService singleThread = Executors.newSingleThreadExecutor();
        Future<List<SimplifiedLog>> submit = singleThread.submit(new Callable<List<SimplifiedLog>>() {
            @Override
            public List<SimplifiedLog> call() {
                List<SimplifiedLog> received = new ArrayList<>();

                while ((received.size() != messageCount) && consumerIterator.hasNext()) {
                    MessageAndMetadata data = consumerIterator.next();
                    received.add((SimplifiedLog) data.message());
                    LOGGER.debug("Received message: {} | From partition: {}", data.message(), data.partition());
                }
                return received;
            }
        });

        try {
            return submit.get(messagesTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
            throw new TimeoutException("Timed out waiting for messages");
        } finally {
            singleThread.shutdown();
            consumer.shutdown();
        }
    }
    */

    public boolean isTopicAvailable() {
        return AdminUtils.topicExists(zkClient, topicName);
    }

    public void stop() throws IOException {
        if (zkClient != null) {
            zkClient.close();
        }
        if (kafkaServer != null) {
            kafkaServer.shutdown();
        }
        if (zkServer != null) {
            zkServer.stop();
        }
    }
}
