package com.github.b0ch3nski.rtla.elasticsearch;

import com.github.b0ch3nski.rtla.common.utils.FileUtils;
import org.elasticsearch.action.admin.cluster.node.info.*;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * @author bochen
 */
public final class ElasticsearchSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchSession.class);
    private final Node node;
    private final Client client;
    private static ElasticsearchSession instance;

    private ElasticsearchSession(Settings settings) {
        node = nodeBuilder()
                .settings(settings)
                .settings(Settings.settingsBuilder()
                                .put("network.host", "0.0.0.0")
                                .put("http.enabled", false)
                                .put("bootstrap.mlockall", true)
                                .put("path.home", FileUtils.createTmpDir("es-client"))
                                .put("discovery.zen.ping.multicast.enabled", false)
                )
                .client(true)
                .node();
        client = node.client();

        if (LOGGER.isInfoEnabled()) getInfo();
    }

    private void getInfo() {
        LOGGER.info("New Elasticsearch session was created with settings = {}", node.settings().getAsMap());

        NodesInfoRequest nodesInfo = new NodesInfoRequestBuilder(client, NodesInfoAction.INSTANCE).clear().request();
        LOGGER.info("Current cluster info = {}", client.admin().cluster().nodesInfo(nodesInfo).actionGet());
    }

    public static ElasticsearchSession getInstance(Settings settings) {
        if (instance == null) {
            synchronized (ElasticsearchSession.class) {
                if (instance == null) {
                    instance = new ElasticsearchSession(settings);
                }
            }
        }
        return instance;
    }

    public Client getClient() {
        return client;
    }

    private boolean isIndexExists(String indexName) {
        boolean isExists = client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
        LOGGER.info("Index {} found = {}", indexName, isExists);
        return isExists;
    }

    public synchronized void createIndex(String indexName, String typeName, XContentBuilder mapping) {
        if (isIndexExists(indexName)) return;
        LOGGER.info("Creating {} index", indexName);
        client.admin().indices().prepareCreate(indexName).addMapping(typeName, mapping).execute().actionGet();
    }

    public synchronized void deleteIndex(String indexName) {
        if (!isIndexExists(indexName)) return;
        LOGGER.info("Index removal called on {} index", indexName);
        client.admin().indices().prepareDelete(indexName).execute().actionGet();
    }

    public static synchronized void shutdown() {
        if (instance != null) {
            instance.client.close();
            instance.node.close();
            instance = null;
            LOGGER.info("Elasticsearch session has been closed!");
        }
    }
}
