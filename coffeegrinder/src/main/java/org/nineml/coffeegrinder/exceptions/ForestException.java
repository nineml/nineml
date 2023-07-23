package org.nineml.coffeegrinder.exceptions;

/**
 * Exceptions that arise processing the shared packed parse forest (SPPF).
 * <p>These are generally caused by errors in how the API is used.</p>
 */
public class ForestException extends CoffeeGrinderException {
    /**
     * An SPPF exception with a message.
     * @param code the code
     * @param message the message
     */
    public ForestException(String code, String message) {
        super(code, message);
    }

    /**
     * An SPPF exception with an underlying cause.
     * @param code the code
     * @param message the message
     * @param cause the cause
     */
    public ForestException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    private static ForestException getException(String code, String param) {
        return getException(code, new String[] {param});
    }

    private static ForestException getException(String code, String[] params) {
        return new ForestException(code, MessageGenerator.getMessage(code, params));
    }

    /**
     * An I/O exception.
     * @param filename filename being accessed at the time of error
     * @param ex the underlying exception
     * @return a ForestException
     */
    public static ForestException ioError(String filename, Exception ex) {
        String code = "F001";
        return new ForestException(code, MessageGenerator.getMessage(code, new String[] {filename}), ex);
    }

    /**
     * A no-such-node exception.
     * <p>This is an internal error.</p>
     * @param node The node.
     * @return A ForestException
     */
    public static ForestException noSuchNode(String node) { return getException("F002", node); }

    /**
     * A cannot-add-child exception.
     * <p>This is an internal error.</p>
     * @param node The node.
     * @return A ForestException
     */
    public static ForestException cannotAddChild(String node) { return getException("F003", node); }
}
