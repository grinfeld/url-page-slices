package com.mikerusoft.cassandra.pageslices.utils;

public final class Utils {
    private Utils() {}

    public static RuntimeException rethrow(Throwable t) throws RuntimeException {
        if (t instanceof Error)
            throw (Error)t;
        else if (t instanceof RuntimeException)
            throw (RuntimeException)t;
        else throw new RuntimeException(t);
    }
}
