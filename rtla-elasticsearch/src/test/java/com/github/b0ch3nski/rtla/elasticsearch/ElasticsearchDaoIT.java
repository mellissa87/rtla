package com.github.b0ch3nski.rtla.elasticsearch;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;

import java.io.File;
import java.nio.file.Files;

/**
 * @author bochen
 */
public class ElasticsearchDaoIT {

    @ClassRule
    public static final ExternalResource RESOURCE = new ExternalResource() {
        private Node node;

        @Override
        protected void before() throws Throwable {
            File temp = Files.createTempDirectory("es-test").toFile();
            temp.deleteOnExit();

            Settings settings = Settings.settingsBuilder()
                    .put("cluster.name", "es-test")
                    .put("http.enabled", "false")
                    .put("path.home", temp.getAbsolutePath())
                    .put("path.data", temp.getAbsolutePath())
                    .build();

            node = NodeBuilder.nodeBuilder()
                    .local(true)
                    .settings(settings)
                    .node();
        }

        @Override
        protected void after() {
            node.close();
        }
    };
}
