package org.nineml.coffeefilter.model;

public class IShare extends XNonterminal {
    public final String symbolName;

    /**
     * Create an IUses.
     *
     * @param parent The parent node.
     */
    public IShare(XNode parent, String symbolName) {
        super(parent, "share");
        this.symbolName = symbolName;
    }

    @Override
    protected XNode copy() {
        return null;
    }
}
