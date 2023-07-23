package org.nineml.coffeegrinder.tokens;

import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A regular expression token {@link Token}.
 */
public class TokenRegex extends Token {
    private final Pattern regex;
    private final String expr;

    private TokenRegex(String expr, Collection<ParserAttribute> attributes) {
        super(attributes);
        if (expr == null) {
            throw new NullPointerException("TokenRegex expression must not be null");
        }
        if (expr.startsWith("^") || expr.endsWith("$")) {
            throw new IllegalArgumentException("TokenRegex must not be anchored");
        }
        this.expr = expr;
        regex = Pattern.compile(expr);
    }

    /**
     * Create a token for the specified regular expression.
     * @param expr the expression
     * @return the token
     */
    public static TokenRegex get(String expr) {
        return new TokenRegex(expr, null);
    }

    /**
     * Create a token for the specified regular expression (with an attribute)
     * @param expr the expression
     * @param attribute the attribute
     * @return the token
     * @throws NullPointerException if the attribute is null
     */
    public static TokenRegex get(String expr, ParserAttribute attribute) {
        if (attribute == null) {
            throw new NullPointerException("Token parser attribute must not be null");
        }
        return new TokenRegex(expr, Collections.singletonList(attribute));
    }

    /**
     * Create a token for the specified regular expression (with attributes)
     * @param expr the expression
     * @param attributes the attributes
     * @return the token
     */
    public static TokenRegex get(String expr, Collection<ParserAttribute> attributes) {
        return new TokenRegex(expr, attributes);
    }

    /**
     * Get the regular expression.
     * @return The regular expression used by this token.
     */
    public String getValue() {
        return expr;
    }

    /**
     * Does this token match the input token?
     * <p>A regular expression token matches a {@link TokenCharacter} or {@link TokenString}
     * if the regular expression matches the value. Returns fals for all other kinds of tokens.</p>
     * @param input The input.
     * @return true if they match
     */
    public final boolean matches(Token input) {
        if (input instanceof TokenCharacter) {
            return regex.matcher(input.getValue()).matches();
        }
        if (input instanceof TokenString) {
            return regex.matcher(input.getValue()).matches();
        }
        return false;
    }

    public final String matches(String input) {
        Matcher matcher = regex.matcher(input);
        if (matcher.lookingAt()) {
            return input.substring(0, matcher.end());
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TokenRegex) {
            return ((TokenRegex) obj).expr.equals(expr);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 11 + (31 * expr.hashCode());
    }

    @Override
    public String toString() {
        return regex.toString();
    }
}
