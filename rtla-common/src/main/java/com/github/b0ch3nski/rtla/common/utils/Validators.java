package com.github.b0ch3nski.rtla.common.utils;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.function.Predicate;

/**
 * @author bochen
 */
public final class Validators {
    private static final String NOT_NULL = " cannot be null!";
    private static final String NOT_NULL_OR_EMPTY = " cannot be null or empty!";
    private static final String GT_THAN_0 = " must be greater than zero!";

    private Validators() { }

    public static <T> T isNotNull(T toValidate, String varName) {
        Preconditions.checkArgument((toValidate != null), varName + NOT_NULL);
        return toValidate;
    }

    public static String isNotNullOrEmpty(String toValidate, String varName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(toValidate), varName + NOT_NULL_OR_EMPTY);
        return toValidate;
    }

    public static int isGreaterThanZero(int toValidate, String varName) {
        Preconditions.checkArgument(toValidate > 0, varName + GT_THAN_0);
        return toValidate;
    }

    public static Predicate<SimplifiedLog> isTimestampAround(long expected) {
        return log -> (log.getTimeStamp() >= (expected - 10000)) && (log.getTimeStamp() <= (expected + 10000));
    }
}
