package com.github.b0ch3nski.rtla.elasticsearch;

import ch.qos.logback.classic.Level;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.RandomLogFactory;
import com.google.common.base.Optional;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.joda.time.DateTime;
import org.junit.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author bochen
 */
public class SimplifiedLogEsDaoTest extends ElasticsearchDaoIT {

    private static final SimplifiedLogEsDao DAO = new SimplifiedLogEsDao(getSettingsForDao());

    @Before
    public void setUp() {
        DAO.createIndex();
    }

    @Test
    public void shouldSaveAndGetSingleLog() {
        SimplifiedLog expected = RandomLogFactory.create(Level.DEBUG);

        String id = DAO.save(expected).getId();
        DAO.refreshIndex();

        Optional<SimplifiedLog> retrieved = DAO.get(id);

        assertThat(retrieved.isPresent(), is(true));
        assertThat(retrieved.get(), is(expected));
    }

//    @Test
    public void shouldSaveAndGetBulkLogs() {
        List<SimplifiedLog> expected = RandomLogFactory.create(15);

        DAO.saveBulk(expected);
        DAO.refreshBulk();
        DAO.refreshIndex();

        QueryBuilder query = QueryBuilders.rangeQuery("timeStamp")
                .from(DateTime.now().minusMinutes(1))
                .to(DateTime.now().plusMinutes(1));

        List<SimplifiedLog> retrieved = DAO.search(query);

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
