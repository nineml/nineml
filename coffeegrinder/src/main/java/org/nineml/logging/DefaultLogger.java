package org.nineml.logging;

/**
 * The default logger prints messages on stderr.
 */
public class DefaultLogger extends Logger {
    public void error(String category, String format, Object... params) {
        if (getLogLevel(category) >= ERROR) {
            System.err.println(message(category, ERROR, format, params));
        }
    }

    public void warn(String category, String format, Object... params) {
        if (getLogLevel(category) >= WARNING) {
            System.err.println(message(category, WARNING, format, params));
        }
    }

    public void info(String category, String format, Object... params) {
        if (getLogLevel(category) >= INFO) {
            System.err.println(message(category, INFO, format, params));
        }
    }

    public void debug(String category, String format, Object... params) {
        if (getLogLevel(category) >= DEBUG) {
            System.err.println(message(category, DEBUG, format, params));
        }
    }

    public void trace(String category, String format, Object... params) {
        if (getLogLevel(category) >= TRACE) {
            System.err.println(message(category, TRACE, format, params));
        }
    }
}
