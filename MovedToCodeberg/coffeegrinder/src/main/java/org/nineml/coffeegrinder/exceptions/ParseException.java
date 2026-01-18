package org.nineml.coffeegrinder.exceptions;

/**
 * Parse exceptions.
 * <p>Parse exceptions are generally errors in the API, or uses of the API.</p>
 */
public class ParseException extends CoffeeGrinderException {
    /**
     * An parse exception with a message.
     * @param code the code
     * @param message the message
     */
    public ParseException(String code, String message) {
        super(code, message);
    }

    /**
     * An parse exception with an underlying cause.
     * @param code the code
     * @param message the message
     * @param cause the cause
     */
    public ParseException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    private static ParseException getException(String code) {
        return getException(code, new String[] {});
    }

    private static ParseException getException(String code, String param) {
        return getException(code, new String[] {param});
    }

    private static ParseException getException(String code, String param1, String param2) {
        return getException(code, new String[] {param1, param2});
    }

    private static ParseException getException(String code, String[] params) {
        return new ParseException(code, MessageGenerator.getMessage(code, params));
    }

    /**
     * Raised if the selected seed token is not in the grammar
     * @param seed the seed name
     * @return a ParseException
     */
    public static ParseException seedNotInGrammar(String seed) { return getException("P001", seed); }

    /**
     * Raised if an attempt is made to continue after an invalid parse.
     * @return a ParseException
     */
    public static ParseException attemptToContinueInvalidParse() { return getException("P002"); }

    /**
     * Raised if an internal error occurs.
     * @param reason a detail message.
     * @return a ParseException
     */
    public static ParseException internalError(String reason) { return getException("P003", reason); }

    /**
     * Raised if an attempt is made to parse an invalid input.
     *
     * <p>The GLL parser can only parse characters, this exception is raised if it is used to
     * parse any other kind of tokens.</p>
     *
     * @return a ParseException
     */
    public static ParseException invalidInputForGLL() { return getException("P004"); }

    /**
     * Raised if an attempt is made to parse an invalid input.
     *
     * <p>Regular expressions are only supported over a sequence of tokens.</p>
     *
     * @return a ParseException
     */
    public static ParseException invalidInputForRegex() { return getException("P005"); }

    /**
     * Raised if an attempt is made to continue with an incompatible parser.
     * @return a ParseException
     */
    public static ParseException attemptToContinueWithIncompatibleParser() { return getException("P006"); }


}
