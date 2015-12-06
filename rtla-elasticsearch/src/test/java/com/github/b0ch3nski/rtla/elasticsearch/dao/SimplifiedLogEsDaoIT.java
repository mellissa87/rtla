package com.github.b0ch3nski.rtla.elasticsearch.dao;

import ch.qos.logback.classic.Level;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.google.common.base.Optional;
import com.jayway.awaitility.Awaitility;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.b0ch3nski.rtla.common.utils.RandomLogFactory.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author bochen
 */
public final class SimplifiedLogEsDaoIT extends ElasticsearchDaoIT {

    private static final int MSG_AMOUNT = 32;
    private static final int TIMEOUT = MSG_AMOUNT * 2;
    private static final Level LEVEL = Level.DEBUG;

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifiedLogEsDaoIT.class);
    private static final SimplifiedLogEsDao DAO = new SimplifiedLogEsDao(getSettingsForDao());
    private static final Map<String, List<SimplifiedLog>> EXPECTED = getPreparedTestData(MSG_AMOUNT, LEVEL);

    @BeforeClass
    public static void setUpBeforeClass() {
        List<SimplifiedLog> allLogs = EXPECTED.get("all");

        DAO.createIndex();
        DAO.save(allLogs);
        DAO.refreshBulk();

        QueryBuilder query = QueryBuilders.matchQuery("level", LEVEL.toString());
        waitForIndexes(query, MSG_AMOUNT);
        LOGGER.debug("Successfully saved {} {} messages in Elasticsearch", LEVEL, MSG_AMOUNT);
    }

    private static void waitForIndexes(QueryBuilder queryToExecute, int expectedCount) {
        Awaitility.await()
                .atMost(TIMEOUT, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> DAO.search(queryToExecute, expectedCount).size() == expectedCount);
    }

    private void checkLists(String expectedListName, QueryBuilder queryToExecute) {
        List<SimplifiedLog> expectedList = EXPECTED.get(expectedListName);
        List<SimplifiedLog> retrievedList = DAO.search(queryToExecute, MSG_AMOUNT);

        LOGGER.debug("Looking for {} results [{}] | Got {}", expectedList.size(), expectedListName, retrievedList.size());
        assertThat(retrievedList.size(), is(expectedList.size()));
        assertThat(retrievedList, is(expectedList));
    }

    @Test
    public void shouldRetrieveLogsByHost() {
        QueryBuilder query = QueryBuilders.matchQuery("hostName", HOST1);

        checkLists("host", query);
    }

    @Test
    public void shouldRetrieveLogsByTimestamp() {
        QueryBuilder query = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("hostName", HOST1))
                .must(QueryBuilders.rangeQuery("timeStamp").from(TIME1).to(TIME2));

        checkLists("time", query);
    }

    @Test
    public void shouldRetrieveLogsByLogger() {
        QueryBuilder query = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("hostName", HOST1))
                .must(QueryBuilders.rangeQuery("timeStamp").from(TIME1).to(TIME2))
                .must(QueryBuilders.matchQuery("loggerName", LOGGER1));

        checkLists("logger", query);
    }

    @Test
    public void shouldRetrieveLogsByThread() {
        QueryBuilder query = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("hostName", HOST1))
                .must(QueryBuilders.rangeQuery("timeStamp").from(TIME1).to(TIME2))
                .must(QueryBuilders.matchQuery("threadName", THREAD1));

        checkLists("thread", query);
    }

    @Test
    public void shouldRetrieveLogsByLoggerAndThread() {
        SimplifiedLog expected = EXPECTED.get("logger_thread").get(0);

        QueryBuilder query = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("hostName", HOST1))
                .must(QueryBuilders.rangeQuery("timeStamp").from(TIME1).to(TIME2))
                .must(QueryBuilders.matchQuery("loggerName", LOGGER1))
                .must(QueryBuilders.matchQuery("threadName", THREAD1));

        String id = DAO.searchIds(query, MSG_AMOUNT).get(0);
        Optional<SimplifiedLog> retrieved = DAO.get(id);

        assertThat(retrieved.isPresent(), is(true));
        assertThat(retrieved.get(), is(expected));
    }

    @AfterClass
    public static void tearDownAfterClass() {
        DAO.deleteIndex();
        DAO.shutdown();
    }
}
