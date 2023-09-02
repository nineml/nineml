package org.nineml.coffeegrinder.tokens;

/**
 * A token that can match nothing.
 * <p>This is used to provide a "right hand side" for undefined tokens. They can't match anything.</p>
 */
public class TokenUndefined extends Token {
    public static final TokenUndefined undefined = new TokenUndefined();

    private TokenUndefined() {
        super(null);
    }

    /**
     * Return the string value of this token.
     * @return The string value of the token ("&lt;undefined&gt;").
     */
    public String getValue() {
        return "<undefined>";
    }

    /**
     * Does this token match the input?
     * <p>No, it does not.</p>
     * @param input The input.
     * @return true if they match.
     */
    @Override
    public boolean matches(Token input) {
        return false;
    }

    /**
     * Test tokens for equality.
     *
     * <p>The singleton {@code TokenUndefined} is only equal to itself.</p>
     *
     * @param obj An object.
     * @return true if <code>obj</code> is equal to this terminal character.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof TokenUndefined;
    }

    /**
     * Assure that equal tokens return the same hash code.
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return 65582;
    }

    /**
     * Pretty print a token.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "<undefined>";
    }
}
