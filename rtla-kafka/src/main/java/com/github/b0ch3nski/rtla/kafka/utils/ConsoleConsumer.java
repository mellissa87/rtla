package com.github.b0ch3nski.rtla.kafka.utils;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.Validators;
import com.google.common.base.Preconditions;
import kafka.consumer.ConsumerIterator;
import kafka.javaapi.consumer.ConsumerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bochen
 */
public final class ConsoleConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleConsumer.class);

    public static void main(String[] args) {
        Preconditions.checkArgument(args.length == 2, "Usage: java -jar logback-kafka-appender-1.0-shaded.jar zkHost:zkPort topicName");
        Validators.isNotNullOrEmpty(args[0], "zkConnection");
        Validators.isNotNullOrEmpty(args[1], "topicName");

        LOGGER.info("Starting consumer...");
        ConsumerConnector consumer = KafkaUtils.createConsumer(args[0], "test_group", "1");
        ConsumerIterator<String, SimplifiedLog> consumerIterator = KafkaUtils.getConsumerIterator(consumer, args[1]);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down consumer...");
            if (consumer != null) consumer.shutdown();
        }, "shutdownHook"));

        LOGGER.info("OK! Waiting for messages...");
        consumerIterator.forEachRemaining(data -> System.out.println("[partition: " + data.partition() + "] " + data.message()));
    }
}
