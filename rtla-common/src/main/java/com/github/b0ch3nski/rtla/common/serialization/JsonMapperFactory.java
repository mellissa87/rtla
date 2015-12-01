package com.github.b0ch3nski.rtla.common.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;

/**
 * @author bochen
 */
public final class JsonMapperFactory {

    private JsonMapperFactory() { }

    public static ObjectMapper getForSimplifiedLog() {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(SimplifiedLog.class, new SimplifiedLogJacksonDeserializer());

        mapper.registerModule(module);
        return mapper;
    }
}
