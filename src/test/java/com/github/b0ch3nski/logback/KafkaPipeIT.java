package com.github.b0ch3nski.logback;

import com.github.b0ch3nski.logback.model.SimplifiedLog;
import com.github.b0ch3nski.logback.util.KafkaTestingHelper;
import com.github.b0ch3nski.logback.util.RandomLogFactory;
import com.google.common.collect.Lists;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;

/**
 * @author bochen
 */
public class KafkaPipeIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPipeIT.class);
    private static final String TOPIC = "test";
    private static final KafkaTestingHelper KAFKA = new KafkaTestingHelper(2181, 8888, TOPIC, 1);

    @Before
    public void setUp() throws Exception {
        KAFKA.start();
    }

    @Test
    public void shouldCreateTopic() {
        LOGGER.debug("Checking if topic '{}' exist...", TOPIC);
        assertTrue(KAFKA.isTopicAvailable());
    }

    @Test
    public void shouldSendAndReceiveLog() {
        Collection<SimplifiedLog> expected = Lists.newArrayList(RandomLogFactory.create(), RandomLogFactory.create());
        KAFKA.send(expected, TOPIC);

        await()
                .atMost(15, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(KAFKA.messagesArrived(expected));
    }

    /*
    // Not used anymore... Kept for a reference.
    @Test
    @Deprecated
    public void shouldSendAndReceiveLogOld() throws TimeoutException {
        Collection<SimplifiedLog> expected = Lists.newArrayList(RandomLogFactory.create(), RandomLogFactory.create());
        KAFKA.send(expected, TOPIC);

        Collection<SimplifiedLog> received = KAFKA.receive(2, 5);
        LOGGER.debug("Received logs: {}", received);

        assertThat(received.containsAll(expected), is(true));
    }
    */

    @After
    public void tearDown() throws IOException {
        KAFKA.stop();
    }
}
