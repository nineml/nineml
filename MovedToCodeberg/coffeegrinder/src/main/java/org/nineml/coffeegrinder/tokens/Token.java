package org.nineml.coffeegrinder.tokens;

import org.nineml.coffeegrinder.exceptions.AttributeException;
import org.nineml.coffeegrinder.exceptions.GrammarException;
import org.nineml.coffeegrinder.util.Decoratable;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * An input token.
 *
 * <p>This is an abstraction for input tokens. It allows the parser to be used, for example, for both
 * sequences of characters and sequences of strings. (Sequences of anything you like, provided you define
 * the tokens.)</p>
 * <p>The only thing that's important about tokens is that we can tell when they match each other.
 * This is not the same as equality becuase, for example, the same regular expression token might
 * match many different input strings.</p>
 */
public abstract class Token extends Decoratable {
    /**
     * A token with attributes.
     *
     * @param attributes the attributes
     * @throws GrammarException if the attribute names are not unique
     * @throws AttributeException if an attribute has an invalid value
     */
    public Token(Collection<ParserAttribute> attributes) {
        super(attributes);
    }

    /**
     * Does this token match the input?
     *
     * @param input The input.
     * @return true if this token matches that input.
     */
    public abstract boolean matches(Token input);

    /**
     * What is this token?
     * @return the token string
     */
    public abstract String getValue();
}
