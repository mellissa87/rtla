package com.github.b0ch3nski.rtla.simulator.utils;

import com.google.common.base.MoreObjects;

import java.util.Objects;

/**
 * @author bochen
 */
public final class SimulatorConfig {
    private String inputDir = "";
    private int delay = 1000;
    private int loops = 0;

    public String getInputDir() {
        return inputDir;
    }

    public void setInputDir(String inputDir) {
        this.inputDir = inputDir;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getLoops() {
        return loops;
    }

    public void setLoops(int loops) {
        this.loops = loops;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("inputDir", inputDir)
                .add("delay", delay)
                .add("loops", loops)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;
        SimulatorConfig config = (SimulatorConfig) o;
        return (delay == config.delay) &&
                (loops == config.loops) &&
                (inputDir.equals(config.inputDir));
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputDir, delay, loops);
    }
}
