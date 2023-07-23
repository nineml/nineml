package org.nineml.coffeegrinder.exceptions;

/**
 * Compiler exceptions.
 * <p>Compiler exceptions identify problems encountered attempting to compile
 * a grammar or parse a compiled grammar.</p>
 */
public class CompilerException extends GrammarException {
    /**
     * Grammar exception with a message.
     * @param code the code
     * @param message the message
     */
    public CompilerException(String code, String message) {
        super(code, message);
    }

    /**
     * Grammar exception with an underlying cause.
     * @param code the code
     * @param message the message
     * @param cause the cause
     */
    public CompilerException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    private static CompilerException getException(String code) {
        return getException(code, new String[] {});
    }

    private static CompilerException getException(String code, String param) {
        return getException(code, new String[] {param});
    }

    private static CompilerException getException(String code, String param1, String param2) {
        return getException(code, new String[] {param1, param2});
    }

    private static CompilerException getException(String code, String[] params) {
        return new CompilerException(code, MessageGenerator.getMessage(code, params));
    }

    /**
     * Raised if the digest cannot be created.
     * <p>This should never happen.</p>
     * @param hash the hash
     * @param reason the underlying exception message
     * @return a CompilerException
     */
    public static CompilerException messageDigestError(String hash, String reason) { return getException("C001", hash, reason); }

    /**
     * Raised if an unexpected character set is used.
     * @param charset the characterset.
     * @return a CompilerException
     */
    public static CompilerException unexpectedCharacterSet(String charset) { return getException("C002", charset); }

    /**
     * Raised if an unexpected terminal token class is encountered
     * @param tokenClass the token class
     * @return a CompilerException
     */
    public static CompilerException unexpectedTerminalTokenClass(String tokenClass) { return getException("C003", tokenClass); }

    /**
     * Raised if a name is improperly escaped.
     * @param escape The invalid escape
     * @param name The full name
     * @return a CompilerException
     */
    public static CompilerException invalidNameEscaping(String escape, String name) { return getException("C004", escape, name); }

    /**
     * Raised if an error occurs reading the grammar
     * @param message a detail message
     * @return a CompilerException
     */
    public static CompilerException errorReadingGrammar(String message) { return getException("C005", message); }

    /**
     * Raised if the compiled grammar is not in the correct namespace.
     * @param namespace the root element namespace
     * @return a CompilerException
     */
    public static CompilerException notAGrammar(String namespace) { return getException("C006", namespace); }

    /**
     * Raised if the compiled grammar contains an element with an unexpected name.
     * @param name the element name
     * @return a CompilerException
     */
    public static CompilerException unexpectedElement(String name) { return getException("C007", name); }

    /**
     * Raised if the compiled grammar has no version attribute.
     * @return a CompilerException
     */
    public static CompilerException noVersionProvided() { return getException("C008"); }

    /**
     * Raised if the version is unrecognized.
     * @param version the version.
     * @return a CompilerException
     */
    public static CompilerException unsupportedVersion(String version) { return getException("C009", version); }

    /**
     * Raised if the compiled grammar checksum doesn't match the computed value.
     * @return a CompilerException
     */
    public static CompilerException checkumFailed() { return getException("C010"); }

    /**
     * Raised if an element is missing a required xml:id
     * @param name the name of the element
     * @return a CompilerException
     */
    public static CompilerException missingXmlId(String name) { return getException("C011", name); }

    /**
     * Raised if an attribute group is missing
     * @param id the id of the group
     * @return a CompilerException
     */
    public static CompilerException missingAttributeGroup(String id) { return getException("C012", id); }

    /**
     * Raised if the internal flag value is unrecognized.
     * @param flag the flag value
     * @return a CompilerException
     */
    public static CompilerException unexpectedFlag(String flag) { return getException("C013", flag); }

    /**
     * Raised if the grammar is invalid.
     * @param message a detail message
     * @return a CompilerException
     */
    public static CompilerException invalidGramamr(String message) { return getException("C014", message); }

    /**
     * Raised if text is used where it isn't allowed.
     * @param text the text
     * @return a CompilerException
     */
    public static CompilerException textNotAllowed(String text) { return getException("C015", text); }
}
