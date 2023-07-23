package org.nineml.logging;

import org.slf4j.LoggerFactory;

/**
 * The system logger routes messages to another logging framework.
 *
 * <p>This class supports either configuration with {@link org.slf4j.Logger org.slf4j.Logger} or
 * configuration directly with a {@link java.util.logging.Logger java.util.logging.Logger}.</p>
 *
 * <p>This logger makes it easy to configure logging through a standard
 * logging framework, as might be present on a Java application server. By default
 * the logger uses the {@link org.slf4j.LoggerFactory org.slf4j.LoggerFactory} to create a logger. This logger
 * can be supported at runtime by a wide variety of concrete backend classes. For details
 * on how SLF4J finds a logging backend, see their documentation.</p>
 *
 * <p>Alternatively, if you instantiate the <code>SystemLogger</code> with a
 * {@link java.util.logging.Logger java.util.logging.Logger} directly, it will use that.</p>
 */
public class SystemLogger extends Logger {
    private final org.slf4j.Logger logger;
    private final java.util.logging.Logger jlogger;

    public SystemLogger() {
        logger = LoggerFactory.getLogger(SystemLogger.class);
        jlogger = null;
    }

    public SystemLogger(java.util.logging.Logger logger) {
        this.logger = null;
        jlogger = logger;
    }

    public void error(String category, String format, Object... params) {
        if (getLogLevel(category) >= ERROR) {
            String logmessage = message(category, ERROR, format, params);
            if (jlogger != null) {
                jlogger.severe(logmessage);
            } else {
                logger.error(logmessage);
            }
        }
    }

    public void warn(String category, String format, Object... params) {
        if (getLogLevel(category) >= WARNING) {
            String logmessage = message(category, WARNING, format, params);
            if (jlogger != null) {
                jlogger.warning(logmessage);
            } else {
                logger.warn(logmessage);
            }
        }
    }

    public void info(String category, String format, Object... params) {
        if (getLogLevel(category) >= INFO) {
            String logmessage = message(category, INFO, format, params);
            if (jlogger != null) {
                jlogger.info(logmessage);
            } else {
                logger.info(logmessage);
            }
        }
    }

    public void debug(String category, String format, Object... params) {
        if (getLogLevel(category) >= DEBUG) {
            String logmessage = message(category, DEBUG, format, params);
            if (jlogger != null) {
                jlogger.fine(logmessage);
            } else {
                logger.debug(logmessage);
            }
        }
    }

    public void trace(String category, String format, Object... params) {
        if (getLogLevel(category) >= TRACE) {
            String logmessage = message(category, TRACE, format, params);
            if (jlogger != null) {
                jlogger.fine(logmessage);
            } else {
                logger.debug(logmessage);
            }
        }
    }
}
