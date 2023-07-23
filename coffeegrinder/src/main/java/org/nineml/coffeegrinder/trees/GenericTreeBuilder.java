package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.tokens.Token;

import java.util.Map;

/**
 * A tree builder that constructs a {@link GenericTree}.
 */
public class GenericTreeBuilder implements TreeBuilder {
    private GenericBranch tree = null;
    private GenericBranch root = null;
    private GenericBranch current = null;

    public GenericBranch getTree() {
        return tree;
    }

    @Override
    public void startTree() {
        current = null;
        tree = null;
        root = null;
    }

    @Override
    public void endTree(boolean ambiguous, boolean absolutelyAmbiguous, boolean infinitelyAmbiguous) {
        current = null;
        tree = root;
        root = null;
    }

    @Override
    public void startNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        if (root == null) {
            root = new GenericBranch(symbol, attributes, leftExtent, rightExtent);
            current = root;
        } else {
            current = (GenericBranch) current.addChild(new GenericBranch(symbol, attributes, leftExtent, rightExtent));
        }
    }

    @Override
    public void endNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        current = current.getParent();
    }

    @Override
    public void token(Token token, Map<String,String> attributes, int leftExtent, int rightExtent) {
         current.addChild(new GenericLeaf(token, attributes, leftExtent, rightExtent));
    }
}
