package com.github.b0ch3nski.rtla.common.utils;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog.SimplifiedLogBuilder;
import com.google.common.net.InetAddresses;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;

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
        return new SimplifiedLogBuilder()
                .withTimeStamp(System.currentTimeMillis())
                .withHostName(InetAddresses.fromInteger(RAND.nextInt()).getHostAddress())
                .withLevel(RandomStringUtils.randomAlphanumeric(5))
                .withThreadName(RandomStringUtils.randomAlphanumeric(randomIntBetween(10, 20)))
                .withLoggerName(RandomStringUtils.randomAlphanumeric(randomIntBetween(10, 20)))
                .withFormattedMessage(RandomStringUtils.randomAlphanumeric(randomIntBetween(20, 100)))
                .build();
    }

    public static Collection<SimplifiedLog> create(int amount) {
        Collection<SimplifiedLog> toReturn = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            toReturn.add(create());
        }
        return toReturn;
    }
}
