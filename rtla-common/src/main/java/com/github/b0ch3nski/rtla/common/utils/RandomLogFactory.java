package com.github.b0ch3nski.rtla.common.utils;

import ch.qos.logback.classic.Level;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog.SimplifiedLogBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.net.InetAddresses;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;
import java.util.Random;

/**
 * @author bochen
 */
public final class RandomLogFactory {
    private static final Random RAND = new Random();

    private RandomLogFactory() { }

    private static int randomIntBetween(int lower, int upper) {
        return (int) (Math.random() * (upper - lower)) + lower;
    }

    public static SimplifiedLog create() {
        return create(RandomStringUtils.randomAlphanumeric(5));
    }

    public static SimplifiedLog create(Level level) {
        return create(level.toString());
    }

    public static SimplifiedLog create(String level) {
        return new SimplifiedLogBuilder()
                .withTimeStamp(System.currentTimeMillis())
                .withHostName(InetAddresses.fromInteger(RAND.nextInt()).getHostAddress())
                .withLevel(level)
                .withThreadName(RandomStringUtils.randomAlphanumeric(randomIntBetween(10, 20)))
                .withLoggerName(RandomStringUtils.randomAlphanumeric(randomIntBetween(10, 20)))
                .withFormattedMessage(RandomStringUtils.randomAlphanumeric(randomIntBetween(50, 150)))
                .build();
    }

    public static List<SimplifiedLog> create(int amount) {
        Builder<SimplifiedLog> builder = ImmutableList.builder();

        for (int i = 0; i < amount; i++) {
            builder.add(create());
        }
        return builder.build();
    }
}
