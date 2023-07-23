package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.GrammarException;
import org.nineml.coffeegrinder.tokens.*;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * A terminal symbol.
 * <p>Terminal symbols match some input token(s) literally.</p>
 */
public class TerminalSymbol extends Symbol {
    public static final TerminalSymbol EPSILON = new TerminalSymbol(TokenEPSILON.EPSILON);
    public static final TerminalSymbol EOF = new TerminalSymbol(TokenEOF.EOF);
    protected Token token;

    /**
     * Make a symbol for the specified token.
     * @param token the token
     * @throws NullPointerException if the token is null
     */
    public TerminalSymbol(Token token) {
        super();
        if (token == null) {
            throw new NullPointerException("Token must not be null");
        }
        this.token = token;
    }

    /**
     * Make a symbol for the specified token with a given ParserAttribute.
     * @param token the token
     * @param attribute the attribute
     * @throws NullPointerException if either is null
     * @throws GrammarException if the attribute attempts to make the symbol optional
     */
    public TerminalSymbol(Token token, ParserAttribute attribute) {
        this(token, Collections.singletonList(attribute));
    }

    /**
     * Make a symbol for the specified token with the given attributes
     * @param token the token
     * @param attributes a collection of attributes
     * @throws NullPointerException if the token is null
     * @throws GrammarException if the attributes attempt to make the symbol optional
     */
    public TerminalSymbol(Token token, Collection<ParserAttribute> attributes) {
        super(attributes);

        if (token == null) {
            throw new NullPointerException("Token must not be null");
        }
        this.token = token;
    }

    /**
     * Return a token for a string.
     * <p>This is just a convenience method for a terminal symbol for a {@link TokenString}.</p>
     * @param terminal the string
     * @return the terminal symbol
     */
    public static TerminalSymbol s(String terminal) {
        return new TerminalSymbol(TokenString.get(terminal));
    }

    /**
     * Return a token for a string.
     * <p>This is just a convenience method for a terminal symbol for a {@link TokenCharacter}.</p>
     * @param terminal the character
     * @return the terminal symbol
     */
    public static TerminalSymbol ch(char terminal) {
        return new TerminalSymbol(TokenCharacter.get(terminal));
    }

    /**
     * Return a token for a string.
     * <p>This is just a convenience method for a terminal symbol for a {@link TokenRegex}.</p>
     * @param regex the regex
     * @return the terminal symbol
     */
    public static TerminalSymbol regex(String regex) {
        return new TerminalSymbol(TokenRegex.get(regex));
    }

    /**
     * Get the token associated with this terminal symbol.
     * @return the token
     */
    public Token getToken() {
        return token;
    }

    /**
     * Does this symbol match the given token?
     * @param input The token.
     * @return true if it matches
     */
    @Override
    public boolean matches(Token input) {
        return token.matches(input);
    }

    /**
     * Does this symbol match this other symbol?
     * <p>No, it does not. No terminal ever matches another symbol.</p>
     *
     * @param input The other symbol.
     * @return false
     */
    @Override
    public final boolean matches(Symbol input) {
        return false;
    }

    /**
     * Test tokens for equality.
     *
     * <p>Two tokens are equal if they represent the same string and have the same attributes.</p>
     *
     * @param obj An object.
     * @return true if <code>obj</code> is equal to this terminal character.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TerminalSymbol) {
            TerminalSymbol other = (TerminalSymbol) obj;
            if (!token.equals(other.token) || getAttributes().size() != other.getAttributes().size()) {
                return false;
            }

            if (getAttributes().isEmpty()) {
                return true;
            }

            HashMap<String,String> attrs = new HashMap<>();
            for (ParserAttribute attr : getAttributes()) {
                attrs.put(attr.getName(), attr.getValue());
            }
            for (ParserAttribute attr : other.getAttributes()) {
                if (!attrs.containsKey(attr.getName()) || !attr.getValue().equals(attrs.get(attr.getName()))) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    /**
     * Assure that equal tokens return the same hash code.
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return 3 * token.hashCode() + 31 * getAttributes().size();
    }

    /**
     * Pretty print a token.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return token.toString();
    }

}
