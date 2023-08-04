package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.tokens.Token;

import java.util.Map;
import java.util.Set;

/**
 * The tree builder interface.
 */
public interface TreeBuilder {
    /**
     * Called first, when construction begins.
     */
    void startTree();

    /**
     * Called last, when construction finishes.
     *
     * @param ambiguous was the parse ambiguous?
     * @param absolutelyAmbiguous is the graph ambiguous?
     * @param infinitelyAmbiguous is the graph infinitely ambiguous?
     */
    void endTree(boolean ambiguous, boolean absolutelyAmbiguous, boolean infinitelyAmbiguous);

    /**
     * Called when a new nonterminal begins.
     * @param symbol The symbol.
     * @param attributes Its attributes.
     * @param leftExtent The starting position in the input.
     * @param rightExtent The ending position in the input.
     */
    void startNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent);

    /**
     * Called when a nonterminal ends.
     * @param symbol The symbol.
     * @param attributes Its attributes.
     * @param leftExtent The starting position in the input.
     * @param rightExtent The ending position in the input.
     */
    void endNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent);

    /**
     * Called when a terminal occurs.
     * @param token The terminal token.
     * @param attributes Its attributes.
     * @param leftExtent The starting position in the input.
     * @param rightExtent The ending position in the input.
     */
    void token(Token token, Map<String,String> attributes, int leftExtent, int rightExtent);

    /**
     * Called at the start of an ambiguity that is not marked by a single nonterminal.
     * <p>The ambiguity id will distinguish this ambiguity from any other ambiguity. The numbers are not
     * sequential or guaranteed stable across parses.</p>
     * @param id The ambiguity id.
     * @param leftExtent The starting position in the input.
     * @param rightExtent The ending position in the input.
     */
    void startAmbiguity(int id, int leftExtent, int rightExtent);

    /**
     * Called at the end of an ambiguity that is not marked by a single nonterminal.
     * @param id The ambiguity id.
     * @param leftExtent The starting position in the input.
     * @param rightExtent The ending position in the input.
     */
    void endAmbiguity(int id, int leftExtent, int rightExtent);
}
