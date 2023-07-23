package org.nineml.coffeesacks;

import org.nineml.logging.Logger;

/**
 * A logger adapter between {@link org.nineml.logging.Logger} and
 * {@link net.sf.saxon.lib.Logger}.
 */
public class SacksLogger extends Logger {
    private final net.sf.saxon.lib.Logger logger;
    
    public SacksLogger(net.sf.saxon.lib.Logger logger) {
        this.logger = logger;
    }
    
    @Override
    public void error(String category, String format, Object... params) {
        if (getLogLevel(category) > Logger.ERROR) {
            logger.error(message(category, Logger.ERROR, format, params));
        }
    }

    @Override
    public void warn(String category, String format, Object... params) {
        if (getLogLevel(category) > Logger.WARNING) {
            logger.warning(message(category, Logger.WARNING, format, params));
        }
    }

    @Override
    public void info(String category, String format, Object... params) {
        if (getLogLevel(category) > Logger.INFO) {
            logger.info(message(category, Logger.INFO, format, params));
        }
    }

    @Override
    public void debug(String category, String format, Object... params) {
        if (getLogLevel(category) > Logger.DEBUG) {
            logger.info(message(category, Logger.DEBUG, format, params));
        }
    }

    @Override
    public void trace(String category, String format, Object... params) {
        if (getLogLevel(category) > Logger.TRACE) {
            logger.info(message(category, Logger.TRACE, format, params));
        }
    }
}
