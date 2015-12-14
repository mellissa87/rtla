package com.github.b0ch3nski.rtla.storm.utils;

/**
 * @author bochen
 */
public enum FieldNames {
    HOST("host"),
    TIME("timestamp"),
    LEVEL("level"),
    LOG("serialized-log");

    private final String name;

    FieldNames(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
