package org.nineml.coffeefilter.model;

public class IOverrides extends XNonterminal {
    /**
     * Create an IOverrides.
     *
     * @param parent The parent node.
     */
    public IOverrides(XNode parent) {
        super(parent, "overrides");
    }

    @Override
    protected XNode copy() {
        return null;
    }
}
