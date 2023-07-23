package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.parser.ParseListener;

/**
 * A default implementation of {@link ParseListener}.
 *
 * <p>This implementation writes messages to <code>System.err</code>.</p>
 */

public class DefaultParseListener implements ParseListener {
    public int level;

    /**
     * Create a default listener.
     * <p>The default listener only reports errors.</p>
     */
    public DefaultParseListener() {
        level = ERROR;
    }

    /**
     * Create a default listener with a particular message level.
     * @param level The level.
     */
    public DefaultParseListener(int level) {
        setMessageLevel(level);
    }

    @Override
    public void debug(String message) {
        if (level >= DEBUG) {
            System.err.println(message);
        }
    }

    @Override
    public void detail(String message) {
        if (level >= DETAIL) {
            System.err.println(message);
        }
    }

    @Override
    public void info(String message) {
        if (level >= INFO) {
            System.err.println(message);
        }
    }

    @Override
    public void warning(String message) {
        if (level >= WARNING) {
            System.err.println(message);
        }
    }

    @Override
    public void error(String message) {
        if (level >= ERROR) {
            System.err.println(message);
        }
    }

    @Override
    public int getMessageLevel() {
        return level;
    }

    @Override
    public void setMessageLevel(int level) {
        this.level = level;
        if (this.level > NONE) {
            this.level = NONE;
        }
    }
}
