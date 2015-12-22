package com.github.b0ch3nski.rtla.storm.utils;

import backtype.storm.task.TopologyContext;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.github.b0ch3nski.rtla.common.metrics.MetricsHandler;

/**
 * @author bochen
 */
public final class StormMetricsFactory {

    private StormMetricsFactory() { }

    public static Meter createMeter(TopologyContext context, String... name) {
        String instanceId = context.getThisComponentId() + context.getThisTaskId();
        String meterName = MetricRegistry.name(instanceId, name);

        return MetricsHandler.getInstance().getRegistry().meter(meterName);
    }
}
