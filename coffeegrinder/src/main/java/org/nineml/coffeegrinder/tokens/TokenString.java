package org.nineml.coffeegrinder.tokens;

import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.Collection;
import java.util.Collections;

/**
 * A string {@link Token}.
 */
public class TokenString extends Token {
    private final String value;

    /**
     * Create an instance of this token for the specified string.
     * @param value the string value.
     */
    private TokenString(String value, Collection<ParserAttribute> attributes) {
        super(attributes);
        if (value == null) {
            throw new NullPointerException("TokenString value must not be null");
        }
        this.value = value;
    }

    /**
     * Create a token for the specified string.
     * @param value the string
     * @return the token
     */
    public static TokenString get(String value) {
        return new TokenString(value, null);
    }

    /**
     * Create a token for the specified string (with an attribute)
     * @param value the string
     * @param attribute the attribute
     * @return the token
     */
    public static TokenString get(String value, ParserAttribute attribute) {
        return new TokenString(value, Collections.singletonList(attribute));
    }

    /**
     * Create a token for the specified string (with attributes)
     * @param value the string
     * @param attributes the attributes
     * @return the token
     */
    public static TokenString get(String value, Collection<ParserAttribute> attributes) {
        return new TokenString(value, attributes);
    }

    /**
     * Get the string.
     * @return The string value of this token.
     */
    public String getValue() {
        return value;
    }

    /**
     * Does this token match the input?
     * <p>This token matches other {@link TokenString token strings} that have the same
     * underlying string. If this is a single character string, it will also match
     * {@link TokenCharacter TokenCharacters} that are defined with the same character.
     * @param input The input.
     * @return true if they match.
     */
    @Override
    public boolean matches(Token input) {
        if (input instanceof TokenString || input instanceof TokenCharacter) {
            return value.equals(input.getValue());
        }
        return false;
    }

    /**
     * Does this toke match this character?
     * @param input the character.
     * @return true if this token matches.
     */
    public boolean matches(char input) {
        return value.length() == 1 && value.charAt(0) == input;
    }

    /**
     * Does this token match this string?
     * @param input the string.
     * @return true if the token matches.
     */
    public boolean matches(String input) {
        return value.equals(input);
    }

    /**
     * Test tokens for equality.
     *
     * <p>Two tokens are equal if they represent the same string.</p>
     *
     * @param obj An object.
     * @return true if <code>obj</code> is equal to this terminal character.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TokenString) {
            return value.equals(((TokenString) obj).value);
        }
        return false;
    }

    /**
     * Assure that equal tokens return the same hash code.
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return 3 * value.hashCode();
    }

    /**
     * Pretty print a token.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        String output = value.replaceAll("\"", "\"\"");
        output = output.replaceAll("[\t]", "\\t");
        output = output.replaceAll("[\r]", "\\r");
        output = output.replaceAll("[\n]", "\\n");
        return "\"" + output + "\"";
    }
}
