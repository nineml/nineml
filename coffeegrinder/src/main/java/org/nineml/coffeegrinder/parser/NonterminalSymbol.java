package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.List;

/**
 * A nonterminal symbol in the grammar.
 * <p>Every nonterminal must be defined by a {@link Rule Rule} in the
 * {@link ParserGrammar Grammar}.</p>
 */
public class NonterminalSymbol extends Symbol {
    public final String symbolName;
    /* package private */ String realName;
    private final Grammar grammar;

    protected NonterminalSymbol(Grammar grammar, String name, List<ParserAttribute> attributes) {
        super(attributes);

        if (name == null) {
            throw new NullPointerException("Nonterminal symbol cannot be null");
        }

        this.grammar = grammar;
        this.symbolName = name;
        realName = name;
    }

    /**
     * The name of this symbol.
     * @return the name of the symbol.
     */
    public String getName() {
        return realName;
    }

    /**
     * Are these the same symbol?
     * @param input The symbol.
     * @return true if they're the same nonterminal symbol.
     */
    @Override
    public final boolean matches(Symbol input) {
        if (input instanceof NonterminalSymbol) {
            return this.equals(input);
        }
        return false;
    }

    /**
     * Does this symbol match the input token?
     * <p>No, it does not. No nonterminal ever matches an input token.</p>
     * @param input The token.
     * @return false
     */
    @Override
    public final boolean matches(Token input) {
        return false;
    }

    /**
     * Test nonterminals for equality.
     *
     * <p>Two nonterminals are equal if they have the same name.</p>
     *
     * @param obj An object.
     * @return true if <code>obj</code> is equal to this nonterminal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NonterminalSymbol) {
            return symbolName.equals(((NonterminalSymbol) obj).symbolName);
        }
        return false;
    }

    /**
     * Assure that equal nonterminals return the same hash code.
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return 7 * symbolName.hashCode();
    }

    /**
     * Pretty print a nonterminal.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        if (grammar.isNullable(this)) {
            return symbolName + "ⁿ";
        }
        return symbolName;
    }
}
