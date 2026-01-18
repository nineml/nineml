package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A branch (nonterminal) in a generic tree.
 */
public class GenericBranch extends GenericTree {
    public final NonterminalSymbol symbol;
    private final ArrayList<GenericTree> children;
    private List<GenericTree> immutableChildren = null;

    /* package */ GenericBranch(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        super(attributes, leftExtent, rightExtent);
        this.symbol = symbol;
        children = new ArrayList<>();
    }

    /* package */ GenericTree addChild(GenericTree child) {
        immutableChildren = null;
        child.parent = this;
        children.add(child);
        return child;
    }

    @Override
    public List<GenericTree> getChildren() {
        if (immutableChildren == null) {
            immutableChildren = Collections.unmodifiableList(children);
        }
        return immutableChildren;
    }
}
