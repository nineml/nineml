package org.nineml.coffeefilter.model;

public class IShares extends XNonterminal {
    /**
     * Create an IShares.
     *
     * @param parent The parent node.
     */
    public IShares(XNode parent) {
        super(parent, "shares");
    }

    @Override
    protected XNode copy() {
        return null;
    }
}
