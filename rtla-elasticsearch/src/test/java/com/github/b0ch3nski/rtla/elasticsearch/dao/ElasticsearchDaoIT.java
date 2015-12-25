package com.github.b0ch3nski.rtla.elasticsearch.dao;

import com.github.b0ch3nski.rtla.common.utils.FileUtils;
import com.github.b0ch3nski.rtla.elasticsearch.ElasticsearchConfigBuilder;
import com.github.b0ch3nski.rtla.elasticsearch.ElasticsearchSession;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bochen
 */
public class ElasticsearchDaoIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchDaoIT.class);
    private static final String CLUSTER_NAME = "unit-tests";

    @ClassRule
    public static final ExternalResource RESOURCE = new ExternalResource() {
        private Node node;

        private Settings getSettings(String path) {
            return Settings.settingsBuilder()
                    .put("cluster.name", CLUSTER_NAME)
                    .put("node.name", "embedded-es")
                    .put("path.home", path)
                    .put("path.data", path + "/data")
                    .put("discovery.zen.ping.multicast.enabled", false)
                    .put("index.number_of_shards", 1)
                    .put("index.number_of_replicas", 0)
                    .put("http.enabled", false)
                    .put("bootstrap.mlockall", true)
                    .build();
        }

        @Override
        protected void before() {
            Settings settings = getSettings(FileUtils.createTmpDir("embedded-es"));

            node = NodeBuilder.nodeBuilder()
                    .settings(settings)
                    .node();

            LOGGER.debug("Created embedded Elasticsearch node with settings = {}", node.settings().getAsMap());
        }

        @Override
        protected void after() {
            ElasticsearchSession.shutdown();
            node.close();
        }
    };

    protected static Settings getSettingsForDao() {
        return new ElasticsearchConfigBuilder()
                .withClusterName(CLUSTER_NAME)
                .withUnicastHosts("localhost")
                .withBulkActions("10")
                .withBulkSize("1mb")
                .withFlushTime("30s")
                .withTtl("30m")
                .build();
    }
}
