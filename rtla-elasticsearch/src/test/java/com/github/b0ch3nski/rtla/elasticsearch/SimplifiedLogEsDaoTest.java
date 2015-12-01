package com.github.b0ch3nski.rtla.elasticsearch;

import ch.qos.logback.classic.Level;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.RandomLogFactory;
import com.google.common.base.Optional;
import org.junit.AfterClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author bochen
 */
public class SimplifiedLogEsDaoTest extends ElasticsearchDaoIT {

    private static final SimplifiedLogEsDao DAO = new SimplifiedLogEsDao(getSettingsForDao());

    @Test
    public void shouldSendAndGetLog() {
        SimplifiedLog expected = RandomLogFactory.create(Level.DEBUG);

        String id = DAO.save(expected);

        Optional<SimplifiedLog> retrieved = DAO.get(id);

        assertThat(retrieved.isPresent(), is(true));
        assertThat(retrieved.get(), is(expected));
    }

    @AfterClass
    public static void tearDownAfterClass() {
        DAO.shutdown();
    }
}
