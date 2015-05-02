package com.github.b0ch3nski.logback.util;

import com.github.b0ch3nski.logback.model.SimplifiedLog;
import com.google.common.base.Preconditions;
import kafka.consumer.*;
import kafka.javaapi.consumer.ConsumerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bochen
 */
public final class ConsoleConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleConsumer.class);

    public static void main(String[] args) {
        Preconditions.checkArgument(args.length == 2, "Usage: java -jar logback-kafka-appender-1.0.jar zkHost:zkPort topicName");

        LOGGER.info("Starting consumer...");
        final ConsumerConnector consumer = KafkaUtils.createConsumer(args[0], "test_group", "1");
        ConsumerIterator<String, SimplifiedLog> consumerIterator = KafkaUtils.getConsumerIterator(consumer, args[1]);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("Shutting down consumer...");

                if (consumer != null) {
                    consumer.shutdown();
                }
            }
        }, "shutdownHook"));

        LOGGER.info("OK! Waiting for messages...");
        while (consumerIterator.hasNext()) {
            System.out.println(consumerIterator.next().message());
        }
    }
}
