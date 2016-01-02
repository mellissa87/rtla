package com.github.b0ch3nski.rtla.kafka;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.FileUtils;
import kafka.admin.AdminUtils;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.message.MessageAndMetadata;
import kafka.producer.KeyedMessage;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import org.I0Itec.zkclient.ZkClient;
import org.apache.curator.test.TestingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static com.github.b0ch3nski.rtla.kafka.KafkaUtils.KafkaProducerType.ASYNC;

/**
 * @author bochen
 */
public final class EmbeddedKafka {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedKafka.class);
    private final int zkPort;
    private final int kafkaPort;
    private TestingServer zkServer;
    private ZkClient zkClient;
    private KafkaServerStartable kafkaServer;
    private Producer<String, SimplifiedLog> producer;

    public EmbeddedKafka(int zkPort, int kafkaPort) {
        this.zkPort = zkPort;
        this.kafkaPort = kafkaPort;
    }

    private Properties getServerProperties() {
        Properties serverProperties = new Properties();
        serverProperties.put("zookeeper.connect", zkServer.getConnectString());
        serverProperties.put("broker.id", "1");
        serverProperties.put("host.name", "localhost");
        serverProperties.put("port", String.valueOf(kafkaPort));
        serverProperties.put("log.dir", FileUtils.createTmpDir("embedded-kafka"));
        serverProperties.put("log.flush.interval.messages", "1");
        return serverProperties;
    }

    public void start() throws Exception {
        zkServer = new TestingServer(zkPort, true);
        zkClient = KafkaUtils.createZkClient(zkServer.getConnectString());

        kafkaServer = new KafkaServerStartable(new KafkaConfig(getServerProperties()));
        kafkaServer.startup();
        LOGGER.debug("Started Kafka server at port {}", kafkaPort);
    }

    public void createTopic(String topicName, int topicPartitions) {
        AdminUtils.createTopic(zkClient, topicName, topicPartitions, 1, new Properties());
        LOGGER.debug("Created topic '{}' with {} partitions", topicName, topicPartitions);
    }

    public void produce(SimplifiedLog message, String topicName) {
        if (producer == null)
            producer = KafkaUtils.createProducer(
                    KafkaUtils.createZkClient(zkServer.getConnectString()), ASYNC, false);
        producer.send(new KeyedMessage<>(topicName, message.getHostName(), message));
        LOGGER.debug("Sent message: {}", message);
    }

    public void produce(List<SimplifiedLog> messages, String topicName) {
        messages.forEach(message -> produce(message, topicName));
    }

    private Callable<List<SimplifiedLog>> createConsumerThread(ConsumerIterator<String, SimplifiedLog> iterator, int expectedMsg) {
        return () -> {
            List<SimplifiedLog> received = new ArrayList<>();
            while ((received.size() < expectedMsg) && iterator.hasNext()) {
                MessageAndMetadata data = iterator.next();
                received.add((SimplifiedLog) data.message());
                LOGGER.debug("Received message: {} | From partition: {}", data.message(), data.partition());
            }
            return received;
        };
    }

    private List<SimplifiedLog> getResultsFromFutures(List<Future<List<SimplifiedLog>>> futures) throws InterruptedException {
        List<SimplifiedLog> received = new ArrayList<>();
        for (Future<List<SimplifiedLog>> future : futures) {
            try {
                received.addAll(future.get());
            } catch (CancellationException ignored) {
            } catch (ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }
        return received;
    }

    public List<SimplifiedLog> consume(String topicName, int topicPartitions, int expectedMsg) throws InterruptedException {
        ConsumerConnector consumer = KafkaUtils.createConsumer(zkServer.getConnectString(), "test_group", "1");
        List<KafkaStream<String, SimplifiedLog>> streams = KafkaUtils.getConsumerStreams(consumer, topicName, topicPartitions);

        List<Callable<List<SimplifiedLog>>> tasks = new ArrayList<>();
        streams.forEach(stream -> tasks.add(createConsumerThread(stream.iterator(), expectedMsg)));

        ExecutorService executor = Executors.newFixedThreadPool(streams.size());
        List<Future<List<SimplifiedLog>>> futures = executor.invokeAll(tasks, 5 * expectedMsg, TimeUnit.SECONDS);

        List<SimplifiedLog> received = getResultsFromFutures(futures);
        consumer.shutdown();
        return received;
    }

    public boolean isTopicAvailable(String topicName) {
        return AdminUtils.topicExists(zkClient, topicName);
    }

    protected String getZkConnectString() {
        if (zkServer != null) return zkServer.getConnectString();
        else throw new IllegalStateException("Zookeeper server is not initialized");
    }

    public void stop() throws IOException {
        if (zkClient != null) zkClient.close();
        if (kafkaServer != null) kafkaServer.shutdown();
        if (zkServer != null) zkServer.stop();
        LOGGER.debug("Zookeeper / Kafka services stopped!");
    }
}
