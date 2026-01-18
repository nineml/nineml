package org.nineml.coffeegrinder.exceptions;

/**
 * Exceptions raised by this API.
 * <p>With a few exceptions (@{link NullPointerException} and {@link IllegalArgumentException}, for example),
 * subclasses of {@link CoffeeGrinderException} are used for all exceptions raised by this API.</p>
 */
public abstract class CoffeeGrinderException extends RuntimeException {
    private final String code;

    /**
     * An exception with a message.
     * @param code the code
     * @param message the message
     */
    public CoffeeGrinderException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * An exception with an underlying cause.
     * @param code the code
     * @param message the message
     * @param cause the cause
     */
    public CoffeeGrinderException(String code, String message, Throwable cause) {
        super(message,cause);
        this.code = code;
    }

    /**
     * Get the error code.
     * @return the error code.
     */
    public String getCode() {
        return code;
    }
}
