package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.tokens.Token;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class implements the {@link TreeBuilder} interface, but discards all the output.
 * <p>It is a "no-op" tree builder.</p>
 */
public class NopTreeBuilder implements TreeBuilder {
    @Override
    public void startTree() {
        // nop
    }

    @Override
    public void endTree(boolean ambiguous, boolean absolutelyAmbiguous, boolean infinitelyAmbiguous) {
        // nop
    }

    @Override
    public void startNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        // nop
    }

    @Override
    public void endNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        // nop
    }

    @Override
    public void token(Token token, Map<String,String> attributes, int leftExtent, int rightExtent) {
        // nop
    }

    @Override
    public void startAmbiguity(int id, int leftExtent, int rightExtent) {
        // nop
    }

    @Override
    public void endAmbiguity(int id, int leftExtent, int rightExtent) {
        // nop
    }
}
