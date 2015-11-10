package com.github.b0ch3nski.rtla.kafka;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.RandomLogFactory;
import com.google.common.collect.Lists;
import com.jayway.awaitility.Awaitility;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author bochen
 */
public class KafkaPipeIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPipeIT.class);
    private static final String TOPIC = "test";
    private static final KafkaTestingHelper KAFKA = new KafkaTestingHelper(12181, 18888, TOPIC, 1);
    private static final int MSG_AMOUNT = 5;
    private static final int TIMEOUT = MSG_AMOUNT * 5;

    @Before
    public void setUp() throws Exception {
        KAFKA.start();
    }

    @Test
    public void shouldCreateTopic() {
        LOGGER.debug("Checking if topic '{}' exist...", TOPIC);
        assertThat(KAFKA.isTopicAvailable(), is(true));
    }

    @Test
    public void shouldSendAndReceiveLog() {
        Collection<SimplifiedLog> expected = Lists.newArrayList(RandomLogFactory.create(MSG_AMOUNT));
        KAFKA.send(expected, TOPIC);

        Awaitility.await()
                .atMost(TIMEOUT, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(KAFKA.messagesArrived(expected));
    }

    /*
    // Not used anymore... Kept for a reference.
    @Test
    @Deprecated
    public void shouldSendAndReceiveLogOld() throws TimeoutException {
        Collection<SimplifiedLog> expected = Lists.newArrayList(RandomLogFactory.create(MSG_AMOUNT));
        KAFKA.send(expected, TOPIC);

        Collection<SimplifiedLog> received = KAFKA.receive(MSG_AMOUNT, TIMEOUT);

        assertThat(received.containsAll(expected), is(true));
    }
    */

    @After
    public void tearDown() throws IOException {
        KAFKA.stop();
    }
}
