package org.nineml.coffeegrinder.tokens;

/**
 * A single character {@link Token}.
 */
public class TokenEPSILON extends Token {
    public static final TokenEPSILON EPSILON = new TokenEPSILON();

    private TokenEPSILON() {
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
     * <p>This token matches other {@link TokenEPSILON token characters} that have the same
     * character as well as {@link TokenString TokenStrings} that are one character long and
     * contain the same character.</p>
     * @param input The input.
     * @return true if they match.
     */
    @Override
    public boolean matches(Token input) {
        return input instanceof TokenEPSILON;
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
        return obj instanceof TokenEPSILON;
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
        return "ε";
    }
}
