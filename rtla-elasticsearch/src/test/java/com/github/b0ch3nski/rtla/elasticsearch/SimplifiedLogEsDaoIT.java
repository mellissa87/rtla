package com.github.b0ch3nski.rtla.elasticsearch;

import ch.qos.logback.classic.Level;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.jayway.awaitility.Awaitility;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.joda.time.DateTime;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.b0ch3nski.rtla.common.utils.RandomLogFactory.getPreparedTestData;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author bochen
 */
public class SimplifiedLogEsDaoIT extends ElasticsearchDaoIT {

    private static final int MSG_AMOUNT = 64;
    private static final int TIMEOUT = MSG_AMOUNT * 2;
    private static final Level LEVEL = Level.DEBUG;

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifiedLogEsDaoIT.class);
    private static final SimplifiedLogEsDao DAO = new SimplifiedLogEsDao(getSettingsForDao());

    private static Map<String, List<SimplifiedLog>> expected;

    @BeforeClass
    public static void setUpBeforeClass() {
        expected = getPreparedTestData(MSG_AMOUNT, LEVEL);
        List<SimplifiedLog> allLogs = expected.get("all");

        DAO.createIndex();
        DAO.save(allLogs);
        DAO.refreshBulk();

        QueryBuilder query = QueryBuilders.matchQuery("level", LEVEL.toString());
        waitForIndexes(query, MSG_AMOUNT);
        LOGGER.debug("Successfully saved {} {} messages in Elasticsearch", LEVEL, MSG_AMOUNT);
    }

    private static void waitForIndexes(QueryBuilder query, int expected) {
        Awaitility.await()
                .atMost(TIMEOUT, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> DAO.search(query, expected).size() == expected);
    }

    /*
    @Test
    public void shouldSaveAndGetSingleLog() {
        // given
        SimplifiedLog expected = RandomLogFactory.create();

        // when
        String id = DAO.save(expected).getId();
        DAO.refreshIndex();

        Optional<SimplifiedLog> retrieved = DAO.get(id);

        // then
        assertThat(retrieved.isPresent(), is(true));
        assertThat(retrieved.get(), is(expected));
    }
    */

    @Test
    public void shouldRetrieveLogsByTimestamp() {
        QueryBuilder query = QueryBuilders.rangeQuery("timeStamp")
                .from(DateTime.now().minusMinutes(10))
                .to(DateTime.now().plusMinutes(10));

        List<SimplifiedLog> retrieved = DAO.search(query, MSG_AMOUNT);

        assertThat(retrieved.size(), is(MSG_AMOUNT));
        assertThat(retrieved, is(expected.get("all")));
    }

    @AfterClass
    public static void tearDownAfterClass() {
        DAO.deleteIndex();
        DAO.shutdown();
    }
}
