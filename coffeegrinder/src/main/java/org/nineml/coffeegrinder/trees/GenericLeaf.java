package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.tokens.Token;

import java.util.Map;

/**
 * A leaf (terminal) in a generic tree.
 */
public class GenericLeaf extends GenericTree {
    public final Token token;

    /* package */ GenericLeaf(Token token, Map<String,String> attributes, int leftExtent, int rightExtent) {
        super(attributes, leftExtent, rightExtent);
        this.token = token;
    }

}
