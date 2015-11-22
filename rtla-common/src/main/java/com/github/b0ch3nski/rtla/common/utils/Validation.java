package com.github.b0ch3nski.rtla.common.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * @author bochen
 */
public final class Validation {
    private static final String NOT_NULL = " cannot be null!";
    private static final String NOT_NULL_OR_EMPTY = " cannot be null or empty!";

    private Validation() { }

    public static void isNotNull(Object toValidate, String varName) {
        Preconditions.checkArgument((toValidate != null), varName + NOT_NULL);
    }

    public static void isNotNullOrEmpty(String toValidate, String varName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(toValidate), varName + NOT_NULL_OR_EMPTY);
    }
}
