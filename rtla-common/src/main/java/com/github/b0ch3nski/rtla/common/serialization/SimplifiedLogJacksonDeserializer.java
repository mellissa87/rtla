package com.github.b0ch3nski.rtla.common.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog.SimplifiedLogBuilder;

import java.io.IOException;

/**
 * @author bochen
 */
public class SimplifiedLogJacksonDeserializer extends JsonDeserializer<SimplifiedLog> {

    @Override
    public SimplifiedLog deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);

        return new SimplifiedLogBuilder()
                .withTimeStamp((Long) node.get("timeStamp").numberValue())
                .withHostName(node.get("hostName").asText())
                .withLevel(node.get("level").asText())
                .withThreadName(node.get("threadName").asText())
                .withLoggerName(node.get("loggerName").asText())
                .withFormattedMessage(node.get("formattedMessage").asText())
                .build();
    }
}
