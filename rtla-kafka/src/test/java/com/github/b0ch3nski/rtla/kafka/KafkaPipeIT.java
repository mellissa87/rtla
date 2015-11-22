package com.github.b0ch3nski.rtla.kafka;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.RandomLogFactory;
import com.jayway.awaitility.Awaitility;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author bochen
 */
public class KafkaPipeIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPipeIT.class);
    private static final KafkaTestingHelper KAFKA = new KafkaTestingHelper(12181, 18888);
    private static final String TOPIC = "test";
    private static final int TOPIC_PART = 1;
    private static final int MSG_AMOUNT = 5;
    private static final int TIMEOUT = MSG_AMOUNT * 5;

    @ClassRule
    public static final ExternalResource RESOURCE = new ExternalResource() {
        @Override
        protected void before() throws Throwable {
            KAFKA.start();
            KAFKA.createTopic(TOPIC, TOPIC_PART);
        }

        @Override
        protected void after() {
            try {
                KAFKA.stop();
            } catch (IOException ignored) {
            }
        }
    };

    @Test
    public void shouldCreateTopic() {
        LOGGER.debug("Checking if topic '{}' exist...", TOPIC);
        assertThat(KAFKA.isTopicAvailable(TOPIC), is(true));
    }

    @Test
    public void shouldSendAndReceiveLog() {
        List<SimplifiedLog> expected = RandomLogFactory.create(MSG_AMOUNT);
        KAFKA.send(expected, TOPIC);

        Awaitility.await()
                .atMost(TIMEOUT, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(KAFKA.messagesArrived(TOPIC, expected));
    }
}
