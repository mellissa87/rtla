package com.github.b0ch3nski.rtla.common.metrics;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;

/**
 * @author bochen
 */
public enum MetricsHandler {

    INSTANCE;

    private final MetricRegistry registry;
    private final JmxReporter reporter;

    MetricsHandler() {
        registry = new MetricRegistry();

        reporter = JmxReporter.forRegistry(registry).build();
        reporter.start();
    }

    public static MetricsHandler getInstance() {
        return INSTANCE;
    }

    public MetricRegistry getRegistry() {
        return registry;
    }

    public void shutdown() {
        reporter.close();
    }
}
