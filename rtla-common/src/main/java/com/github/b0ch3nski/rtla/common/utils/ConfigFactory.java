package com.github.b0ch3nski.rtla.common.utils;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.Reader;

/**
 * @author bochen
 */
public final class ConfigFactory {

    private ConfigFactory() { }

    public static Object fromYaml(Reader configFile, Class cls) {
        Constructor constructor = new Constructor(cls);
        TypeDescription configType = new TypeDescription(cls);
        constructor.addTypeDescription(configType);

        try {
            return cls.cast(
                    new Yaml(constructor).load(configFile)
            );
        } catch (ClassCastException e) {
            throw new IllegalStateException("Config file doesn't match config class!", e);
        }
    }
}
