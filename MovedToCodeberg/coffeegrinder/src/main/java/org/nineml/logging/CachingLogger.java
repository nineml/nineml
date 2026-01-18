package org.nineml.logging;

import java.util.ArrayList;
import java.util.List;

/**
 * The caching logger stores all of the messages.
 * <p>You can retrieve them by calling <code>getMessages</code>.</p>
 */
public class CachingLogger extends Logger {
    private final ArrayList<String> messages = new ArrayList<>();

    public List<String> getMessages() {
        return messages;
    }

    public void clearMessages() {
        messages.clear();
    }

    public void error(String category, String format, Object... params) {
        if (getLogLevel(category) >= ERROR) {
            messages.add(message(category, ERROR, format, params));
        }
    }

    public void warn(String category, String format, Object... params) {
        if (getLogLevel(category) >= WARNING) {
            messages.add(message(category, WARNING, format, params));
        }
    }

    public void info(String category, String format, Object... params) {
        if (getLogLevel(category) >= INFO) {
            messages.add(message(category, INFO, format, params));
        }
    }

    public void debug(String category, String format, Object... params) {
        if (getLogLevel(category) >= DEBUG) {
            messages.add(message(category, DEBUG, format, params));
        }
    }

    public void trace(String category, String format, Object... params) {
        if (getLogLevel(category) >= TRACE) {
            messages.add(message(category, TRACE, format, params));
        }
    }
}
