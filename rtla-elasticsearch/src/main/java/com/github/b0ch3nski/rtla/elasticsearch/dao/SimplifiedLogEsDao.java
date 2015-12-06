package com.github.b0ch3nski.rtla.elasticsearch.dao;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.serialization.JsonMapperFactory;
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

    public SimplifiedLogEsDao(Settings settings) {
        super(settings, CLASS_TYPE, JsonMapperFactory.getForSimplifiedLog(), INDEX_NAME, TYPE_NAME);
    }

    @Override
    protected XContentBuilder provideMapping() throws IOException {
        return jsonBuilder()
                .startObject()
                    .startObject(TYPE_NAME)
                        .startObject("properties")
                            .startObject("formattedMessage").field("type", "string").endObject()
                            .startObject("hostName").field("type", "string").endObject()
                            .startObject("level").field("type", "string").endObject()
                            .startObject("loggerName").field("type", "string").endObject()
                            .startObject("threadName").field("type", "string").endObject()
                            .startObject("timeStamp").field("type", "date").endObject()
                        .endObject()
                    .endObject()
                .endObject();
    }
}
