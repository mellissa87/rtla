package com.github.b0ch3nski.rtla.kafka;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.RandomLogFactory;
import org.hamcrest.MatcherAssert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author bochen
 */
public class KafkaPipeIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPipeIT.class);
    private static final EmbeddedKafka KAFKA = new EmbeddedKafka(12181, 18888);
    private static final String TOPIC = "test";
    private static final int TOPIC_PART = 5;
    private static final int MSG_AMOUNT = 15;

    @ClassRule
    public static final ExternalResource RESOURCE = new ExternalResource() {
        @Override
        protected void before() throws Exception {
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

    private List<SimplifiedLog> createTestData() {
        List<SimplifiedLog> toReturn = new ArrayList<>();
        for (int i = 0; i < TOPIC_PART; i++) {
            toReturn.addAll(RandomLogFactory.create((MSG_AMOUNT / TOPIC_PART), "host" + i));
        }
        return toReturn;
    }

    private void checkLists(List<SimplifiedLog> expectedList, List<SimplifiedLog> retrievedList) {
        LOGGER.debug("Looking for {} results | Got {}", expectedList.size(), retrievedList.size());
        MatcherAssert.assertThat(retrievedList.size(), is(expectedList.size()));
        MatcherAssert.assertThat(retrievedList.containsAll(expectedList), is(true));
    }

    @Test
    public void shouldCreateTopic() {
        assertThat(KAFKA.isTopicAvailable(TOPIC), is(true));
    }

    @Test
    public void shouldSendAndReceiveLogs() throws InterruptedException {
        List<SimplifiedLog> expected = createTestData();
        KAFKA.produce(expected, TOPIC);

        List<SimplifiedLog> retrieved = KAFKA.consume(TOPIC, TOPIC_PART, (MSG_AMOUNT / TOPIC_PART));

        checkLists(expected, retrieved);
    }
}
