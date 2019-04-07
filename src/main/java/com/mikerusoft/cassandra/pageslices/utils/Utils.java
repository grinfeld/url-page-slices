package com.mikerusoft.cassandra.pageslices.utils;

import com.mikerusoft.cassandra.pageslices.model.jpa.Slice;

public final class Utils {
    private Utils() {}

    public static RuntimeException rethrow(Throwable t) throws RuntimeException {
        if (t instanceof Error)
            throw (Error)t;
        else if (t instanceof RuntimeException)
            throw (RuntimeException)t;
        else throw new RuntimeException(t);
    }

    private static final class ERROR_SLICE extends Slice {
        @Override
        public boolean equals(Object o) {
            return o == this;
        }
    }

    private static final Slice ERROR_SLICE = new ERROR_SLICE();

    public static Slice errorSlice() {
        return ERROR_SLICE;
    }

    public static boolean isError(Slice s) {
        return ERROR_SLICE.equals(s);
    }

}
