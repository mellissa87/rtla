package com.github.b0ch3nski.rtla.elasticsearch;

import org.elasticsearch.action.admin.cluster.node.info.*;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
                .settings(Settings.settingsBuilder()
                                .put("network.host", "0.0.0.0")
                                .put("http.enabled", false)
                                .put("path.home", createTmpDir("es-client"))
                                .put("discovery.zen.ping.multicast.enabled", false)
                )
                .client(true)
                .node();
        client = node.client();

        if (LOGGER.isInfoEnabled()) getInfo();
    }

    private String createTmpDir(String path) {
        File temp;
        try {
            temp = Files.createTempDirectory(path).toFile();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't create temporary home dir for ES client", e);
        }

        temp.deleteOnExit();
        return temp.getAbsolutePath();
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

    public static synchronized void shutdown() {
        if (instance != null) {
            instance.client.close();
            instance.node.close();
            instance = null;
            LOGGER.info("Elasticsearch session has been closed!");
        }
    }
}
