package com.github.b0ch3nski.rtla.elasticsearch;

import com.github.b0ch3nski.rtla.common.utils.Validators;
import org.elasticsearch.common.settings.Settings;

import java.util.Map;

/**
 * @author bochen
 */
public final class ElasticsearchConfigBuilder {

    private String clusterName;
    private String hostName;
    private String bulkActions;
    private String bulkSize;
    private String flushTime;

    public ElasticsearchConfigBuilder fromStormConf(Map stormConf) {
        Map config = (Map) stormConf.get("elasticsearch.config");

        clusterName = (String) config.get("elasticsearch.cluster");
        hostName = (String) config.get("elasticsearch.host");
        bulkActions = (String) config.get("elasticsearch.bulk.actions");
        bulkSize = (String) config.get("elasticsearch.bulk.size");
        flushTime = (String) config.get("elasticsearch.flush.time");
        return this;
    }

    public ElasticsearchConfigBuilder withClusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }

    public ElasticsearchConfigBuilder withHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public ElasticsearchConfigBuilder withBulkActions(String bulkActions) {
        this.bulkActions = bulkActions;
        return this;
    }

    public ElasticsearchConfigBuilder withBulkSize(String bulkSize) {
        this.bulkSize = bulkSize;
        return this;
    }

    public ElasticsearchConfigBuilder withFlushTime(String flushTime) {
        this.flushTime = flushTime;
        return this;
    }

    public Settings build() {
        Validators.isNotNullOrEmpty(clusterName, "clusterName");
        Validators.isNotNullOrEmpty(hostName, "hostName");
        Validators.isNotNullOrEmpty(bulkActions, "bulkActions");
        Validators.isNotNullOrEmpty(bulkSize, "bulkSize");
        Validators.isNotNullOrEmpty(flushTime, "flushTime");

        return Settings.settingsBuilder()
                .put("cluster.name", clusterName)
                .put("network.host", hostName)
                .put("bulk.actions", bulkActions)
                .put("bulk.size", bulkSize)
                .put("flush.time", flushTime)
                .build();
    }
}
