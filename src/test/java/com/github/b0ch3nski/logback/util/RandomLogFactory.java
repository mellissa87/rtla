package com.github.b0ch3nski.logback.util;

import com.github.b0ch3nski.logback.model.SimplifiedLog;
import com.github.b0ch3nski.logback.model.SimplifiedLog.SimplifiedLogBuilder;
import com.google.common.net.InetAddresses;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;

/**
 * @author bochen
 */
public final class RandomLogFactory {
    private static final Random RAND = new Random();

    private RandomLogFactory() { }

    public static SimplifiedLog create() {
        return new SimplifiedLogBuilder()
                .withTimeStamp(System.currentTimeMillis())
                .withHostName(InetAddresses.fromInteger(RAND.nextInt()).getHostAddress())
                .withLevel(RandomStringUtils.randomAlphanumeric(5))
                .withThreadName(RandomStringUtils.randomAlphanumeric(15))
                .withLoggerName(RandomStringUtils.randomAlphanumeric(15))
                .withFormattedMessage(RandomStringUtils.randomAlphanumeric(100))
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
