package com.github.b0ch3nski.rtla.storm.utils;

/**
 * @author bochen
 */
public enum FieldNames {
    HOST("host"),
    LEVEL("level"),
    LOG("serialized-log"),
    FRAME("serialized-frame");

    private final String name;

    FieldNames(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
