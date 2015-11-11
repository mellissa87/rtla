package com.github.b0ch3nski.rtla.common.utils;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.Reader;

/**
 * @author bochen
 */
public final class ConfigFactory {

    private ConfigFactory() { }

    private static Object loadConfig(Reader configFile, Class cls) {
        Constructor constructor = new Constructor(cls);
        TypeDescription configType = new TypeDescription(cls);
        constructor.addTypeDescription(configType);
        return cls.cast(
                new Yaml(constructor).load(configFile)
        );
    }

    public static Object fromYaml(Reader configFile, Class cls) {
        try {
            return loadConfig(configFile, cls);
        } catch (YAMLException | ClassCastException e) {
            throw new IllegalStateException("Config file doesn't match config class " + cls.getSimpleName(), e);
        }
    }
}
