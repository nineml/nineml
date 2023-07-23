package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.AttributeException;
import org.nineml.coffeegrinder.exceptions.GrammarException;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.util.Decoratable;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.Collection;

/**
 * A grammar symbol.
 *
 * <p>Symbols match either tokens in the input (for {@link TerminalSymbol} symbols) or other symbols
 * (for {@link NonterminalSymbol} symbols). For convenience, this interface defines both match methods
 * for all Symbols.</p>
 */
public abstract class Symbol extends Decoratable {
    /**
     * Create a symbol with no attributes.
     */
    public Symbol() {
        super();
    }

    /**
     * Create a symbol with an initial set of attributes.
     * @param attributes the attributes
     * @throws GrammarException if the attribute names are not unique
     * @throws AttributeException if an attribute has an invalid value
     */
    public Symbol(Collection<ParserAttribute> attributes) {
        super(attributes);
    }

    /**
     * Does this symbol match the specified token?
     * <p>This is very like equality, but consider that for some kinds of symbols (for example, tokens
     * that match regular expressions) it isn't really the same as equality.</p>
     * @param input The token.
     * @return true if the token matches.
     */
    public abstract boolean matches(Token input);

    /**
     * Does this symbol match the specified symbol?
     * @param input The symbol.
     * @return true if it is the same symbol as this symbol.
     */
    public abstract boolean matches(Symbol input);
}
