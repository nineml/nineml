package org.nineml.logging;

import java.util.HashMap;
import java.util.Set;

/**
 * The abstract class that all concrete loggers must extend.
 */
public abstract class Logger {
    /**
     * The logging catagory for logger messages.
     */
    public static final String logcategory = "Logger";

    /**
     * The system property for setting the default log level.
     */
    public static final String defaultLogLevelProperty = "org.nineml.logging.defaultLogLevel";

    /**
     * The system property for setting the category:loglevel mappings.
     */
    public static final String logLevelsProperty = "org.nineml.logging.logLevels";

    /**
     * The log level to indicate no logging, not even errors.
     */
    public static final int SILENT = 0;
    /**
     * The log level for error messages.
     */
    public static final int ERROR = 1;
    /**
     * The log level for warning messages.
     */
    public static final int WARNING = 2;
    /**
     * The log level for informational messages.
     */
    public static final int INFO = 3;
    /**
     * The log level for debug messages.
     */
    public static final int DEBUG = 4;
    /**
     * The log level for trace messages.
     */
    public static final int TRACE = 5;

    private static final HashMap<String,Integer> levelNames = new HashMap<>();
    static {
        levelNames.put("silent", SILENT);
        levelNames.put("error", ERROR);
        levelNames.put("warning", WARNING);
        levelNames.put("info", INFO);
        levelNames.put("debug", DEBUG);
        levelNames.put("trace", TRACE);
    }

    private int defaultLogLevel = ERROR;

    private final HashMap<String,Integer> logLevels = new HashMap<>();

    /**
     * Set the log levels by reading system properties.
     * <p>The properties are:</p>
     * <ul>
     *     <li>{@link #defaultLogLevelProperty}
     *     <p>Must be an integer specifying the initial default log level or one of the strings
     *     "silent", "error", "warning", "info", "debug", or "trace". Absent this property,
     *     the default level is 5, or "error".</p>
     *     </li>
     *     <li>{@link #logLevelsProperty}
     *     <p>This property specifies a mapping between log categories and the log level for each category.
     *     The format of the property is a list of comma or space separated values of the form
     *     "category:level". The category "*" sets the default log level.</p>
     *     </li>
     * </ul>
     */
    public void readSystemProperties() {
        String value = System.getProperty(defaultLogLevelProperty);
        if (value != null) {
            defaultLogLevel = logLevelNumber(value);
        }

        value = System.getProperty(logLevelsProperty);
        if (value != null) {
            setLogLevels(value);
        }
    }

    private int logLevelNumber(String name) {
        if (Character.isDigit(name.charAt(0))) {
            try {
                int value = Integer.parseInt(name);
                return Math.max(0, value);
            } catch (NumberFormatException ex) {
                error(logcategory, "Failed to parse log level: %s", name);
                return ERROR;
            }
        }

        if (levelNames.containsKey(name)) {
            return levelNames.get(name);
        }

        error(logcategory, "Unknown log level specified: %s", name);
        return ERROR;
    }

    /**
     * Get the default log level
     * @return the default log level
     */
    public int getDefaultLogLevel() {
        return defaultLogLevel;
    }

    /**
     * Set the default log level.
     * <p>If the level is less than zero, it will be set to 0 (silent).</p>
     * @param level the level
     */
    public void setDefaultLogLevel(int level) {
        defaultLogLevel = Math.max(0, level);
    }

    /**
     * Set the default log level.
     * <p>The level must be "silent", "error", "warning", "info", "debug", or "trace". If an invalid
     * value is specified, "error" is used.</p>
     * @param level the level.
     * @throws NullPointerException if the level is null.
     */
    public void setDefaultLogLevel(String level) {
        if (level == null) {
            throw new NullPointerException("The level must not be null");
        }
        setDefaultLogLevel(logLevelNumber(level));
    }

    /**
     * Get all of the configured log level categories
     * @return the set of categories
     */
    public Set<String> getLogCategories() {
        return logLevels.keySet();
    }

    /**
     * Get the log level for a particular category.
     * <p>Category names are not case sensitive.</p>
     * @param category the category
     * @return the level
     * @throws NullPointerException if the category is null
     */
    public int getLogLevel(String category) {
        if (category == null) {
            throw new NullPointerException("The category must not be null");
        }
        return logLevels.getOrDefault(category.toLowerCase(), defaultLogLevel);
    }

    /**
     * Set the log level for a particular category.
     * @param category the category
     * @param level the level
     * @throws NullPointerException if the category is null.
     */
    public void setLogLevel(String category, int level) {
        if (category == null) {
            throw new NullPointerException("The category must not be null");
        }
        logLevels.put(category.toLowerCase(), Math.max(0, level));
    }

    /**
     * Set the log level for a particular category.
     * <p>The level must be "silent", "error", "warning", "info", "debug", or "trace". If an invalid
     * value is specified, "error" is used.</p>
     * @param category the category.
     * @param level the level.
     * @throws NullPointerException if the category or level is null.
     */
    public void setLogLevel(String category, String level) {
        setLogLevel(category, logLevelNumber(level));
    }

    /**
     * Set the log levels for a set of categories.
     * <p>The <code>config</code> specifies a mapping between log categories and the log level for each category.
     * The format of the string is a list of comma or space separated values of the form
     * "category:level".</p>
     * <p>The level must be an integer or one of "silent", "error", "warning", "info", "debug", or "trace". If an invalid
     * value is specified, "error" is used. The category "*" sets the default log level.</p>
     * @param config the category.
     */
    public void setLogLevels(String config) {
        if (config == null) {
            return;
        }

        for (String pair : config.split("[,\\s]+")) {
            if (pair.contains(":")) {
                int pos = pair.indexOf(":");
                String name = pair.substring(0, pos);
                String value = pair.substring(pos+1);
                if ("*".equals(name)) {
                    setDefaultLogLevel(value);
                } else {
                    setLogLevel(name, value);
                }
            } else {
                error(logcategory, "Cannot parse log level setting: %s", pair);
            }
        }
    }

    /**
     * Clear the log levels.
     * <p>This method removes all configured log levels. All subsequent logging (until more
     * levels are set) will be based entirely on the default log level.</p>
     */
    public void clearLogLevels() {
        logLevels.clear();
    }

    protected String message(String category, int level, String message, Object... params) {
        StringBuilder sb = new StringBuilder();
        switch (level) {
            case ERROR:
                sb.append("E: ");
                break;
            case WARNING:
                sb.append("W: ");
                break;
            case INFO:
                sb.append("I: ");
                break;
            case DEBUG:
                sb.append("D: ");
                break;
            case TRACE:
                sb.append("T: ");
                break;
            default:
                sb.append("X: ");
                break;
        }

        sb.append(String.format(message, params));
        return sb.toString();
    }

    /** Issue an error message.
     * @param category the message category
     * @param format the format string
     * @param params message parameters
     */
    public abstract void error(String category, String format, Object... params);

    /** Issue a warning message.
     * @param category the message category
     * @param format the format string
     * @param params message parameters
     */
    public abstract void warn(String category, String format, Object... params);

    /** Issue an informational message.
     * @param category the message category
     * @param format the format string
     * @param params message parameters
     */
    public abstract void info(String category, String format, Object... params);

    /** Issue a debug message.
     * @param category the message category
     * @param format the format string
     * @param params message parameters
     */
    public abstract void debug(String category, String format, Object... params);

    /** Issue a trace message.
     * @param category the message category
     * @param format the format string
     * @param params message parameters
     */
    public abstract void trace(String category, String format, Object... params);
}
