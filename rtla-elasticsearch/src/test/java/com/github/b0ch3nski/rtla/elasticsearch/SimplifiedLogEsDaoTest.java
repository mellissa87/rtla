package com.github.b0ch3nski.rtla.elasticsearch;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.RandomLogFactory;
import com.google.common.base.Optional;
import com.jayway.awaitility.Awaitility;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.joda.time.DateTime;
import org.junit.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author bochen
 */
public class SimplifiedLogEsDaoTest extends ElasticsearchDaoIT {

    private static final SimplifiedLogEsDao DAO = new SimplifiedLogEsDao(getSettingsForDao());
    private static final int TIMEOUT = 15;

    @Before
    public void setUp() {
        DAO.createIndex();
    }

    private void waitForIndexes(QueryBuilder query, int expected) {
        Awaitility.await()
                .atMost(TIMEOUT, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> DAO.search(query).size() == expected);
    }

    private void checkLists(List<SimplifiedLog> expected, List<SimplifiedLog> retrieved) {
        assertThat(retrieved.size(), is(expected.size()));
        assertThat(retrieved.containsAll(expected), is(true));
    }

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

//    @Test
    public void shouldSaveAndGetBulkLogs() {
        // given
        List<SimplifiedLog> expected = RandomLogFactory.create(14);

        // when
        DAO.saveBulk(expected);
        DAO.refreshBulk();

        QueryBuilder query = QueryBuilders.rangeQuery("timeStamp")
                .from(DateTime.now().minusMinutes(1))
                .to(DateTime.now().plusMinutes(1));

        waitForIndexes(query, expected.size());
        List<SimplifiedLog> retrieved = DAO.search(query);

        // then
        assertThat(retrieved.size(), is(expected.size()));
        assertThat(retrieved, is(expected));
    }

    @After
    public void tearDown() {
        DAO.deleteIndex();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        DAO.shutdown();
    }
}
