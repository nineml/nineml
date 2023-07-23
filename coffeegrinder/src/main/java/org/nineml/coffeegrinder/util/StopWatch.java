package org.nineml.coffeegrinder.util;

import java.util.Calendar;

/**
 * A utility class for managing a "wall clock" timer.
 * <p>The timer starts when you create the object. Subsequent calls will return information
 * based on the elapsed wall-clock time since the object was created. If <code>stop</code>
 * is called, it will fix the end time.</p>
 */
public class StopWatch {
    private static final int MS = 1000;
    private static final int MINMS = MS * 60;
    private static final int HOURMS = MINMS * 60;
    private static final int DAYMS = HOURMS * 24;

    private final long startTime;
    private long endTime;

    /**
     * Create a stopwatch.
     * <p>Time begins counting immediately.</p>
     */
    public StopWatch() {
        startTime = Calendar.getInstance().getTimeInMillis();
        endTime = 0;
    }

    /**
     * Stop the watch.
     * <p>After calling this method, the time will remain fixed.</p>
     */
    public void stop() {
        endTime = Calendar.getInstance().getTimeInMillis();
    }

    /**
     * The number of milliseconds that the stopwatch has been running.
     * <p>Or, if <code>stop</code> has been called, how long it ran.</p>
     * @return the number of milliseconds
     */
    public long duration() {
        if (endTime != 0) {
            return endTime - startTime;
        }
        return Calendar.getInstance().getTimeInMillis() - startTime;
    }

    /**
     * Compute events per second.
     * <p>Computes events per second for the given number of events. It returns
     * a string of the form <code>%3.1f</code>.</p>
     * @param events The number of events
     * @return A formatted string
     */
    public String perSecond(long events) {
        if (duration() == 0) {
            return "∞";
        }
        return String.format("%3.1f", (1.0*events) / (duration() / 1000.0));
    }

    /**
     * The elapsed time in a human-friendly format.
     * <p>This method uses the timer's elapsed time.</p>
     * @return A formatted string
     */
    public String elapsed() {
        return elapsed(duration());
    }

    /**
     * The elapsed time in a human-friendly format.
     * <p>Returns the number of days, minutes, hours, and seconds represented by
     * the duration, a number of milliseconds. For example,
     * 103,010,000ms is "1d4h36m50s".</p>
     * @param duration the duration in milliseconds
     * @return A formatted string
     */
    public String elapsed(long duration) {
        long days = floorDiv(duration, DAYMS);

        long left = duration - (days * DAYMS);
        long hours = floorDiv(left, HOURMS);
        left = left - (hours * HOURMS);

        long minutes = floorDiv(left, MINMS);
        left = left - (minutes * MINMS);

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d");
        }
        if (days > 0 || hours > 0) {
            sb.append(hours).append("h");
        }
        if (days > 0 || hours > 0 || minutes > 0) {
            sb.append(minutes).append("m");
        }
        sb.append(String.format("%1.2f", (1.0*left/1000.0))).append("s");

        return sb.toString();
    }

    // Stolen from java.lang.Math version 9+
    private long floorDiv(long x, long y) {
        long r = x / y;
        // if the signs are different and modulo not zero, round down
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }
}
