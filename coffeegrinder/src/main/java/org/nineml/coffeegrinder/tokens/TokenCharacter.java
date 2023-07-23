package org.nineml.coffeegrinder.tokens;

import org.nineml.coffeegrinder.util.CodepointToString;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * A single character {@link Token}.
 */
public class TokenCharacter extends Token {
    private final String chstr; // String because non-BMP characters are longer than one Java char
    private final int codepoint;

    private TokenCharacter(int codepoint,  Collection<ParserAttribute> attributes) {
        super(attributes);
        this.codepoint = codepoint;
        StringBuilder sb = new StringBuilder();
        sb.appendCodePoint(codepoint);
        chstr = sb.toString();
    }

    /**
     * Create a token for the specified character.
     * @param ch the character
     * @return a token
     */
    public static TokenCharacter get(int ch) {
        return new TokenCharacter(ch, null);
    }

    /**
     * Create a token for the specified character.
     * @param ch the character
     * @param attribute the attribute
     * @return a token
     */
    public static TokenCharacter get(int ch, ParserAttribute attribute) {
        if (attribute == null) {
            throw new NullPointerException("Token attribute must not be null");
        }
        return new TokenCharacter(ch, Collections.singletonList(attribute));
    }

    /**
     * Create a token for the specified character.
     * @param ch the character
     * @param attributes the attributes
     * @return a token
     */
    public static TokenCharacter get(int ch, Collection<ParserAttribute> attributes) {
        return new TokenCharacter(ch, attributes);
    }

    /**
     * Return the value of this token (its character).
     * @return The character value of the token.
     */
    public String getValue() {
        return chstr;
    }

    /**
     * Return the codepoint value of this token (its character codepoint)
     * @return the codepoint
     */
    public int getCodepoint() {
        return codepoint;
    }

    /**
     * Does this token match the input?
     * <p>This token matches other {@link TokenCharacter token characters} that have the same
     * character as well as {@link TokenString TokenStrings} that are one character long and
     * contain the same character.</p>
     * @param input The input.
     * @return true if they match.
     */
    @Override
    public boolean matches(Token input) {
        if (input instanceof TokenCharacter) {
            return codepoint == ((TokenCharacter) input).codepoint;
        }

        if (input instanceof TokenString) {
            return chstr.equals(input.getValue());
        }
        return false;
    }

    /**
     * Does this token match this string?
     * @param input the input string.
     * @return true if it's a single-character long string containing the same character as this token.
     */
    public boolean matches(String input) {
        return chstr.equals(input);
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
        if (obj instanceof TokenCharacter) {
            return codepoint == ((TokenCharacter) obj).codepoint;
        }
        return false;
    }

    /**
     * Assure that equal tokens return the same hash code.
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return codepoint;
    }

    /**
     * Pretty print a token.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return CodepointToString.of(codepoint);
    }
}
