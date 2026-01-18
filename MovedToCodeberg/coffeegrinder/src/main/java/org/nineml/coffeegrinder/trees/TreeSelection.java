package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeegrinder.parser.ForestNode;

public class TreeSelection {
    public final ForestNode node;
    public final ParseTree parent;
    public final Family selection;
    public TreeSelection(ForestNode node, ParseTree parent, Family selection) {
        this.node = node;
        this.parent = parent;
        this.selection = selection;
    }
}
