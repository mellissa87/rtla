package com.github.b0ch3nski.rtla.elasticsearch.dao;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * @author bochen
 */
public final class SimplifiedLogEsDao extends BaseEsDao<SimplifiedLog> {

    private static final Class<SimplifiedLog> CLASS_TYPE = SimplifiedLog.class;
    private static final String INDEX_NAME = "logs";
    private static final String TYPE_NAME = "log";
    private final String ttl;

    public SimplifiedLogEsDao(Settings settings) {
        super(settings, CLASS_TYPE, INDEX_NAME, TYPE_NAME);
        ttl = settings.get("default.ttl");
    }

    @Override
    protected XContentBuilder provideMapping() throws IOException {
        // @formatter:off
        return jsonBuilder()
                .startObject()
                    .startObject(TYPE_NAME)
                        .field("dynamic", false)
                        .startObject("_ttl")
                            .field("enabled", true)
                            .field("default", ttl)
                        .endObject()
                        .startObject("properties")
                            .startObject("timeStamp")
                                .field("type", "date")
                                .field("format", "epoch_millis")
                            .endObject()
                            .startObject("hostName")
                                .field("type", "string")
                                .field("index", "not_analyzed")
                            .endObject()
                            .startObject("level")
                                .field("type", "string")
                                .field("index", "not_analyzed")
                            .endObject()
                            .startObject("loggerName")
                                .field("type", "string")
                            .endObject()
                            .startObject("threadName")
                                .field("type", "string")
                            .endObject()
                            .startObject("formattedMessage")
                                .field("type", "string")
                            .endObject()
                        .endObject()
                    .endObject()
                .endObject();
        // @formatter:on
    }
}
