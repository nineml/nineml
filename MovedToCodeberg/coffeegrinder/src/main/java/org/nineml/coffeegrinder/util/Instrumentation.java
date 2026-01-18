package org.nineml.coffeegrinder.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Instrumentation {
    public static final boolean ACTIVE = false;
    public static final HashMap<String,Long> counters = new HashMap<>();
    public static long start = new Date().getTime();

    public static void count(String format, Object... params) {
        if (ACTIVE) {
            String key = String.format(format, params);
            long counter = counters.getOrDefault(key, 0L);
            counters.put(key, counter+1);
        }
    }

    public static String callStack() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        return stacktrace[3].getClassName() + "." + stacktrace[3].getMethodName() +
                " from " + stacktrace[4].getClassName() + "." + stacktrace[4].getMethodName() +
                " from " + stacktrace[5].getClassName() + "." + stacktrace[5].getMethodName();
    }

    public static void report() {
        report(0);
    }

    public static void report(long threshold) {
        if (ACTIVE) {
            long end = new Date().getTime();
            System.err.printf("Instrumentation active for %2.3fs%n", (end - start) / 1000.0);
            if (!counters.isEmpty()) {
                if (threshold > 0) {
                    System.err.printf("Instrumentation counters (> %d):%n", threshold);
                } else {
                    System.err.println("Instrumentation counters:");
                }
                for (Map.Entry<String, Long> c : counters.entrySet()) {
                    if (c.getValue() > threshold) {
                        System.err.printf("%04d %s%n", c.getValue(), c.getKey());
                    }
                }
            }
        }
    }

    // Reset all counters
    public static void reset() {
        if (ACTIVE) {
            start = new Date().getTime();
            counters.clear();
        }
    }
}
