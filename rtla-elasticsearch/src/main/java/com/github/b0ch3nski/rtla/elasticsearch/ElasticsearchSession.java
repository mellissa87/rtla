package com.github.b0ch3nski.rtla.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * @author bochen
 */
public final class ElasticsearchSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchSession.class);
    private static ElasticsearchSession instance = null;
    private Node node = null;
    private Client client = null;

    private ElasticsearchSession(Settings settings) {
        node = nodeBuilder()
                .settings(settings)
                .settings(Settings.settingsBuilder().put("http.enabled", false))
                .client(true)
                .node();
        client = node.client();

        LOGGER.debug("New Elasticsearch session was created with settings = {}", node.settings().getAsMap());
    }

    public static synchronized ElasticsearchSession getInstance(Settings settings) {
        if (instance == null) instance = new ElasticsearchSession(settings);
        return instance;
    }

    public Client getClient() {
        return client;
    }

    public static synchronized void shutdown() {
        if (instance != null) {
            instance.client.close();
            instance.node.close();
            instance = null;
            LOGGER.debug("Elasticsearch session has been closed!");
        }
    }
}
