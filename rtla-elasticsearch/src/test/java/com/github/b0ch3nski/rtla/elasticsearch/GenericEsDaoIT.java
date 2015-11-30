package com.github.b0ch3nski.rtla.elasticsearch;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.RandomLogFactory;
import org.elasticsearch.common.settings.Settings;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author bochen
 */
public class GenericEsDaoIT extends ElasticsearchDaoIT {

    private static final GenericEsDao DAO = new GenericEsDao(
            Settings.settingsBuilder()
                    .put("cluster.name", "es-test")
                    .put("discovery.zen.ping.unicast.hosts", "localhost")
                    .build()
    );

//    @Test
    public void shouldSendAndGetLog() throws IOException {
        SimplifiedLog expected = RandomLogFactory.create();

        DAO.save(expected, "test", "test");

        SimplifiedLog retrieved = (SimplifiedLog) DAO.get("test", "test", "1", SimplifiedLog.class);

        assertThat(retrieved, is(expected));
    }

    @AfterClass
    public static void tearDownAfterClass() {
        DAO.shutdown();
    }
}
