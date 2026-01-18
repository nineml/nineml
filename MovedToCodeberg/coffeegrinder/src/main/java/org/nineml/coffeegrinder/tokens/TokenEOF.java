package org.nineml.coffeegrinder.tokens;

/**
 * A token that matches end of file.
 * <p>This token is used by the GLL parser.</p>
 */
public class TokenEOF extends Token {
    public static final TokenEOF EOF = new TokenEOF();

    private TokenEOF() {
        super(null);
    }

    /**
     * Return the string value of this token.
     * @return The string value of the token ("&lt;EOF&gt;").
     */
    public String getValue() {
        return "<EOF>";
    }

    /**
     * Does this token match the input?
     * <p>The {@code TokenEOF} is a singleton. It matches itself, nothing else.</p>
     * @param input The input.
     * @return true if they match.
     */
    @Override
    public boolean matches(Token input) {
        return input instanceof TokenEOF;
    }

    /**
     * Test tokens for equality.
     *
     * <p>Two tokens are equal if they represent the same character.</p>
     *
     * @param obj An object.
     * @return true if <code>obj</code> is equal to this terminal character.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof TokenEOF;
    }

    /**
     * Assure that equal tokens return the same hash code.
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return 65583;
    }

    /**
     * Pretty print a token.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "<EOF>";
    }
}
