package org.nineml.coffeegrinder.parser;

/**
 * The parser uses a ParseListener to report on its progress.
 *
 * <p>There's no particularly rigerous definition for the various levels.</p>
 *
 * <p>I suppose it would be a little cleaner to make the message levels an enumeration,
 * but I didn't think it was worth the extra clutter.</p>
 */
public interface ParseListener {
    /** Error messages only. */
    static final int ERROR = 1;
    /** Warning messages and above. */
    static final int WARNING = 2;
    /** Informational messages and above. */
    static final int INFO = 3;
    /** Detail messages and above. */
    static final int DETAIL= 4;
    /** All messages. */
    static final int DEBUG= 5;
    /** No messages. */
    static final int NONE = 6;

    /**
     * Emit a debug message.
     * <p>Tell me <em>everything</em> about the parse.</p>
     * @param message The message.
     */
    void debug(String message);

    /**
     * Emit a detail message.
     * <p>Tell me about the details of the parse.</p>
     * @param message The message.
     */
    void detail(String message);

    /**
     * Emit an info message.
     * <p>Keep me informed about the process of the parse.</p>
     * @param message The message.
     */
    void info(String message);

    /**
     * Emit a warning message.
     * <p>Tell if something looks fishy.</p>
     * @param message The message.
     */
    void warning(String message);

    /**
     * Emit an error message.
     * <p>Tell me if there's been an error of some sort.</p>
     * @param message The message.
     */
    void error(String message);

    /**
     * How verbose are we being?
     * <p>This returns a level, one of the constants defined in this interface.</p>
     * @return The current message level.
     */
    public int getMessageLevel();

    /**
     * How verbose do you want me to be?
     * <p>Sets the message output level.</p>
     * @param level The level; should be one of the constants defined in this interface.
     */
    public void setMessageLevel(int level);
}
