package org.nineml.coffeefilter.model;

public class IUses extends XNonterminal {
    /**
     * Create an IUses.
     *
     * @param parent The parent node.
     */
    public IUses(XNode parent) {
        super(parent, "uses");
    }

    @Override
    protected XNode copy() {
        return null;
    }
}
