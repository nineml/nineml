package org.nineml.coffeegrinder.exceptions;

/**
 * Exceptions that arise processing the attributes of a symbol or token.
 * <p>These are generally caused by errors in how the API is used.</p>
 */
public class AttributeException extends CoffeeGrinderException {
    /**
     * An attribute exception with a message.
     * @param code the code
     * @param message the message
     */
    public AttributeException(String code, String message) {
        super(code, message);
    }

    /**
     * An attribute exception with an underlying cause.
     * @param code the code
     * @param message the message
     * @param cause the cause
     */
    public AttributeException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    private static AttributeException getException(String code, String param) {
        return getException(code, new String[] {param});
    }

    private static AttributeException getException(String code, String param1, String param2) {
        return getException(code, new String[] {param1, param2});
    }

    private static AttributeException getException(String code, String[] params) {
        return new AttributeException(code, MessageGenerator.getMessage(code, params));
    }

    /**
     * Raised if an attempt is made to set an invalid option value.
     * @param value the value.
     * @return an AttributeException.
     */
    public static AttributeException invalidOPTIONAL(String value) { return getException("A001", value); }

    /**
     * Raised if an attempt is made to modify an existing attribute.
     * @param name the attribute name.
     * @param value the value.
     * @return an AttributeException.
     */
    public static AttributeException immutable(String name, String value) { return getException("A003", name, value); }


}
